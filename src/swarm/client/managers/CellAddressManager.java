package swarm.client.managers;

import swarm.client.app.AppContext;
import swarm.client.entities.BufferCell;
import swarm.client.entities.A_ClientUser;
import swarm.client.structs.CellAddressCache;
import swarm.client.transaction.E_ResponseErrorControl;
import swarm.client.transaction.E_ResponseSuccessControl;
import swarm.client.transaction.E_TransactionAction;
import swarm.client.transaction.I_TransactionResponseHandler;
import swarm.client.transaction.ClientTransactionManager;
import swarm.shared.app.BaseAppContext;
import swarm.shared.json.MutableJsonQuery;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.E_CellAddressParseError;
import swarm.shared.structs.E_GetCellAddressError;
import swarm.shared.structs.E_GetCellAddressMappingError;
import swarm.shared.structs.GetCellAddressMappingResult;
import swarm.shared.structs.GetCellAddressResult;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.time.I_TimeSource;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;


public class CellAddressManager implements I_TransactionResponseHandler
{
	public static interface I_Listener
	{
		void onMappingFound(CellAddress address, CellAddressMapping mapping);
		
		void onMappingNotFound(CellAddress address);
		
		void onResponseError(CellAddress address);
		
		
		void onAddressFound(CellAddressMapping mapping, CellAddress address);
		
		void onAddressNotFound(CellAddressMapping mapping);
		
		void onResponseError(CellAddressMapping mapping);
	}
	
	private final CellAddressCache m_cache;
	
	private final MutableJsonQuery m_query = new MutableJsonQuery();
	
	private I_Listener m_listener = null;
	
	private final CellAddressMapping m_utilMapping = new CellAddressMapping();
	
	private final GetCellAddressMappingResult m_reusedMappingResult = new GetCellAddressMappingResult();
	private final GetCellAddressResult m_reusedAddressResult = new GetCellAddressResult();

	private final AppContext m_appContext;
	
	public CellAddressManager(AppContext context, int cacheSize, double cacheExpiration, I_TimeSource timeSource)
	{
		m_cache = new CellAddressCache(cacheSize, cacheExpiration, timeSource);

		m_appContext = context;
		
		m_query.addCondition(null);
	}
	
	public void start(I_Listener listener)
	{
		m_appContext.txnMngr.addHandler(this);
		
		m_listener = listener;
	}
	
	public void stop()
	{
		m_appContext.txnMngr.removeHandler(this);
		
		m_listener = null;
	}
	
	public boolean isWaitingOnResponse(CellAddressMapping mapping)
	{
		m_query.setCondition(0, mapping);
		
		return m_appContext.txnMngr.containsDispatchedRequest(E_RequestPath.getCellAddress, m_query);
	}
	
	public boolean isWaitingOnResponse(CellAddress address)
	{
		ClientTransactionManager txnMngr = m_appContext.txnMngr;
		m_query.setCondition(0, address);
		
		return txnMngr.containsDispatchedRequest(E_RequestPath.getCellAddressMapping, m_query);
	}
	
	public void getCellAddress(GridCoordinate coordinate, E_TransactionAction action)
	{
		m_utilMapping.getCoordinate().copy(coordinate);
		
		this.getCellAddress(m_utilMapping, action);
	}
	
	private void onAddressFound(CellAddressMapping mapping, CellAddress address, boolean updateBufferCell)
	{
		if( updateBufferCell )
		{
			this.setBufferCellAddress(address, mapping);
		}
		
		if( m_listener != null )  m_listener.onAddressFound(mapping, address);
	}
	
