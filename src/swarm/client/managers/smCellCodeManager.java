package swarm.client.managers;

import java.util.HashSet;
import java.util.logging.Logger;

import swarm.client.app.smAppContext;
import swarm.client.entities.smBufferCell;
import swarm.client.entities.smA_ClientUser;
import swarm.client.entities.smE_CellNuke;
import swarm.client.entities.smE_CodeStatus;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.structs.smCellCodeCache;
import swarm.client.structs.smI_LocalCodeRepository;
import swarm.client.transaction.smE_ResponseErrorControl;
import swarm.client.transaction.smE_ResponseSuccessControl;
import swarm.client.transaction.smE_TransactionAction;
import swarm.client.transaction.smI_TransactionResponseHandler;
import swarm.client.transaction.smClientTransactionManager;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.app.smS_App;
import swarm.shared.code.smCompilerResult;
import swarm.shared.code.smE_CompilationStatus;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smA_Cell;
import swarm.shared.entities.smE_CodeSafetyLevel;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smI_WritesJson;
import swarm.shared.json.smMutableJsonQuery;
import swarm.shared.json.smJsonHelper;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smI_RequestPath;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;


import com.google.gwt.http.client.RequestBuilder;

/**
 * Encapsulates all cell code management, including going out to server.
 * 
 * @author Doug
 *
 */
public class smCellCodeManager implements smI_TransactionResponseHandler
{
	public static interface I_SyncOrPreviewDelegate
	{
		void onCompilationFinished(smCompilerResult result);
	}
	
	private static final Logger s_logger = Logger.getLogger(smCellCodeManager.class.getName());
	
	private final smGridCoordinate m_utilCoord = new smGridCoordinate();
	private final smA_Cell m_utilCell = new smA_Cell(){};
	
	I_SyncOrPreviewDelegate m_syncOrPreviewErrorDelegate = null;
	
	private final smMutableJsonQuery m_postQuery = new smMutableJsonQuery();
	private final smMutableJsonQuery m_getQuery	 = new smMutableJsonQuery();
	
	private final smAppContext m_appContext;
	
	public smCellCodeManager(smAppContext appContext)
	{
		m_appContext = appContext;
		
		m_postQuery.addCondition(null);
		
		m_getQuery.addCondition(null);
		m_getQuery.addCondition(null);
	}
	
	public void start(I_SyncOrPreviewDelegate syncOrPreviewErrorDelegate)
	{
		smClientTransactionManager txnMngr = m_appContext.txnMngr;
		txnMngr.addHandler(this);
		
		m_syncOrPreviewErrorDelegate = syncOrPreviewErrorDelegate;
	}
	
	public void stop()
	{
		smClientTransactionManager txnMngr = m_appContext.txnMngr;
		txnMngr.removeHandler(this);
		
		m_syncOrPreviewErrorDelegate = null;
	}
	
	public void flush()
	{
		smClientTransactionManager txnMngr = m_appContext.txnMngr;
		txnMngr.flushRequestQueue();
	}
	
	private void preparePostQuery(smGridCoordinate coord)
	{
		m_postQuery.setCondition(0, coord);
	}
	
	private void prepareGetQuery(smGridCoordinate coord, smE_CodeType type)
	{
		m_getQuery.setCondition(0, coord);
		m_getQuery.setCondition(1, smE_JsonKey.codeType, type.ordinal());
	}
	
	public void nukeFromOrbit(smGridCoordinate coord, smE_CellNuke nukeType)
	{
		if( nukeType == smE_CellNuke.EVERYTHING )
		{
			m_appContext.codeCache.clear(coord);
		}
		
		//--- DRK > If not a user's cell, below we go about ensuring that the cell has all local code references cleared
		//---		by delving into the registered buffer managers and clearing the cell if it's visible.
		smCellBufferManager.Iterator bufferManagerIterator = m_appContext.getRegisteredBufferMngrs();
		for( smCellBufferManager manager = null; (manager = bufferManagerIterator.next()) != null; )
		{
			smCellBuffer buffer = manager.getDisplayBuffer();
			
			if( buffer.getSubCellCount() == 1 ) // NOTE: should always be the case for the snap buffer manager.
			{
				if( buffer.isInBoundsAbsolute(coord) )
				{
					smBufferCell cell = buffer.getCellAtAbsoluteCoord(coord);
					cell.nuke(nukeType);
				}
			}
		}
	}
	
