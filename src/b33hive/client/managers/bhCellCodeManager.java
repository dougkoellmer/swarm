package b33hive.client.managers;

import java.util.HashSet;
import java.util.logging.Logger;

import b33hive.client.app.bh_c;
import b33hive.client.entities.bhBufferCell;
import b33hive.client.entities.bhA_ClientUser;
import b33hive.client.entities.bhE_CellNuke;
import b33hive.client.entities.bhE_CodeStatus;
import b33hive.client.states.camera.State_ViewingCell;
import b33hive.client.structs.bhCellCodeCache;
import b33hive.client.structs.bhI_LocalCodeRepository;
import b33hive.client.transaction.bhE_ResponseErrorControl;
import b33hive.client.transaction.bhE_ResponseSuccessControl;
import b33hive.client.transaction.bhE_TransactionAction;
import b33hive.client.transaction.bhI_TransactionResponseHandler;
import b33hive.client.transaction.bhClientTransactionManager;
import b33hive.shared.app.bh;
import b33hive.shared.app.bhS_App;
import b33hive.shared.code.bhCompilerResult;
import b33hive.shared.code.bhE_CompilationStatus;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.entities.bhA_Cell;
import b33hive.shared.entities.bhE_CodeSafetyLevel;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.structs.bhPoint;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhMutableJsonQuery;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhI_RequestPath;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;


import com.google.gwt.http.client.RequestBuilder;

/**
 * Encapsulates all cell code management, including going out to server.
 * 
 * @author Doug
 *
 */
public class bhCellCodeManager implements bhI_TransactionResponseHandler
{
	public static interface I_SyncOrPreviewDelegate
	{
		void onCompilationFinished(bhCompilerResult result);
	}
	
	private static final Logger s_logger = Logger.getLogger(bhCellCodeManager.class.getName());
	
	private static final bhCellCodeManager s_instance = new bhCellCodeManager();
	
	private final bhGridCoordinate m_utilCoord = new bhGridCoordinate();
	private final bhA_Cell m_utilCell = new bhA_Cell(){};
	
	I_SyncOrPreviewDelegate m_syncOrPreviewErrorDelegate = null;
	
	private final bhMutableJsonQuery m_postQuery = new bhMutableJsonQuery();
	private final bhMutableJsonQuery m_getQuery	 = new bhMutableJsonQuery();
	
	private bhCellCodeManager()
	{
		m_postQuery.addCondition(null);
		
		m_getQuery.addCondition(null);
		m_getQuery.addCondition(null);
	}
	
	public static bhCellCodeManager getInstance()
	{
		return s_instance;
	}
	
	public void start(I_SyncOrPreviewDelegate syncOrPreviewErrorDelegate)
	{
		bhClientTransactionManager txnMngr = bh_c.txnMngr;
		txnMngr.addHandler(this);
		
		m_syncOrPreviewErrorDelegate = syncOrPreviewErrorDelegate;
	}
	
	public void stop()
	{
		bhClientTransactionManager txnMngr = bh_c.txnMngr;
		txnMngr.removeHandler(this);
		
		m_syncOrPreviewErrorDelegate = null;
	}
	
	public void flush()
	{
		bhClientTransactionManager txnMngr = bh_c.txnMngr;
		txnMngr.flushRequestQueue();
	}
	
	private void preparePostQuery(bhGridCoordinate coord)
	{
		m_postQuery.setCondition(0, coord);
	}
	
	private void prepareGetQuery(bhGridCoordinate coord, bhE_CodeType type)
	{
		m_getQuery.setCondition(0, coord);
		m_getQuery.setCondition(1, bhE_JsonKey.codeType, type.ordinal());
	}
	
	public void nukeFromOrbit(bhGridCoordinate coord, bhE_CellNuke nukeType)
	{
		if( nukeType == bhE_CellNuke.EVERYTHING )
		{
			bhCellCodeCache.getInstance().clear(coord);
		}
		
		//--- DRK > If not a user's cell, below we go about ensuring that the cell has all local code references cleared
		//---		by delving into the registered buffer managers and clearing the cell if it's visible.
		bhCellBufferManager.Iterator bufferManagerIterator = bhCellBufferManager.getRegisteredInstances();
		for( bhCellBufferManager manager = null; (manager = bufferManagerIterator.next()) != null; )
		{
			bhCellBuffer buffer = manager.getDisplayBuffer();
			
			if( buffer.getCellSize() == 1 ) // NOTE: should always be the case for the snap buffer manager.
			{
				if( buffer.isInBoundsAbsolute(coord) )
				{
					bhBufferCell cell = buffer.getCellAtAbsoluteCoord(coord);
					cell.nuke(nukeType);
				}
			}
		}
	}
	