	public void getCellAddress(CellAddressMapping mapping, E_TransactionAction action)
	{
		//--- DRK > Try to get address from cache.
		CellAddress address = m_cache.get(mapping);
		if( address != null )
		{
			onAddressFound(mapping, address, true);
			
			return;
		}
		
		//--- DRK > Try to get address from user.
		UserManager userManager = m_appContext.userMngr;
		A_ClientUser user = userManager.getUser();
		address = user.getCellAddress(mapping);
		if( address != null )
		{
			onAddressFound(mapping, address, true);
			
			return;
		}
		
		//--- DRK > Try to get address from cell buffer.
		address = this.getAddressFromCellBuffer(mapping);
		if( address != null )
		{
			onAddressFound(mapping, address, false);
			
			return;
		}
		
		//--- DRK > If all else fails, we have to contact server.
		if( !isWaitingOnResponse(mapping) )
		{
			ClientTransactionManager txnMngr = m_appContext.txnMngr;
			txnMngr.performAction(action, E_RequestPath.getCellAddress, mapping);
		}
	}
	
	protected CellAddressMapping getMappingFromLocalSource(CellAddress address)
	{
		//--- DRK > Try to get mapping from cache.
		CellAddressMapping mapping = m_cache.get(address);
		if( mapping != null )
		{
			return mapping;
		}
		
		//--- DRK > Try to get mapping from user.
		UserManager userManager = m_appContext.userMngr;
		A_ClientUser user = userManager.getUser();
		mapping = user.getCellAddressMapping(address);
		if( mapping != null )
		{
			return mapping;
		}
		
		//--- DRK > Try to get mapping from cell buffer.
		mapping = getMappingFromCellBuffer(address);
		if( mapping != null )
		{
			return mapping;
		}
		
		return null;
	}
	
	public void getCellAddressMapping(CellAddress address, E_TransactionAction action)
	{
		E_CellAddressParseError parseError = address.getParseError();
		
		//--- DRK > Early out if address has a bad format.
		if( parseError != E_CellAddressParseError.NO_ERROR )
		{
			if( m_listener != null )  m_listener.onMappingNotFound(address);
			
			return;
		}
		
		CellAddressMapping mapping = this.getMappingFromLocalSource(address);
		if( mapping != null )
		{
			if( m_listener != null )  m_listener.onMappingFound(address, mapping);
		}
		
		//--- DRK > If all else fails we must contact server.
		if( !isWaitingOnResponse(address) )
		{
			ClientTransactionManager txnMngr = m_appContext.txnMngr;
			txnMngr.performAction(action, E_RequestPath.getCellAddressMapping, address);
		}
	}
	
	private CellAddressMapping getMappingFromCellBuffer(CellAddress address)
	{
		CellBuffer displayBuffer = m_appContext.cellBufferMngr.getDisplayBuffer();
		
		if( displayBuffer.getSubCellCount() == 1 )
		{
			for( int i = 0; i < displayBuffer.getCellCount(); i++ )
			{
				BufferCell cell = displayBuffer.getCellAtIndex(i);
				
				if( cell != null )
				{
					if( cell.getAddress() != null )
					{
						if( cell.getAddress().isEqualTo(address) )
						{
							return new CellAddressMapping(cell.getCoordinate());
						}
					}
				}
			}
		}
		
		return null;
	}
	
	private CellAddress getAddressFromCellBuffer(CellAddressMapping mapping)
	{
		CellBuffer displayBuffer = m_appContext.cellBufferMngr.getDisplayBuffer();
		if( displayBuffer.getSubCellCount() == 1 )
		{
			BufferCell cell = displayBuffer.getCellAtAbsoluteCoord(mapping.getCoordinate());
			
			if( cell != null )
			{
				cell.getAddress();
			}
		}
		
		return null;
	}
	
	private void setBufferCellAddress(CellAddress address, CellAddressMapping mapping)
	{
		CellBuffer displayBuffer = m_appContext.cellBufferMngr.getDisplayBuffer();
		
		if( displayBuffer.getSubCellCount() == 1 )
		{
			if( displayBuffer.isInBoundsAbsolute(mapping.getCoordinate()) )
			{
				BufferCell cell = displayBuffer.getCellAtAbsoluteCoord(mapping.getCoordinate());
				
				//--- DRK > At least one case where this is validly null...when user cell is created when user is created.
				//---		The user is populated, and calls into a method that eventually reaches here...but, the grid/buffer
				//---		hasn't had a chance to update yet and actually create the cell.
				if( cell != null )
				{
					cell.onAddressFound(address);
				}
			}
		}
	}

