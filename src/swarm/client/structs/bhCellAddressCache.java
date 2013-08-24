package swarm.client.structs;

import swarm.client.entities.bhBufferCell;
import swarm.shared.app.bhS_App;
import swarm.shared.entities.bhA_Cell;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.memory.bhLRUCache;
import swarm.shared.structs.bhCellAddress;
import swarm.shared.structs.bhCellAddressMapping;
import swarm.shared.structs.bhCode;
import swarm.shared.structs.bhGridCoordinate;
import swarm.shared.time.bhI_TimeSource;

public class bhCellAddressCache
{
	private final bhLRUCache m_addressToMapping;
	private final bhLRUCache m_mappingToAddress;
	
	public bhCellAddressCache(int cacheSize, double cacheExpiration, bhI_TimeSource timeSource) 
	{
		//m_addressToMapping = new bhLRUCache(bhS_ClientApp.ADDRESS_CACHE_SIZE, bhS_ClientApp.ADDRESS_CACHE_EXPIRATION, bhClientApp.getInstance());
		//m_mappingToAddress = new bhLRUCache(bhS_ClientApp.ADDRESS_CACHE_SIZE, bhS_ClientApp.ADDRESS_CACHE_EXPIRATION, bhClientApp.getInstance());
		
		m_addressToMapping = new bhLRUCache(cacheSize, cacheExpiration, timeSource);
		m_mappingToAddress = new bhLRUCache(cacheSize, cacheExpiration, timeSource);
	}
	
	public bhCellAddressMapping get(bhCellAddress address)
	{
		return (bhCellAddressMapping) m_addressToMapping.get(address.getRawAddressLeadSlash());
	}
	
	public bhCellAddress get(bhCellAddressMapping mapping)
	{
		return (bhCellAddress) m_mappingToAddress.get(mapping.writeString());
	}
	
	public void put(bhCellAddress key, bhCellAddressMapping value)
	{
		m_addressToMapping.put(key.getRawAddressLeadSlash(), value);
	}
	
	public void put(bhCellAddressMapping key, bhCellAddress value)
	{
		m_mappingToAddress.put(key.writeString(), value);
	}
	
	public void clear(bhCellAddressMapping mapping)
	{
		m_mappingToAddress.remove(mapping.writeString());
	}

	public void clear(bhCellAddress address)
	{
		m_addressToMapping.remove(address.getRawAddressLeadSlash());
	}
}