	public void syncCell(smBufferCell cell, smCode sourceCode)
	{
		smGridCoordinate coord = cell.getCoordinate();

		m_appContext.txnMngr.makeRequest(smE_RequestPath.syncCode, sourceCode, coord);

		smCode compiledCode = new smCode(sourceCode.getRawCode(), smE_CodeType.COMPILED);
		compiledCode.setSafetyLevel(smE_CodeSafetyLevel.REQUIRES_VIRTUAL_SANDBOX);
		cell.onSyncStart(sourceCode, compiledCode);
		
		smUserManager userManager = m_appContext.userMngr;
		smA_ClientUser user = userManager.getUser();
		user.onSyncStart(coord, compiledCode);
		
		cell.onServerRequest(smE_CodeType.SPLASH);
	}
	
	public void previewCell(smBufferCell cell, smCode sourceCode)
	{
		//--- DRK > Old scheme required trip to the server to compile preview code.
		//---		With the new scheme, with code being sandboxed client-side, we
		//---		just route things directly back to the cell.
		//syncOrPreviewCell(cell, sourceCode, false);
		smCode previewCode = new smCode(sourceCode.getRawCode(), smE_CodeType.COMPILED);
		previewCode.setSafetyLevel(smE_CodeSafetyLevel.REQUIRES_VIRTUAL_SANDBOX);
		cell.onPreviewSuccess(previewCode);
	}
	
	private boolean isSyncing(smGridCoordinate coord)
	{
		preparePostQuery(coord);
		smClientTransactionManager txnMngr = m_appContext.txnMngr;
		
		return txnMngr.containsDispatchedRequest(smE_RequestPath.syncCode, m_postQuery);
	}
	
	public void populateCell(smBufferCell cell, smI_LocalCodeRepository localHtmlSource, int cellSize, boolean recycled, boolean communicateWithServer, final smE_CodeType eType)
	{
		smClientTransactionManager txnMngr = m_appContext.txnMngr;
		
		smGridCoordinate absCoord = cell.getCoordinate();
		
		if ( recycled )
		{
			cell.onCellRecycled(cellSize);
		}
		
		smE_CodeStatus status = cell.getStatus(eType);
	
		if( status == smE_CodeStatus.NEEDS_CODE || status == smE_CodeStatus.GET_ERROR )
		{
			if( cellSize == 1 )
			{
				if( eType == smE_CodeType.SPLASH && isSyncing(absCoord) )
				{
					cell.onServerRequest(eType);
					
					return;
				}
				
				if( !localHtmlSource.tryPopulatingCell(absCoord, eType, cell) )
				{
					if( communicateWithServer )
					{
						//--- DRK > If we're syncing this cell, it's valid for it to not have compiled html, but we 
						//---		don't want to hit the server because we expect that compiled html from the sync is incoming.
						//---		Generally the cell WILL have both static and dynamic html already because syncing implies that we've visited
						//---		the cell and probably gotten data.  We DO NOT need compiled versions of the cell to be able to update it though.
						//---		Retrieval of the compiled html versions can (theoretically) fail and the retrieval of source can
						//---		succeed, and editing the cell is still allowed...this is an unlikely scenario on many accounts though.
						if( isSyncing(absCoord) )
						{
							smU_Debug.ASSERT(eType != smE_CodeType.SOURCE, "populateCell1");
							
							cell.onServerRequest(eType);
							
							return;
						}
						
						//--- DRK > This if-check is the last line of defense (as of this writing) to prevent duplicate requests from being sent out.
						//---		There are more checks in place before this, but this is necessary to catch at least one fringy case:
						//---
						//---		(1) User navigates quickly over a cell.
						//---		(2) Transaction goes out, and cell gets its status for eType set to WAITING_ON_CODE.
						//---		(3) User navigates past the cell so it's now off screen.
						//---		(4) User navigates quickly back...the cell now has status of NEEDS_CODE despite a transaction being out.
						//---		(5) Since a cell's state is somewhat transient and doesn't know that a transaction is already out, we track it here.
						//---		(6) Cell gets its status set correctly to WAITING_ON_CODE without another transaction going out.
						prepareGetQuery(cell.getCoordinate(), eType);
						if( !txnMngr.containsDispatchedRequest(smE_RequestPath.getCode, m_getQuery) )
						{							
							smI_WritesJson codeTypeWrapper = new smI_WritesJson()
							{
								@Override
								public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
								{
									factory.getHelper().putInt(json_out, smE_JsonKey.codeType, eType.ordinal());
								}
							};
							
							txnMngr.queueRequest(smE_RequestPath.getCode, cell.getCoordinate(), codeTypeWrapper);
						}
					
						cell.onServerRequest(eType);
					}
				}
			}
			else
			{
				smU_Debug.ASSERT(eType != smE_CodeType.SOURCE, "populateCell2");
				
				// TODO: Need to put image url for generated meta image.
			}
		}
	}
	
