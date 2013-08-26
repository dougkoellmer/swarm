package swarm.client.managers;

import swarm.client.app.sm_c;
import swarm.client.entities.smBufferCell;
import swarm.client.entities.smA_ClientUser;
import swarm.client.structs.smCellAddressCache;
import swarm.client.transaction.smE_ResponseErrorControl;
import swarm.client.transaction.smE_ResponseSuccessControl;
import swarm.client.transaction.smE_TransactionAction;
import swarm.client.transaction.smI_TransactionResponseHandler;
import swarm.client.transaction.smClientTransactionManager;
import swarm.shared.app.sm;
import swarm.shared.json.smMutableJsonQuery;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.structs.smE_CellAddressParseError;
import swarm.shared.structs.smE_GetCellAddressError;
import swarm.shared.structs.smE_GetCellAddressMappingError;
import swarm.shared.structs.smGetCellAddressMappingResult;
import swarm.shared.structs.smGetCellAddressResult;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.time.smI_TimeSource;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;


public class smCellAddressManager implements smI_TransactionResponseHandler
{
	public static interface I_Listener
	{
		void onMappingFound(smCellAddress address, smCellAddressMapping mapping);
		
		void onMappingNotFound(smCellAddress address);
		
		void onResponseError(smCellAddress address);
		
		
		void onAddressFound(smCellAddressMapping mapping, smCellAddress address);
		
		void onAddressNotFound(smCellAddressMapping mapping);
		
		void onResponseError(smCellAddressMapping mapping);
	}
	
	private final smCellAddressCache m_cache;
	
	private final smMutableJsonQuery m_query = new smMutableJsonQuery();
	
	private I_Listener m_listener = null;
	
	private final smCellAddressMapping m_utilMapping = new smCellAddressMapping();
	
	private final smGetCellAddressMappingResult m_reusedMappingResult = new smGetCellAddressMappingResult();
	private final smGetCellAddressResult m_reusedAddressResult = new smGetCellAddressResult();
	
	public smCellAddressManager(int cacheSize, double cacheExpiration, smI_TimeSource timeSource)
	{
		m_cache = new smCellAddressCache(cacheSize, cacheExpiration, timeSource);
		
		m_query.addCondition(null);
	}
	
	public void start(I_Listener listener)
	{
		smClientTransactionManager txnMngr = sm_c.txnMngr;
		
		txnMngr.addHandler(this);
		
		m_listener = listener;
	}
	
	public void stop()
	{
		smClientTransactionManager txnMngr = sm_c.txnMngr;
		txnMngr.removeHandler(this);
		
		m_listener = null;
	}
	
	public boolean isWaitingOnResponse(smCellAddressMapping mapping)
	{
		smClientTransactionManager txnMngr = sm_c.txnMngr;
		m_query.setCondition(0, mapping);
		
		return txnMngr.containsDispatchedRequest(smE_RequestPath.getCellAddress, m_query);
	}
	
	public boolean isWaitingOnResponse(smCellAddress address)
	{
		smClientTransactionManager txnMngr = sm_c.txnMngr;
		m_query.setCondition(0, address);
		
		return txnMngr.containsDispatchedRequest(smE_RequestPath.getCellAddressMapping, m_query);
	}
	
	public void getCellAddress(smGridCoordinate coordinate, smE_TransactionAction action)
	{
		m_utilMapping.getCoordinate().copy(coordinate);
		
		this.getCellAddress(m_utilMapping, action);
	}
	
	private void onAddressFound(smCellAddressMapping mapping, smCellAddress address, boolean updateBufferCell)
	{
		if( updateBufferCell )
		{
			this.setBufferCellAddress(address, mapping);
		}
		
		m_listener.onAddressFound(mapping, address);
	}
	