	public void syncCell(bhBufferCell cell, bhCode sourceCode)
	{
		bhGridCoordinate coord = cell.getCoordinate();
		
		bhTransactionRequest request = new bhTransactionRequest(bhE_RequestPath.syncCode);
		sourceCode.writeJson(request.getJson());
		coord.writeJson(request.getJson());

		bh_c.txnMngr.makeRequest(request);

		bhCode compiledCode = new bhCode(sourceCode.getRawCode(), bhE_CodeType.COMPILED);
		compiledCode.setSafetyLevel(bhE_CodeSafetyLevel.REQUIRES_DYNAMIC_SANDBOX);
		cell.onSyncStart(sourceCode, compiledCode);
		
		bhUserManager userManager = bh_c.userMngr;
		bhA_ClientUser user = userManager.getUser();
		user.onSyncStart(coord, compiledCode);
		
		cell.onServerRequest(bhE_CodeType.SPLASH);
	}
	
	public void previewCell(bhBufferCell cell, bhCode sourceCode)
	{
		//--- DRK > Old scheme required trip to the server to compile preview code.
		//---		With the new scheme, with code being sandboxed client-side, we
		//---		just route things directly back to the cell.
		//syncOrPreviewCell(cell, sourceCode, false);
		bhCode previewCode = new bhCode(sourceCode.getRawCode(), bhE_CodeType.COMPILED);
		previewCode.setSafetyLevel(bhE_CodeSafetyLevel.REQUIRES_DYNAMIC_SANDBOX);
		cell.onPreviewSuccess(previewCode);
	}
	
	private boolean isSyncing(bhGridCoordinate coord)
	{
		preparePostQuery(coord);
		bhClientTransactionManager txnMngr = bh_c.txnMngr;
		
		return txnMngr.containsDispatchedRequest(bhE_RequestPath.syncCode, m_postQuery);
	}
	
