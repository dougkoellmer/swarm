package swarm.client.managers;

import java.util.logging.Logger;

import swarm.client.app.AppContext;
import swarm.client.entities.A_ClientUser;
import swarm.client.entities.BufferCell;
import swarm.client.managers.CellCodeManager.I_SyncOrPreviewDelegate;
import swarm.client.transaction.ClientTransactionManager;
import swarm.client.transaction.E_ResponseErrorControl;
import swarm.client.transaction.E_ResponseSuccessControl;
import swarm.client.transaction.I_TransactionResponseHandler;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Grid;
import swarm.shared.json.MutableJsonQuery;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.CellSize;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class CellSizeManager implements I_TransactionResponseHandler
{
	private static final Logger s_logger = Logger.getLogger(CellSizeManager.class.getName());
	
	private final MutableJsonQuery m_jsonQuery = new MutableJsonQuery();
	
	private final AppContext m_appContext;
	private final CellSizeCache m_cache;
	private final CellSize m_utilCellSize = new CellSize();
	private final CellAddressMapping m_utilMapping = new CellAddressMapping();
	
	public CellSizeManager(AppContext appContext)
	{
		m_appContext = appContext;
		m_cache = new CellSizeCache(m_appContext.config.cellSizeCacheSize, m_appContext.config.cellSizeCacheExpiration_seconds, m_appContext.timeSource);
		
		m_jsonQuery.addCondition(null);
	}
	
	public void start()
	{
		ClientTransactionManager txnMngr = m_appContext.txnMngr;
		txnMngr.addHandler(this);
	}
	
	public void stop()
	{
		ClientTransactionManager txnMngr = m_appContext.txnMngr;
		txnMngr.removeHandler(this);
	}
	
	public void forceCache(CellAddressMapping mapping, CellSize cellSize_copied)
	{
		m_cache.put(mapping, cellSize_copied);
	}
	
	public void getCellSize(CellAddressMapping mapping, CellBuffer targetBuffer)
	{
		
	}
	
	public void getCellSizeFromServer(CellAddressMapping mapping)
	{
		m_jsonQuery.setCondition(0, mapping);
		
		ClientTransactionManager txnMngr = m_appContext.txnMngr;
		if( txnMngr.containsDispatchedRequest(E_RequestPath.getFocusedCellSize, m_jsonQuery) )
		{
			return;
		}
		
		txnMngr.queueRequest(E_RequestPath.getFocusedCellSize, mapping);
	}

	@Override
	public E_ResponseSuccessControl onResponseSuccess(TransactionRequest request, TransactionResponse response)
	{
		if( request.getPath() == E_RequestPath.getFocusedCellSize )
		{
			m_utilMapping.readJson(m_appContext.jsonFactory, request.getJsonArgs());
			m_utilCellSize.readJson(m_appContext.jsonFactory, response.getJsonArgs());
			
			
			A_Grid grid = m_appContext.gridMngr.getGrid();
			A_ClientUser user = m_appContext.userMngr.getUser();
			m_utilCellSize.setIfDefault(grid.getCellWidth(), grid.getCellHeight());
			
			if( user.isCellOwner(m_utilMapping) )
			{
				user.getCell(m_utilMapping).getFocusedCellSize().copy(m_utilCellSize);
			}
			else
			{
				//--- DRK > Should only use cache if we can't dump it in user.
				//---		On sign out, user will dump all data into cache to preserve it.
				m_cache.put(m_utilMapping, m_utilCellSize);
			}
			
			CellBufferManager.Iterator iterator = m_appContext.getRegisteredBufferMngrs();
			for( CellBufferManager manager = null; (manager = iterator.next()) != null; )
			{
				CellBuffer buffer = manager.getDisplayBuffer();
				
				//--- DRK > Cell data is only requested if we're close enough to see individually rendered cells.
				//---		If we've since zoomed out, we cache the data as above, but otherwise ignore it.
				if( buffer.getSubCellCount() == 1 )
				{
					if ( buffer.isInBoundsAbsolute(m_utilMapping.getCoordinate()) )
					{
						BufferCell cell = buffer.getCellAtAbsoluteCoord(m_utilMapping.getCoordinate());

						cell.getFocusedCellSize().copy(m_utilCellSize);
					}
				}
			}
			
			return E_ResponseSuccessControl.BREAK;
		}
		
		return E_ResponseSuccessControl.CONTINUE;
	}

	@Override
	public E_ResponseErrorControl onResponseError(TransactionRequest request, TransactionResponse response)
	{
		if( request.getPath() == E_RequestPath.getFocusedCellSize )
		{
			return E_ResponseErrorControl.BREAK;
		}
		
		return E_ResponseErrorControl.CONTINUE;
	}
}
