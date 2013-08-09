package b33hive.client.managers;

import b33hive.client.app.bh_c;
import b33hive.client.entities.bhBufferCell;
import b33hive.client.entities.bhA_ClientUser;
import b33hive.client.structs.bhCellAddressCache;
import b33hive.client.transaction.bhE_ResponseErrorControl;
import b33hive.client.transaction.bhE_ResponseSuccessControl;
import b33hive.client.transaction.bhE_TransactionAction;
import b33hive.client.transaction.bhI_TransactionResponseHandler;
import b33hive.client.transaction.bhClientTransactionManager;
import b33hive.shared.app.bh;
import b33hive.shared.json.bhMutableJsonQuery;
import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhCellAddressMapping;
import b33hive.shared.structs.bhE_CellAddressParseError;
import b33hive.shared.structs.bhE_GetCellAddressError;
import b33hive.shared.structs.bhE_GetCellAddressMappingError;
import b33hive.shared.structs.bhGetCellAddressMappingResult;
import b33hive.shared.structs.bhGetCellAddressResult;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.time.bhI_TimeSource;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;


public class bhCellAddressManager implements bhI_TransactionResponseHandler
{
	public static interface I_Listener
	{
		void onMappingFound(bhCellAddress address, bhCellAddressMapping mapping);
		
		void onMappingNotFound(bhCellAddress address);
		
		void onResponseError(bhCellAddress address);
		
		
		void onAddressFound(bhCellAddressMapping mapping, bhCellAddress address);
		
		void onAddressNotFound(bhCellAddressMapping mapping);
		
		void onResponseError(bhCellAddressMapping mapping);
	}
	
	private final bhCellAddressCache m_cache;
	
	private final bhMutableJsonQuery m_query = new bhMutableJsonQuery();
	
	private I_Listener m_listener = null;
	
	private final bhCellAddressMapping m_utilMapping = new bhCellAddressMapping();
	
	private final bhGetCellAddressMappingResult m_reusedMappingResult = new bhGetCellAddressMappingResult();
	private final bhGetCellAddressResult m_reusedAddressResult = new bhGetCellAddressResult();
	
	public bhCellAddressManager(int cacheSize, double cacheExpiration, bhI_TimeSource timeSource)
	{
		m_cache = new bhCellAddressCache(cacheSize, cacheExpiration, timeSource);
		
		m_query.addCondition(null);
	}
	
	public void start(I_Listener listener)
	{
		bhClientTransactionManager txnMngr = bh_c.txnMngr;
		
		txnMngr.addHandler(this);
		
		m_listener = listener;
	}
	
	public void stop()
	{
		bhClientTransactionManager txnMngr = bh_c.txnMngr;
		txnMngr.removeHandler(this);
		
		m_listener = null;
	}
	
	public boolean isWaitingOnResponse(bhCellAddressMapping mapping)
	{
		bhClientTransactionManager txnMngr = bh_c.txnMngr;
		m_query.setCondition(0, mapping);
		
		return txnMngr.containsDispatchedRequest(bhE_RequestPath.getCellAddress, m_query);
	}
	
	public boolean isWaitingOnResponse(bhCellAddress address)
	{
		bhClientTransactionManager txnMngr = bh_c.txnMngr;
		m_query.setCondition(0, address);
		
		return txnMngr.containsDispatchedRequest(bhE_RequestPath.getCellAddressMapping, m_query);
	}
	
	public void getCellAddress(bhGridCoordinate coordinate, bhE_TransactionAction action)
	{
		m_utilMapping.getCoordinate().copy(coordinate);
		
		this.getCellAddress(m_utilMapping, action);
	}
	
	private void onAddressFound(bhCellAddressMapping mapping, bhCellAddress address, boolean updateBufferCell)
	{
		if( updateBufferCell )
		{
			this.setBufferCellAddress(address, mapping);
		}
		
		m_listener.onAddressFound(mapping, address);
	}
	
	public void getCellAddress(bhCellAddressMapping mapping, bhE_TransactionAction action)
	{
		//--- DRK > Try to get address from cache.
		bhCellAddress address = m_cache.get(mapping);
		if( address != null )
		{
			onAddressFound(mapping, address, true);
			
			return;
		}
		
		//--- DRK > Try to get address from user.
		bhUserManager userManager = bh_c.userMngr;
		bhA_ClientUser user = userManager.getUser();
		address = user.getCellAddress(mapping);
		if( address != null )
		{
			onAddressFound(mapping, address, true);
			
			return;
		}
		
		//--- DRK > Try to get address from cell buffer.
		Object addressUncast = this.getAddressOrMappingFromCellBuffer(mapping);
		if( addressUncast != null )
		{
			address = (bhCellAddress) addressUncast;

			onAddressFound(mapping, address, false);
			
			return;
		}
		
		//--- DRK > If all else fails, we have to contact server.
		if( !isWaitingOnResponse(mapping) )
		{
			bhClientTransactionManager txnMngr = bh_c.txnMngr;
			txnMngr.performAction(action, bhE_RequestPath.getCellAddress, mapping);
		}
	}
	
