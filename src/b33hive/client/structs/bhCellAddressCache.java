package b33hive.client.structs;

import b33hive.client.entities.bhBufferCell;
import b33hive.shared.app.bhS_App;
import b33hive.shared.entities.bhA_Cell;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.memory.bhLRUCache;
import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhCellAddressMapping;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.time.bhI_TimeSource;

public class bhCellAddressCache
{
	private final bhLRUCache m_addressToMapping;
	private final bhLRUCache m_mappingToAddress;
	
	public bhCellAddressCache(int cacheSize, int cacheExpiration, bhI_TimeSource timeSource) 
	{
		//m_addressToMapping = new bhLRUCache(bhS_ClientApp.ADDRESS_CACHE_SIZE, bhS_ClientApp.ADDRESS_CACHE_EXPIRATION, bhClientApp.getInstance());
		//m_mappingToAddress = new bhLRUCache(bhS_ClientApp.ADDRESS_CACHE_SIZE, bhS_ClientApp.ADDRESS_CACHE_EXPIRATION, bhClientApp.getInstance());
		
		m_addressToMapping = new bhLRUCache(cacheSize, cacheExpiration, timeSource);
		m_mappingToAddress = new bhLRUCache(cacheSize, cacheExpiration, timeSource);
	}
	
	public bhCellAddressMapping get(bhCellAddress address)
	{
		return (bhCellAddressMapping) m_addressToMapping.get(address.getRawAddress());
	}
	
	public bhCellAddress get(bhCellAddressMapping mapping)
	{
		return (bhCellAddress) m_mappingToAddress.get(mapping.writeString());
	}
	
	public void put(bhCellAddress key, bhCellAddressMapping value)
	{
		m_addressToMapping.put(key.getRawAddress(), value);
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
		m_addressToMapping.remove(address.getRawAddress());
	}
}