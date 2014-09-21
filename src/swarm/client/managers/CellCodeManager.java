package swarm.client.managers;

import java.util.HashSet;
import java.util.logging.Logger;

import swarm.client.app.AppContext;
import swarm.client.entities.BufferCell;
import swarm.client.entities.A_ClientUser;
import swarm.client.entities.E_CellNuke;
import swarm.client.entities.E_CodeStatus;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.structs.CellCodeCache;
import swarm.client.structs.I_LocalCodeRepository;
import swarm.client.transaction.E_ResponseErrorControl;
import swarm.client.transaction.E_ResponseSuccessControl;
import swarm.client.transaction.E_TransactionAction;
import swarm.client.transaction.I_TransactionResponseHandler;
import swarm.client.transaction.ClientTransactionManager;
import swarm.shared.app.BaseAppContext;
import swarm.shared.app.S_CommonApp;
import swarm.shared.code.CompilerResult;
import swarm.shared.code.E_CompilationStatus;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Cell;
import swarm.shared.entities.E_CodeSafetyLevel;
import swarm.shared.entities.E_CodeType;
import swarm.shared.statemachine.A_State;
import swarm.shared.structs.Code;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.I_WritesJson;
import swarm.shared.json.MutableJsonQuery;
import swarm.shared.json.JsonHelper;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.I_RequestPath;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;


import com.google.gwt.http.client.RequestBuilder;

/**
 * Encapsulates all cell code management, including going out to server.
 * 
 * @author Doug
 *
 */
public class CellCodeManager implements I_TransactionResponseHandler
{
	public static interface I_Listener
	{
		void onCompilationFinished(CompilerResult result);
	}
	
	private static final Logger s_logger = Logger.getLogger(CellCodeManager.class.getName());
	
	private final GridCoordinate m_utilCoord = new GridCoordinate();
	private final A_Cell m_utilCell = new A_Cell(){};
	
	I_Listener m_syncOrPreviewErrorDelegate = null;
	
	private final MutableJsonQuery m_postQuery = new MutableJsonQuery();
	private final MutableJsonQuery m_getQuery	 = new MutableJsonQuery();
	
	private final AppContext m_appContext;
	
	public CellCodeManager(AppContext appContext)
	{
		m_appContext = appContext;
		
		m_postQuery.addCondition(null);
		
		m_getQuery.addCondition(null);
		m_getQuery.addCondition(null);
	}
	
	public void start(I_Listener syncOrPreviewErrorDelegate)
	{
		ClientTransactionManager txnMngr = m_appContext.txnMngr;
		txnMngr.addHandler(this);
		
		m_syncOrPreviewErrorDelegate = syncOrPreviewErrorDelegate;
	}
	
	public void stop()
	{
		ClientTransactionManager txnMngr = m_appContext.txnMngr;
		txnMngr.removeHandler(this);
		
		m_syncOrPreviewErrorDelegate = null;
	}
	
	public void flush()
	{
		ClientTransactionManager txnMngr = m_appContext.txnMngr;
		//txnMngr.flushAsyncRequestQueue();
	}
	
	private void preparePostQuery(GridCoordinate coord)
	{
		m_postQuery.setCondition(0, coord);
	}
	
	private void prepareGetQuery(GridCoordinate coord, E_CodeType type)
	{
		m_getQuery.setCondition(0, coord);
		m_getQuery.setCondition(1, E_JsonKey.codeType, type.ordinal());
	}
	
	public void nukeFromOrbit(GridCoordinate coord, E_CellNuke nukeType)
	{
		if( nukeType == E_CellNuke.EVERYTHING )
		{
			m_appContext.codeCache.clear(coord);
		}
		
		//--- DRK > If not a user's cell, below we go about ensuring that the cell has all local code references cleared
		//---		by delving into the registered buffer managers and clearing the cell if it's visible.
		CellBufferManager.Iterator bufferManagerIterator = m_appContext.getRegisteredBufferMngrs();
		for( CellBufferManager manager = null; (manager = bufferManagerIterator.next()) != null; )
		{
			CellBuffer buffer = manager.getBaseDisplayBuffer();
			
			if( buffer.getSubCellCount() == 1 ) // NOTE: should always be the case for the snap buffer manager.
			{
				if( buffer.isInBoundsAbsolute(coord) )
				{
					BufferCell cell = buffer.getCellAtAbsoluteCoord(coord);
					
					if( cell != null )
					{
						cell.nuke(nukeType);
					}
				}
			}
		}
	}
	