	public void getCellAddressMapping(bhCellAddress address, bhE_TransactionAction action)
	{
		bhE_CellAddressParseError parseError = address.getParseError();
		
		//--- DRK > Early out if address has a bad format.
		if( parseError != bhE_CellAddressParseError.NO_ERROR )
		{
			m_listener.onMappingNotFound(address);
			
			return;
		}
		
		//--- DRK > Try to get mapping from cache.
		bhCellAddressMapping mapping = m_cache.get(address);
		if( mapping != null )
		{
			m_listener.onMappingFound(address, mapping);
			
			return;
		}
		
		//--- DRK > Try to get mapping from user.
		bhUserManager userManager = bh_c.userMngr;
		bhA_ClientUser user = userManager.getUser();
		mapping = user.getCellAddressMapping(address);
		if( mapping != null )
		{
			m_listener.onMappingFound(address, mapping);
			
			return;
		}
		
		//--- DRK > Try to get mapping from cell buffer.
		Object mappingUncast = this.getAddressOrMappingFromCellBuffer(address);
		if( mappingUncast != null )
		{
			mapping = (bhCellAddressMapping) mappingUncast;
			
			m_listener.onMappingFound(address, mapping);
			
			return;
		}
		
		//--- DRK > If all else fails, we must contact server.
		if( !isWaitingOnResponse(address) )
		{
			bhClientTransactionManager txnMngr = bh_c.txnMngr;
			txnMngr.performAction(action, bhE_RequestPath.getCellAddressMapping, address);
		}
	}
	
	private Object getAddressOrMappingFromCellBuffer(Object mappingOrAddress)
	{
		bhCellAddress address = null;
		bhCellAddressMapping mapping = null;
		
		if( mappingOrAddress instanceof bhCellAddress )
		{
			address = (bhCellAddress) mappingOrAddress;
		}
		else
		{
			mapping = (bhCellAddressMapping) mappingOrAddress;
		}
		
		bhCellBuffer displayBuffer = bhCellBufferManager.getInstance().getDisplayBuffer();
		if( displayBuffer.getSubCellCount() == 1 )
		{
			for( int i = 0; i < displayBuffer.getCellCount(); i++ )
			{
				bhBufferCell cell = displayBuffer.getCellAtIndex(i);
				
				if( address != null )
				{
					if( cell.getCellAddress() != null )
					{
						if( cell.getCellAddress().isEqualTo(address) )
						{
							return new bhCellAddressMapping(cell.getCoordinate());
						}
					}
				}
				else
				{
					if( cell.getCoordinate().isEqualTo(mapping.getCoordinate()) )
					{
						return cell.getCellAddress();
					}
				}
			}
		}
		
		return null;
	}
	
	private void setBufferCellAddress(bhCellAddress address, bhCellAddressMapping mapping)
	{
		bhCellBuffer displayBuffer = bhCellBufferManager.getInstance().getDisplayBuffer();
		
		if( displayBuffer.getSubCellCount() == 1 )
		{
			if( displayBuffer.isInBoundsAbsolute(mapping.getCoordinate()) )
			{
				bhBufferCell cell = displayBuffer.getCellAtAbsoluteCoord(mapping.getCoordinate());
				
				cell.onAddressFound(address);
			}
		}
	}

	@Override
	public bhE_ResponseSuccessControl onResponseSuccess(bhTransactionRequest request, bhTransactionResponse response)
	{
		if( request.getPath() == bhE_RequestPath.getCellAddress )
		{
			bhCellAddressMapping mapping = new bhCellAddressMapping();
			mapping.readJson(request.getJson());

			m_reusedAddressResult.readJson(response.getJson());
			
			if( m_reusedAddressResult.getError() == bhE_GetCellAddressError.NO_ERROR )
			{
				bhCellAddress address = m_reusedAddressResult.getAddress();
				
				m_cache.put(address, mapping);
				m_cache.put(mapping, address);

				onAddressFound(mapping, address, true);
			}
			else
			{
				m_listener.onAddressNotFound(mapping);
			}
		}
		else if( request.getPath() == bhE_RequestPath.getCellAddressMapping )
		{
			bhCellAddress address = new bhCellAddress();
			address.readJson(request.getJson());

			m_reusedMappingResult.readJson(response.getJson());
			
			if( m_reusedMappingResult.getError() == bhE_GetCellAddressMappingError.NO_ERROR )
			{
				bhCellAddressMapping mapping = m_reusedMappingResult.getMapping();
				
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
		
		return bhE_ResponseSuccessControl.CONTINUE;
	}

	@Override
	public bhE_ResponseErrorControl onResponseError(bhTransactionRequest request, bhTransactionResponse response)
	{
		if( request.getPath() == bhE_RequestPath.getCellAddress )
		{
			bhCellAddressMapping mapping = new bhCellAddressMapping();
			mapping.readJson(request.getJson());
			
			if( response.getError() == bhE_ResponseError.VERSION_MISMATCH )
			{
				m_listener.onResponseError(mapping);
				
				return bhE_ResponseErrorControl.CONTINUE;
			}
			else
			{
				//--- DRK > Since this transaction is of trivial importance, we don't make most response errors
				//---		seem all that bad, just that we couldn't find the address for whatever reason.
				//---		If there's a really critical problem with the server, other more important transactions
				//---		will blow up loudly anyway.
				m_listener.onAddressNotFound(mapping);
				
				return bhE_ResponseErrorControl.BREAK;
			}
		}
		else if( request.getPath() == bhE_RequestPath.getCellAddressMapping )
		{
			bhCellAddress address = new bhCellAddress();
			address.readJson(request.getJson());
			
			if( response.getError() == bhE_ResponseError.VERSION_MISMATCH )
			{
				m_listener.onResponseError(address);
				
				return bhE_ResponseErrorControl.CONTINUE;
			}
			else
			{
				//--- DRK > Since this transaction is of trivial importance, we don't make most response errors
				//---		seem all that bad, just that we couldn't find the mapping for whatever reason.
				//---		If there's a really critical problem with the server, other more important transactions
				//---		will blow up loudly anyway.
				m_listener.onMappingNotFound(address);
				
				return bhE_ResponseErrorControl.BREAK;
			}
		}
		
		return bhE_ResponseErrorControl.CONTINUE;
	}
}