	private void onGetCellDataSuccess(smTransactionRequest request, smTransactionResponse response)
	{
		m_utilCell.readJson(this.m_appContext.jsonFactory, response.getJsonArgs());
		m_utilCoord.readJson(this.m_appContext.jsonFactory, request.getJsonArgs());
		m_utilCell.getCoordinate().copy(m_utilCoord);

		smUserManager userManager = m_appContext.userMngr;
		smA_ClientUser user = userManager.getUser();
		
		int typeOrdinal = m_appContext.jsonFactory.getHelper().getInt(request.getJsonArgs(), smE_JsonKey.codeType);
		smE_CodeType eCodeType = smE_CodeType.values()[typeOrdinal];
		smCode code = m_utilCell.getCode(eCodeType);
		
		if( code == null )
		{
			//TODO: Get this from somewhere else.
			final String OPEN_CELL_CODE = "<div style='width:100%; height:100%; background-color:#BBBBBB;'></div>";
			
			code = new smCode(OPEN_CELL_CODE, smE_CodeType.values());
			code.setSafetyLevel(smE_CodeSafetyLevel.SAFE);
			m_utilCell.setCode(eCodeType, code);
		}
		
		boolean isCodeNull = code == null;
		
		if( eCodeType == smE_CodeType.SOURCE )
		{
			if( isSyncing(m_utilCoord) )
			{
				smU_Debug.ASSERT(false, "Shouldn't be getting source while posting.");
				
				return;
			}
		}
		
		if( user.isCellOwner(m_utilCoord) )
		{
			smU_Debug.ASSERT(!isCodeNull, "onGetCellDataSuccess1");

			if( !user.setInitialCellData(m_utilCoord, m_utilCell) )
			{
				return;
			}
		}
		else
		{
			//--- DRK > Should only use cache if we can't dump it in user.
			//---		On sign out, user will dump all data into cache to preserve it.
			m_appContext.codeCache.cacheCell(m_utilCell);
		}
		
		smCellBufferManager.Iterator iterator = m_appContext.getRegisteredBufferMngrs();
		for( smCellBufferManager manager = null; (manager = iterator.next()) != null; )
		{
			smCellBuffer buffer = manager.getDisplayBuffer();
			
			//--- DRK > Cell data is only requested if we're close enough to see individually rendered cells.
			//---		If we've since zoomed out, we cache the data as above, but otherwise ignore it.
			if( buffer.getSubCellCount() == 1 )
			{
				if ( buffer.isInBoundsAbsolute(m_utilCoord) )
				{
					smBufferCell cell = buffer.getCellAtAbsoluteCoord(m_utilCoord);

					cell.copy(m_utilCell);
				}
			}
		}
	}
	