	public void populateCell(bhBufferCell cell, bhI_LocalCodeRepository localHtmlSource, int cellSize, boolean recycled, boolean communicateWithServer, bhE_CodeType eType)
	{
		bhClientTransactionManager txnMngr = bh_c.txnMngr;
		
		bhGridCoordinate absCoord = cell.getCoordinate();
		
		if ( recycled )
		{
			cell.onCellRecycled(cellSize);
		}
		
		bhE_CodeStatus status = cell.getStatus(eType);
	
		if( status == bhE_CodeStatus.NEEDS_CODE || status == bhE_CodeStatus.GET_ERROR )
		{
			if( cellSize == 1 )
			{
				if( eType == bhE_CodeType.SPLASH && isSyncing(absCoord) )
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
							bhU_Debug.ASSERT(eType != bhE_CodeType.SOURCE, "populateCell1");
							
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
						if( !txnMngr.containsDispatchedRequest(bhE_RequestPath.getCode, m_getQuery) )
						{
							bhTransactionRequest request = new bhTransactionRequest(bhE_RequestPath.getCode);
							
							cell.getCoordinate().writeJson(request.getJson());
							bh.jsonFactory.getHelper().putInt(request.getJson(), bhE_JsonKey.codeType, eType.ordinal());
							
							txnMngr.queueRequest(request);
						}
					
						cell.onServerRequest(eType);
					}
				}
			}
			else
			{
				bhU_Debug.ASSERT(eType != bhE_CodeType.SOURCE, "populateCell2");
				
				// TODO: Need to put image url for generated meta image.
			}
		}
	}
	
	private void onGetCellDataSuccess(bhTransactionRequest request, bhTransactionResponse response)
	{
		m_utilCell.readJson(response.getJson());
		m_utilCoord.readJson(request.getJson());
		m_utilCell.getCoordinate().copy(m_utilCoord);

		bhUserManager userManager = bh_c.userMngr;
		bhA_ClientUser user = userManager.getUser();
		
		int typeOrdinal = bh.jsonFactory.getHelper().getInt(request.getJson(), bhE_JsonKey.codeType);
		bhE_CodeType eHtmlType = bhE_CodeType.values()[typeOrdinal];
		bhCode code = m_utilCell.getCode(eHtmlType);
		
		if( code == null )
		{
			//TODO: Get this from somewhere else.
			final String OPEN_CELL_CODE = "<div style='width:100%; height:100%; background-color:#BBBBBB;'></div>";
			
			code = new bhCode(OPEN_CELL_CODE, bhE_CodeType.values());
			code.setSafetyLevel(bhE_CodeSafetyLevel.SAFE);
			m_utilCell.setCode(eHtmlType, code);
		}
		
		boolean isCodeNull = code == null;
		
		if( eHtmlType == bhE_CodeType.SOURCE )
		{
			if( isSyncing(m_utilCoord) )
			{
				bhU_Debug.ASSERT(false, "Shouldn't be getting source while posting.");
				
				return;
			}
		}
		
		if( user.isCellOwner(m_utilCoord) )
		{
			bhU_Debug.ASSERT(!isCodeNull, "onGetCellDataSuccess1");

			if( !user.setInitialCellData(m_utilCoord, m_utilCell) )
			{
				return;
			}
		}
		else
		{
			//--- DRK > Should only use cache if we can't dump it in user.
			//---		On sign out, user will dump all data into cache to preserve it.
			bhCellCodeCache.getInstance().cacheCell(m_utilCell);
		}
		
		bhCellBufferManager.Iterator iterator = bhCellBufferManager.getRegisteredInstances();
		for( bhCellBufferManager manager = null; (manager = iterator.next()) != null; )
		{
			bhCellBuffer buffer = manager.getDisplayBuffer();
			
			//--- DRK > Cell data is only requested if we're close enough to see individually rendered cells.
			//---		If we've since zoomed out, we cache the data as above, but otherwise ignore it.
			if( buffer.getCellSize() == 1 )
			{
				if ( buffer.isInBoundsAbsolute(m_utilCoord) )
				{
					bhBufferCell cell = buffer.getCellAtAbsoluteCoord(m_utilCoord);

					cell.copy(m_utilCell);
				}
			}
		}
	}
	
	public bhE_ResponseSuccessControl onResponseSuccess(bhTransactionRequest request, bhTransactionResponse response)
	{
		if ( request.getPath() == bhE_RequestPath.getCode )
		{
			this.onGetCellDataSuccess(request, response);

			return bhE_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == bhE_RequestPath.syncCode )
		{
			bhUserManager userManager = bh_c.userMngr;
			bhA_ClientUser user = userManager.getUser();
			m_utilCoord.readJson(request.getJson());
			
			if( isSyncing(m_utilCoord) )
			{
				return bhE_ResponseSuccessControl.BREAK;
			}
			
			//--- DRK > Presumably the user started out as the cell owner, but if they somehow managed to log out,
			//---		totally fringe or even impossible as that may be, then we shouldn't update cell code.
			boolean isStillCellOwner = user.isCellOwner(m_utilCoord);
			
			bhCompilerResult result = new bhCompilerResult();
			result.readJson(response.getJson());
			
			bhCode sourceCode = new bhCode(request.getJson(), bhE_CodeType.SOURCE);
			
			if( result.getStatus() == bhE_CompilationStatus.NO_ERROR )
			{
				bhCode splashScreenCode = result.getCode(bhE_CodeType.SPLASH);
				bhCode compiledCode = result.getCode(bhE_CodeType.COMPILED);
				
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
					bhCellCodeCache cache = bhCellCodeCache.getInstance();
					cache.cacheCode(m_utilCoord, sourceCode, bhE_CodeType.SOURCE);
					cache.cacheCode(m_utilCoord, splashScreenCode, bhE_CodeType.SPLASH);
					cache.cacheCode(m_utilCoord, compiledCode, bhE_CodeType.COMPILED);
				}
				
				//--- DRK > I think technically we need only supply the compiled html to the main display buffer in this case.
				//---		This is unlike the "get" method, which SHOULD iterate through all registered buffers so
				//---		that there is zero reliance on the LRU cache.  In practice, the LRU cache again probably makes it
				//---		unnecessary to iterate through all buffers for those cases, but I like not having a reliance on it.
				bhCellBufferManager.Iterator bufferManagerIterator = bhCellBufferManager.getRegisteredInstances();
				for( bhCellBufferManager manager = null; (manager = bufferManagerIterator.next()) != null; )
				{
					bhCellBuffer buffer = manager.getDisplayBuffer();

					//--- DRK > The user can technically commit their code and then navigate away while it's in transit to/from
					//---		the server...that's why we have to dig and see if the cell is even visible anymore.
					if( buffer.getCellSize() == 1 )
					{
						if ( buffer.isInBoundsAbsolute(m_utilCoord) )
						{
							bhBufferCell cell = buffer.getCellAtAbsoluteCoord(m_utilCoord);
							
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
			
			return bhE_ResponseSuccessControl.BREAK;
		}

		return bhE_ResponseSuccessControl.CONTINUE;
	}
	
	private void assertNotPosting(bhGridCoordinate coord)
	{
		preparePostQuery(m_utilCoord);
		bhClientTransactionManager txnMngr = bh_c.txnMngr;
		
		bhU_Debug.ASSERT(!txnMngr.containsDispatchedRequest(bhE_RequestPath.syncCode, m_postQuery), "assertNotPosting1");
	}
	
	private void cell_onServerError(bhGridCoordinate coordinate, bhE_CodeType eCodeType, boolean isSync)
	{
		bhCellBufferManager.Iterator bufferManagerIterator = bhCellBufferManager.getRegisteredInstances();
		for( bhCellBufferManager manager = null; (manager = bufferManagerIterator.next()) != null; )
		{
			bhCellBuffer buffer = manager.getDisplayBuffer();
			
			//--- DRK > Cell data is only requested if we're close enough to see individually rendered cells.
			//---		If we've since zoomed out, we cache the data as above, but otherwise ignore it.
			if( buffer.getCellSize() == 1 )
			{
				if ( buffer.isInBoundsAbsolute(m_utilCoord) )
				{
					bhBufferCell cell = buffer.getCellAtAbsoluteCoord(m_utilCoord);
	
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
	
	private void onGetCellDataError(bhTransactionRequest request, bhTransactionResponse response)
	{
		if( response.getError() == bhE_ResponseError.REDUNDANT )  return;
		
		int typeOrdinal = bh.jsonFactory.getHelper().getInt(request.getJson(), bhE_JsonKey.codeType);
		bhE_CodeType eCodeType = bhE_CodeType.values()[typeOrdinal];
		
		m_utilCoord.readJson(request.getJson());

		if( eCodeType == bhE_CodeType.SOURCE )
		{
			assertNotPosting(m_utilCoord);
		}

		cell_onServerError(m_utilCoord, eCodeType, /*isSyncOrPreview=*/false);
	}
	
	@Override
	public bhE_ResponseErrorControl onResponseError(bhTransactionRequest request, bhTransactionResponse response)
	{
		if ( request.getPath() == bhE_RequestPath.getCode )
		{
			this.onGetCellDataError(request, response);
			
			if( response.getError() == bhE_ResponseError.VERSION_MISMATCH )
			{
				return bhE_ResponseErrorControl.CONTINUE; // bubble up to StateMachine_BaseController
			}
			
			return bhE_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == bhE_RequestPath.syncCode )
		{
			//TODO: have to invalidate the html sent with this transaction...also have to account for multiple "set_data" calls coming back out of order
			//		e.g. transaction A gets sent out, transaction B gets sent out, transaction B succeeds, A fails...have to ignore A failing.
		
			if( isSyncing(m_utilCoord) )
			{
				return bhE_ResponseErrorControl.BREAK;
			}

			bhUserManager userManager = bh_c.userMngr;
			bhA_ClientUser user = userManager.getUser();
			
			m_utilCoord.readJson(request.getJson());
			
			bhCompilerResult result = new bhCompilerResult();
			result.onFailure(bhE_CompilationStatus.RESPONSE_ERROR);

			if( user.isCellOwner(m_utilCoord) )
			{
				user.onSyncResponseError(m_utilCoord);
			}
			
			cell_onServerError(m_utilCoord, null, /*isSyncOrPreview=*/true);
			
			if( response.getError() == bhE_ResponseError.VERSION_MISMATCH || response.getError() == bhE_ResponseError.NOT_AUTHENTICATED)
			{
				return bhE_ResponseErrorControl.CONTINUE; // bubble up to StateMachine_BaseController
			}
			
			m_syncOrPreviewErrorDelegate.onCompilationFinished(result);
			
			return bhE_ResponseErrorControl.BREAK;
		}
		
		return bhE_ResponseErrorControl.CONTINUE;
	}
}