	public void syncCell(BufferCell cell, Code sourceCode)
	{
		GridCoordinate coord = cell.getCoordinate();

		m_appContext.txnMngr.makeRequest(E_RequestPath.syncCode, sourceCode, coord);

		Code compiledCode = new Code(sourceCode.getRawCode(), E_CodeType.COMPILED);
		compiledCode.setSafetyLevel(E_CodeSafetyLevel.VIRTUAL_DYNAMIC_SANDBOX);
		cell.onSyncStart(sourceCode, compiledCode);
		
		UserManager userManager = m_appContext.userMngr;
		A_ClientUser user = userManager.getUser();
		user.onSyncStart(coord, compiledCode);
		
		cell.onServerRequest(E_CodeType.SPLASH);
	}
	
	public void previewCell(BufferCell cell, Code sourceCode)
	{
		//--- DRK > Old scheme required trip to the server to compile preview code.
		//---		With the new scheme, with code being sandboxed client-side, we
		//---		just route things directly back to the cell.
		//syncOrPreviewCell(cell, sourceCode, false);
		Code previewCode = new Code(sourceCode.getRawCode(), E_CodeType.COMPILED);
		previewCode.setSafetyLevel(E_CodeSafetyLevel.VIRTUAL_DYNAMIC_SANDBOX);
		cell.onPreviewSuccess(previewCode);
	}
	
	private boolean isSyncing(GridCoordinate coord)
	{
		preparePostQuery(coord);
		ClientTransactionManager txnMngr = m_appContext.txnMngr;
		
		return txnMngr.containsDispatchedRequest(E_RequestPath.syncCode, m_postQuery);
	}
	