	public smE_ResponseSuccessControl onResponseSuccess(smTransactionRequest request, smTransactionResponse response)
	{
		if ( request.getPath() == smE_RequestPath.getCode )
		{
			this.onGetCellDataSuccess(request, response);

			return smE_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == smE_RequestPath.syncCode )
		{
			smUserManager userManager = m_appContext.userMngr;
			smA_ClientUser user = userManager.getUser();
			m_utilCoord.readJson(m_appContext.jsonFactory, request.getJsonArgs());
			
			if( isSyncing(m_utilCoord) )
			{
				return smE_ResponseSuccessControl.BREAK;
			}
			
			//--- DRK > Presumably the user started out as the cell owner, but if they somehow managed to log out,
			//---		totally fringe or even impossible as that may be, then we shouldn't update cell code.
			boolean isStillCellOwner = user.isCellOwner(m_utilCoord);
			
			smCompilerResult result = new smCompilerResult(m_appContext.jsonFactory, response.getJsonArgs());
			smCode sourceCode = new smCode(m_appContext.jsonFactory, request.getJsonArgs(), smE_CodeType.SOURCE);
			
			if( result.getStatus() == smE_CompilationStatus.NO_ERROR )
			{
				smCode splashScreenCode = result.getCode(smE_CodeType.SPLASH);
				smCode compiledCode = result.getCode(smE_CodeType.COMPILED);
				
				//s_logger.info(compiledHtmlStatic + "\n" + compiledDynamicHtml);
				
				//--- A theoretical fringe case is that user syncs cell, signs out, the sync transaction makes
				//--- it to server first, but sign out transaction makes it back to client first, in which case everything
				//--- is valid, but the user doesn't own the cell anymore as far as the client is concerned.  However,
				//--- we do propogate the changes to the buffer cell below, if necessary.
				if( isStillCellOwner )
				{
					user.onSyncSuccess(m_utilCoord, splashScreenCode, compiledCode);
				}
				else
				{
					//--- DRK > Since user is no longer available, we dump this right into the cache.
					m_appContext.codeCache.cacheCode(m_utilCoord, sourceCode, smE_CodeType.SOURCE);
					m_appContext.codeCache.cacheCode(m_utilCoord, splashScreenCode, smE_CodeType.SPLASH);
					m_appContext.codeCache.cacheCode(m_utilCoord, compiledCode, smE_CodeType.COMPILED);
				}
				
				//--- DRK > I think technically we need only supply the compiled html to the main display buffer in this case.
				//---		This is unlike the "get" method, which SHOULD iterate through all registered buffers so
				//---		that there is zero reliance on the LRU cache.  In practice, the LRU cache again probably makes it
				//---		unnecessary to iterate through all buffers for those cases, but I like not having a reliance on it.
				smCellBufferManager.Iterator bufferManagerIterator = m_appContext.getRegisteredBufferMngrs();
				for( smCellBufferManager manager = null; (manager = bufferManagerIterator.next()) != null; )
				{
					smCellBuffer buffer = manager.getDisplayBuffer();

					//--- DRK > The user can technically commit their code and then navigate away while it's in transit to/from
					//---		the server...that's why we have to dig and see if the cell is even visible anymore.
					if( buffer.getSubCellCount() == 1 )
					{
						if ( buffer.isInBoundsAbsolute(m_utilCoord) )
						{
							smBufferCell cell = buffer.getCellAtAbsoluteCoord(m_utilCoord);
							
							cell.onSyncResponseSuccess(splashScreenCode, compiledCode);
						}
					}
				}
				
				m_syncOrPreviewErrorDelegate.onCompilationFinished(result);
			}
			else
			{
				//--- DRK > See above isCellOwner check for the description of the fringe race condition this if-block protects against.
				if( isStillCellOwner )
				{
					user.onSyncResponseError(m_utilCoord);
					
					m_syncOrPreviewErrorDelegate.onCompilationFinished(result);
				}
				
				cell_onServerError(m_utilCoord, null, true);
			}
			
			return smE_ResponseSuccessControl.BREAK;
		}

		return smE_ResponseSuccessControl.CONTINUE;
	}
	