	@Override
	public E_ResponseSuccessControl onResponseSuccess(TransactionRequest request, TransactionResponse response)
	{
		if( request.getPath() == E_RequestPath.getCellAddress )
		{
			CellAddressMapping mapping = new CellAddressMapping();
			mapping.readJson(request.getJsonArgs(), this.m_appContext.jsonFactory);

			m_reusedAddressResult.readJson(response.getJsonArgs(), this.m_appContext.jsonFactory);
			
			if( m_reusedAddressResult.getError() == E_GetCellAddressError.NO_ERROR )
			{
				CellAddress address = m_reusedAddressResult.getAddress();
				
				m_cache.put(address, mapping);
				m_cache.put(mapping, address);

				onAddressFound(mapping, address, true);
			}
			else
			{
				if( m_listener != null )  m_listener.onAddressNotFound(mapping);
			}
		}
		else if( request.getPath() == E_RequestPath.getCellAddressMapping )
		{
			CellAddress address = new CellAddress();
			address.readJson(request.getJsonArgs(), this.m_appContext.jsonFactory);

			m_reusedMappingResult.readJson(response.getJsonArgs(), this.m_appContext.jsonFactory);
			
			if( m_reusedMappingResult.getError() == E_GetCellAddressMappingError.NO_ERROR )
			{
				CellAddressMapping mapping = m_reusedMappingResult.getMapping();
				
				m_cache.put(address,  mapping);
				m_cache.put(mapping, address);
				
				this.setBufferCellAddress(address, mapping);
				
				if( m_listener != null )  m_listener.onMappingFound(address, mapping);
			}
			else
			{
				if( m_listener != null )  m_listener.onMappingNotFound(address);
			}
		}
		
		return E_ResponseSuccessControl.CONTINUE;
	}

	@Override
	public E_ResponseErrorControl onResponseError(TransactionRequest request, TransactionResponse response)
	{
		if( request.getPath() == E_RequestPath.getCellAddress )
		{
			CellAddressMapping mapping = new CellAddressMapping();
			mapping.readJson(request.getJsonArgs(), this.m_appContext.jsonFactory);
			
			if( response.getError() == E_ResponseError.VERSION_MISMATCH )
			{
				if( m_listener != null )  m_listener.onResponseError(mapping);
				
				return E_ResponseErrorControl.CONTINUE;
			}
			else
			{
				//--- DRK > Since this transaction is of trivial importance, we don't make most response errors
				//---		seem all that bad, just that we couldn't find the address for whatever reason.
				//---		If there's a really critical problem with the server, other more important transactions
				//---		will blow up loudly anyway.
				if( m_listener != null )  m_listener.onAddressNotFound(mapping);
				
				return E_ResponseErrorControl.BREAK;
			}
		}
		else if( request.getPath() == E_RequestPath.getCellAddressMapping )
		{
			CellAddress address = new CellAddress();
			address.readJson(request.getJsonArgs(), this.m_appContext.jsonFactory);
			
			if( response.getError() == E_ResponseError.VERSION_MISMATCH )
			{
				if( m_listener != null )  m_listener.onResponseError(address);
				
				return E_ResponseErrorControl.CONTINUE;
			}
			else
			{
				//--- DRK > Since this transaction is of trivial importance, we don't make most response errors
				//---		seem all that bad, just that we couldn't find the mapping for whatever reason.
				//---		If there's a really critical problem with the server, other more important transactions
				//---		will blow up loudly anyway.
				if( m_listener != null )  m_listener.onMappingNotFound(address);
				
				return E_ResponseErrorControl.BREAK;
			}
		}
		
		return E_ResponseErrorControl.CONTINUE;
	}
}