	public void populateCell(BufferCell cell, I_LocalCodeRepository localHtmlSource, int cellSize, boolean recycled, boolean communicateWithServer, final E_CodeType eType)
	{
		ClientTransactionManager txnMngr = m_appContext.txnMngr;
		
		GridCoordinate absCoord = cell.getCoordinate();
		
		if ( recycled )
		{
			cell.onCellRecycled(cellSize);
		}
		
		E_CodeStatus status = cell.getStatus(eType);
	
		if( status == E_CodeStatus.NEEDS_CODE || status == E_CodeStatus.GET_ERROR )
		{
			if( cellSize == 1 )
			{
				if( eType == E_CodeType.SPLASH && isSyncing(absCoord) )
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
							U_Debug.ASSERT(eType != E_CodeType.SOURCE, "populateCell1");
							
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
						if( !txnMngr.containsDispatchedRequest(E_RequestPath.getCode, m_getQuery) )
						{							
							I_WritesJson codeTypeWrapper = new I_WritesJson()
							{
								@Override
								public void writeJson(I_JsonObject json_out, A_JsonFactory factory)
								{
									factory.getHelper().putInt(json_out, E_JsonKey.codeType, eType.ordinal());
								}
							};
							
							txnMngr.queueRequest(E_RequestPath.getCode, cell.getCoordinate(), codeTypeWrapper);
						}
					
						cell.onServerRequest(eType);
					}
				}
			}
			else
			{
				String url = "/img/cell_content/meta/"+cellSize+"/"+absCoord.writeString()+".png?v="+m_appContext.config.appVersion;
				Code code = new Code(url, E_CodeType.SPLASH);
				code.setSafetyLevel(E_CodeSafetyLevel.META_IMAGE);
				cell.setCode(E_CodeType.SPLASH, code);
			}
		}
	}
	
	private void setStaticHtml(String html, E_CodeType type, A_Cell cell)
	{
		Code code = new Code(html, E_CodeType.values());
		code.setSafetyLevel(E_CodeSafetyLevel.NO_SANDBOX_STATIC);
		cell.setCode(type, code);
	}
	
	private void onGetCellDataSuccess(TransactionRequest request, TransactionResponse response)
	{
		m_utilCell.readJson(response.getJsonArgs(), this.m_appContext.jsonFactory);
		m_utilCoord.readJson(request.getJsonArgs(), this.m_appContext.jsonFactory);
		m_utilCell.getCoordinate().copy(m_utilCoord);

		UserManager userManager = m_appContext.userMngr;
		A_ClientUser user = userManager.getUser();
		
		int typeOrdinal = m_appContext.jsonFactory.getHelper().getInt(request.getJsonArgs(), E_JsonKey.codeType);
		E_CodeType eCodeType = E_CodeType.values()[typeOrdinal];
		Code code = m_utilCell.getCode(eCodeType);
		
		if( code == null )
		{
			//TODO: Get this from somewhere else.
			final String OPEN_CELL_CODE = "<div style='width:100%; height:100%; background-color:#BBBBBB;'></div>";
			setStaticHtml(OPEN_CELL_CODE, eCodeType, m_utilCell);
		}
		
		boolean isCodeNull = code == null;
		
		if( eCodeType == E_CodeType.SOURCE )
		{
			if( isSyncing(m_utilCoord) )
			{
				U_Debug.ASSERT(false, "Shouldn't be getting source while posting.");
				
				return;
			}
		}
		
		if( user.isCellOwner(m_utilCoord) )
		{
			U_Debug.ASSERT(!isCodeNull, "onGetCellDataSuccess1");

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
		
		CellBufferManager.Iterator iterator = m_appContext.getRegisteredBufferMngrs();
		for( CellBufferManager manager = null; (manager = iterator.next()) != null; )
		{
			CellBuffer buffer = manager.getBaseDisplayBuffer();
			
			//--- DRK > Cell data is only requested if we're close enough to see individually rendered cells.
			//---		If we've since zoomed out, we cache the data as above, but otherwise ignore it.
			if( buffer.getSubCellCount() == 1 )
			{
				if ( buffer.isInBoundsAbsolute(m_utilCoord) )
				{
					BufferCell cell = buffer.getCellAtAbsoluteCoord(m_utilCoord);

					if( cell != null ) // Cell could conceivably be deleted or something while GET txn is out.
					{
						cell.copy(m_utilCell);
					}
					else
					{
						
					}
				}
			}
		}
	}
	
	public E_ResponseSuccessControl onResponseSuccess(TransactionRequest request, TransactionResponse response)
	{
		if ( request.getPath() == E_RequestPath.getCode )
		{
			this.onGetCellDataSuccess(request, response);

			return E_ResponseSuccessControl.BREAK;
		}
		else if( request.getPath() == E_RequestPath.syncCode )
		{
			UserManager userManager = m_appContext.userMngr;
			A_ClientUser user = userManager.getUser();
			m_utilCoord.readJson(request.getJsonArgs(), m_appContext.jsonFactory);
			
			if( isSyncing(m_utilCoord) )
			{
				return E_ResponseSuccessControl.BREAK;
			}
			
			//--- DRK > Presumably the user started out as the cell owner, but if they somehow managed to log out,
			//---		totally fringe or even impossible as that may be, then we shouldn't update cell code.
			boolean isStillCellOwner = user.isCellOwner(m_utilCoord);
			
			CompilerResult result = new CompilerResult(m_appContext.jsonFactory, response.getJsonArgs());
			Code sourceCode = new Code(m_appContext.jsonFactory, request.getJsonArgs(), E_CodeType.SOURCE);
			
			if( result.getStatus() == E_CompilationStatus.NO_ERROR )
			{
				Code splashScreenCode = result.getCode(E_CodeType.SPLASH);
				Code compiledCode = result.getCode(E_CodeType.COMPILED);
				
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
					m_appContext.codeCache.cacheCode(m_utilCoord, sourceCode, E_CodeType.SOURCE);
					m_appContext.codeCache.cacheCode(m_utilCoord, splashScreenCode, E_CodeType.SPLASH);
					m_appContext.codeCache.cacheCode(m_utilCoord, compiledCode, E_CodeType.COMPILED);
				}
				
				//--- DRK > I think technically we need only supply the compiled html to the main display buffer in this case.
				//---		This is unlike the "get" method, which SHOULD iterate through all registered buffers so
				//---		that there is zero reliance on the LRU cache.  In practice, the LRU cache again probably makes it
				//---		unnecessary to iterate through all buffers for those cases, but I like not having a reliance on it.
				CellBufferManager.Iterator bufferManagerIterator = m_appContext.getRegisteredBufferMngrs();
				for( CellBufferManager manager = null; (manager = bufferManagerIterator.next()) != null; )
				{
					CellBuffer buffer = manager.getBaseDisplayBuffer();

					//--- DRK > The user can technically commit their code and then navigate away while it's in transit to/from
					//---		the server...that's why we have to dig and see if the cell is even visible anymore.
					if( buffer.getSubCellCount() == 1 )
					{
						if ( buffer.isInBoundsAbsolute(m_utilCoord) )
						{
							BufferCell cell = buffer.getCellAtAbsoluteCoord(m_utilCoord);
							
							if( cell != null )
							{
								cell.onSyncResponseSuccess(splashScreenCode, compiledCode);
							}
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
			
			return E_ResponseSuccessControl.BREAK;
		}

		return E_ResponseSuccessControl.CONTINUE;
	}
	
	private void assertNotPosting(GridCoordinate coord)
	{
		preparePostQuery(m_utilCoord);
		ClientTransactionManager txnMngr = m_appContext.txnMngr;
		
		U_Debug.ASSERT(!txnMngr.containsDispatchedRequest(E_RequestPath.syncCode, m_postQuery), "assertNotPosting1");
	}
	
	private void cell_onServerError(GridCoordinate coordinate, E_CodeType eCodeType, boolean isSync)
	{
		CellBufferManager.Iterator bufferManagerIterator = m_appContext.getRegisteredBufferMngrs();
		for( CellBufferManager manager = null; (manager = bufferManagerIterator.next()) != null; )
		{
			CellBuffer buffer = manager.getBaseDisplayBuffer();
			
			//--- DRK > Cell data is only requested if we're close enough to see individually rendered cells.
			//---		If we've since zoomed out, we cache the data as above, but otherwise ignore it.
			if( buffer.getSubCellCount() == 1 )
			{
				if ( buffer.isInBoundsAbsolute(m_utilCoord) )
				{
					BufferCell cell = buffer.getCellAtAbsoluteCoord(m_utilCoord);
	
					if( cell != null )
					{
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
	}
	
	private void onGetCellDataError(TransactionRequest request, TransactionResponse response)
	{
		if( response.getError() == E_ResponseError.REDUNDANT )  return;
		
		int typeOrdinal = m_appContext.jsonFactory.getHelper().getInt(request.getJsonArgs(), E_JsonKey.codeType);
		E_CodeType eCodeType = E_CodeType.values()[typeOrdinal];
		
		m_utilCoord.readJson(request.getJsonArgs(), m_appContext.jsonFactory);

		if( eCodeType == E_CodeType.SOURCE )
		{
			assertNotPosting(m_utilCoord);
		}

		cell_onServerError(m_utilCoord, eCodeType, /*isSyncOrPreview=*/false);
	}
	
	@Override
	public E_ResponseErrorControl onResponseError(TransactionRequest request, TransactionResponse response)
	{
		if ( request.getPath() == E_RequestPath.getCode )
		{
			this.onGetCellDataError(request, response);
			
			if( response.getError() == E_ResponseError.VERSION_MISMATCH )
			{
				return E_ResponseErrorControl.CONTINUE; // bubble up to StateMachine_BaseController
			}
			
			return E_ResponseErrorControl.BREAK;
		}
		else if( request.getPath() == E_RequestPath.syncCode )
		{
			//TODO: have to invalidate the html sent with this transaction...also have to account for multiple "set_data" calls coming back out of order
			//		e.g. transaction A gets sent out, transaction B gets sent out, transaction B succeeds, A fails...have to ignore A failing.
		
			if( isSyncing(m_utilCoord) )
			{
				return E_ResponseErrorControl.BREAK;
			}

			UserManager userManager = m_appContext.userMngr;
			A_ClientUser user = userManager.getUser();
			
			m_utilCoord.readJson(request.getJsonArgs(), m_appContext.jsonFactory);
			
			CompilerResult result = new CompilerResult();
			result.onFailure(E_CompilationStatus.RESPONSE_ERROR);

			if( user.isCellOwner(m_utilCoord) )
			{
				user.onSyncResponseError(m_utilCoord);
			}
			
			cell_onServerError(m_utilCoord, null, /*isSyncOrPreview=*/true);
			
			if( response.getError() == E_ResponseError.VERSION_MISMATCH || response.getError() == E_ResponseError.NOT_AUTHENTICATED)
			{
				return E_ResponseErrorControl.CONTINUE; // bubble up to StateMachine_BaseController
			}
			
			m_syncOrPreviewErrorDelegate.onCompilationFinished(result);
			
			return E_ResponseErrorControl.BREAK;
		}
		
		return E_ResponseErrorControl.CONTINUE;
	}
}
