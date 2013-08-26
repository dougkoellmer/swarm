package swarm.client.structs;

import swarm.client.entities.smBufferCell;
import swarm.shared.app.smS_App;
import swarm.shared.entities.smA_Cell;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.memory.smLRUCache;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.time.smI_TimeSource;

public class smCellAddressCache
{
	private final smLRUCache m_addressToMapping;
	private final smLRUCache m_mappingToAddress;
	
	public smCellAddressCache(int cacheSize, double cacheExpiration, smI_TimeSource timeSource) 
	{
		//m_addressToMapping = new smLRUCache(smS_ClientApp.ADDRESS_CACHE_SIZE, smS_ClientApp.ADDRESS_CACHE_EXPIRATION, smClientApp.getInstance());
		//m_mappingToAddress = new smLRUCache(smS_ClientApp.ADDRESS_CACHE_SIZE, smS_ClientApp.ADDRESS_CACHE_EXPIRATION, smClientApp.getInstance());
		
		m_addressToMapping = new smLRUCache(cacheSize, cacheExpiration, timeSource);
		m_mappingToAddress = new smLRUCache(cacheSize, cacheExpiration, timeSource);
	}
	
	public smCellAddressMapping get(smCellAddress address)
	{
		return (smCellAddressMapping) m_addressToMapping.get(address.getRawAddressLeadSlash());
	}
	
	public smCellAddress get(smCellAddressMapping mapping)
	{
		return (smCellAddress) m_mappingToAddress.get(mapping.writeString());
	}
	
	public void put(smCellAddress key, smCellAddressMapping value)
	{
		m_addressToMapping.put(key.getRawAddressLeadSlash(), value);
	}
	
	public void put(smCellAddressMapping key, smCellAddress value)
	{
		m_mappingToAddress.put(key.writeString(), value);
	}
	
	public void clear(smCellAddressMapping mapping)
	{
		m_mappingToAddress.remove(mapping.writeString());
	}

	public void clear(smCellAddress address)
	{
		m_addressToMapping.remove(address.getRawAddressLeadSlash());
	}
}