	public void getCellAddress(smCellAddressMapping mapping, smE_TransactionAction action)
	{
		//--- DRK > Try to get address from cache.
		smCellAddress address = m_cache.get(mapping);
		if( address != null )
		{
			onAddressFound(mapping, address, true);
			
			return;
		}
		
		//--- DRK > Try to get address from user.
		smUserManager userManager = sm_c.userMngr;
		smA_ClientUser user = userManager.getUser();
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
			smClientTransactionManager txnMngr = sm_c.txnMngr;
			txnMngr.performAction(action, smE_RequestPath.getCellAddress, mapping);
		}
	}
	
	public void getCellAddressMapping(smCellAddress address, smE_TransactionAction action)
	{
		smE_CellAddressParseError parseError = address.getParseError();
		
		//--- DRK > Early out if address has a bad format.
		if( parseError != smE_CellAddressParseError.NO_ERROR )
		{
			m_listener.onMappingNotFound(address);
			
			return;
		}
		
		//--- DRK > Try to get mapping from cache.
		smCellAddressMapping mapping = m_cache.get(address);
		if( mapping != null )
		{
			m_listener.onMappingFound(address, mapping);
			
			return;
		}
		
		//--- DRK > Try to get mapping from user.
		smUserManager userManager = sm_c.userMngr;
		smA_ClientUser user = userManager.getUser();
		mapping = user.getCellAddressMapping(address);
		if( mapping != null )
		{
			m_listener.onMappingFound(address, mapping);
			
			return;
		}
		
		//--- DRK > Try to get mapping from cell buffer.
		mapping = getMappingFromCellBuffer(address);
		if( mapping != null )
		{
			m_listener.onMappingFound(address, mapping);
			
			return;
		}
		
		//--- DRK > If all else fails, we must contact server.
		if( !isWaitingOnResponse(address) )
		{
			smClientTransactionManager txnMngr = sm_c.txnMngr;
			txnMngr.performAction(action, smE_RequestPath.getCellAddressMapping, address);
		}
	}
	
	//private smCellAddress
	
	private smCellAddressMapping getMappingFromCellBuffer(smCellAddress address)
	{
		smCellBuffer displayBuffer = smCellBufferManager.getInstance().getDisplayBuffer();
		
		if( displayBuffer.getSubCellCount() == 1 )
		{
			for( int i = 0; i < displayBuffer.getCellCount(); i++ )
			{
				smBufferCell cell = displayBuffer.getCellAtIndex(i);
				
				if( address != null )
				{
					if( cell.getCellAddress() != null )
					{
						if( cell.getCellAddress().isEqualTo(address) )
						{
							return new smCellAddressMapping(cell.getCoordinate());
						}
					}
				}
			}
		}
		
		return null;
	}
	
	private smCellAddress getAddressFromCellBuffer(smCellAddressMapping mapping)
	{
		smCellBuffer displayBuffer = smCellBufferManager.getInstance().getDisplayBuffer();
		if( displayBuffer.getSubCellCount() == 1 )
		{
			smBufferCell cell = displayBuffer.getCellAtAbsoluteCoord(mapping.getCoordinate());
			
			if( cell != null )
			{
				cell.getCellAddress();
			}
		}
		
		return null;
	}
	
	private void setBufferCellAddress(smCellAddress address, smCellAddressMapping mapping)
	{
		smCellBuffer displayBuffer = smCellBufferManager.getInstance().getDisplayBuffer();
		
		if( displayBuffer.getSubCellCount() == 1 )
		{
			if( displayBuffer.isInBoundsAbsolute(mapping.getCoordinate()) )
			{
				smBufferCell cell = displayBuffer.getCellAtAbsoluteCoord(mapping.getCoordinate());
				
				cell.onAddressFound(address);
			}
		}
	}

	@Override
	public smE_ResponseSuccessControl onResponseSuccess(smTransactionRequest request, smTransactionResponse response)
	{
		if( request.getPath() == smE_RequestPath.getCellAddress )
		{
			smCellAddressMapping mapping = new smCellAddressMapping();
			mapping.readJson(request.getJson());

			m_reusedAddressResult.readJson(response.getJson());
			
			if( m_reusedAddressResult.getError() == smE_GetCellAddressError.NO_ERROR )
			{
				smCellAddress address = m_reusedAddressResult.getAddress();
				
				m_cache.put(address, mapping);
				m_cache.put(mapping, address);

				onAddressFound(mapping, address, true);
			}
			else
			{
				m_listener.onAddressNotFound(mapping);
			}
		}
		else if( request.getPath() == smE_RequestPath.getCellAddressMapping )
		{
			smCellAddress address = new smCellAddress();
			address.readJson(request.getJson());

			m_reusedMappingResult.readJson(response.getJson());
			
			if( m_reusedMappingResult.getError() == smE_GetCellAddressMappingError.NO_ERROR )
			{
				smCellAddressMapping mapping = m_reusedMappingResult.getMapping();
				
				m_cache.put(address,  mapping);
				m_cache.put(mapping, address);
				
				this.setBufferCellAddress(address, mapping);
				
				m_listener.onMappingFound(address, mapping);
			}
			else
			{
				m_listener.onMappingNotFound(address);
			}
		}
		
		return smE_ResponseSuccessControl.CONTINUE;
	}

	@Override
	public smE_ResponseErrorControl onResponseError(smTransactionRequest request, smTransactionResponse response)
	{
		if( request.getPath() == smE_RequestPath.getCellAddress )
		{
			smCellAddressMapping mapping = new smCellAddressMapping();
			mapping.readJson(request.getJson());
			
			if( response.getError() == smE_ResponseError.VERSION_MISMATCH )
			{
				m_listener.onResponseError(mapping);
				
				return smE_ResponseErrorControl.CONTINUE;
			}
			else
			{
				//--- DRK > Since this transaction is of trivial importance, we don't make most response errors
				//---		seem all that bad, just that we couldn't find the address for whatever reason.
				//---		If there's a really critical problem with the server, other more important transactions
				//---		will blow up loudly anyway.
				m_listener.onAddressNotFound(mapping);
				
				return smE_ResponseErrorControl.BREAK;
			}
		}
		else if( request.getPath() == smE_RequestPath.getCellAddressMapping )
		{
			smCellAddress address = new smCellAddress();
			address.readJson(request.getJson());
			
			if( response.getError() == smE_ResponseError.VERSION_MISMATCH )
			{
				m_listener.onResponseError(address);
				
				return smE_ResponseErrorControl.CONTINUE;
			}
			else
			{
				//--- DRK > Since this transaction is of trivial importance, we don't make most response errors
				//---		seem all that bad, just that we couldn't find the mapping for whatever reason.
				//---		If there's a really critical problem with the server, other more important transactions
				//---		will blow up loudly anyway.
				m_listener.onMappingNotFound(address);
				
				return smE_ResponseErrorControl.BREAK;
			}
		}
		
		return smE_ResponseErrorControl.CONTINUE;
	}
}