	private void assertNotPosting(smGridCoordinate coord)
	{
		preparePostQuery(m_utilCoord);
		smClientTransactionManager txnMngr = m_appContext.txnMngr;
		
		smU_Debug.ASSERT(!txnMngr.containsDispatchedRequest(smE_RequestPath.syncCode, m_postQuery), "assertNotPosting1");
	}
	
	private void cell_onServerError(smGridCoordinate coordinate, smE_CodeType eCodeType, boolean isSync)
	{
		smCellBufferManager.Iterator bufferManagerIterator = m_appContext.getRegisteredBufferMngrs();
		for( smCellBufferManager manager = null; (manager = bufferManagerIterator.next()) != null; )
		{
			smCellBuffer buffer = manager.getDisplayBuffer();
			
			//--- DRK > Cell data is only requested if we're close enough to see individually rendered cells.
			//---		If we've since zoomed out, we cache the data as above, but otherwise ignore it.
			if( buffer.getSubCellCount() == 1 )
			{
				if ( buffer.isInBoundsAbsolute(m_utilCoord) )
				{
					smBufferCell cell = buffer.getCellAtAbsoluteCoord(m_utilCoord);
	
					if( isSync )
					{
						cell.onSyncResponseError();
					}
					else
					{
						cell.onGetResponseError(eCodeType);
					}
				}
			}
		}
	}
	
	private void onGetCellDataError(smTransactionRequest request, smTransactionResponse response)
	{
		if( response.getError() == smE_ResponseError.REDUNDANT )  return;
		
		int typeOrdinal = m_appContext.jsonFactory.getHelper().getInt(request.getJsonArgs(), smE_JsonKey.codeType);
		smE_CodeType eCodeType = smE_CodeType.values()[typeOrdinal];
		
		m_utilCoord.readJson(m_appContext.jsonFactory, request.getJsonArgs());

		if( eCodeType == smE_CodeType.SOURCE )
		{
			assertNotPosting(m_utilCoord);
		}

		cell_onServerError(m_utilCoord, eCodeType, /*isSyncOrPreview=*/false);
	}
	
	@Override
	public smE_ResponseErrorControl onResponseError(smTransactionRequest request, smTransactionResponse response)
	{
		if ( request.getPath() == smE_RequestPath.getCode )
		{
			this.onGetCellDataError(request, response);
			
			if( response.getError() == smE_ResponseError.VERSION_MISMATCH )
			{
				return smE_ResponseErrorControl.CONTINUE; // bubble up to StateMachine_BaseController
			}
			
			return smE_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == smE_RequestPath.syncCode )
		{
			//TODO: have to invalidate the html sent with this transaction...also have to account for multiple "set_data" calls coming back out of order
			//		e.g. transaction A gets sent out, transaction B gets sent out, transaction B succeeds, A fails...have to ignore A failing.
		
			if( isSyncing(m_utilCoord) )
			{
				return smE_ResponseErrorControl.BREAK;
			}

			smUserManager userManager = m_appContext.userMngr;
			smA_ClientUser user = userManager.getUser();
			
			m_utilCoord.readJson(m_appContext.jsonFactory, request.getJsonArgs());
			
			smCompilerResult result = new smCompilerResult();
			result.onFailure(smE_CompilationStatus.RESPONSE_ERROR);

			if( user.isCellOwner(m_utilCoord) )
			{
				user.onSyncResponseError(m_utilCoord);
			}
			
			cell_onServerError(m_utilCoord, null, /*isSyncOrPreview=*/true);
			
			if( response.getError() == smE_ResponseError.VERSION_MISMATCH || response.getError() == smE_ResponseError.NOT_AUTHENTICATED)
			{
				return smE_ResponseErrorControl.CONTINUE; // bubble up to StateMachine_BaseController
			}
			
			m_syncOrPreviewErrorDelegate.onCompilationFinished(result);
			
			return smE_ResponseErrorControl.BREAK;
		}
		
		return smE_ResponseErrorControl.CONTINUE;
	}
}
