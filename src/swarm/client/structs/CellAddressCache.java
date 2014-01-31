package swarm.client.structs;

import swarm.client.entities.BufferCell;
import swarm.shared.app.S_CommonApp;
import swarm.shared.entities.A_Cell;
import swarm.shared.entities.E_CodeType;
import swarm.shared.memory.LRUCache;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.Code;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.time.I_TimeSource;

public class CellAddressCache
{
	private final LRUCache m_addressToMapping;
	private final LRUCache m_mappingToAddress;
	
	public CellAddressCache(int cacheSize, double cacheExpiration, I_TimeSource timeSource) 
	{
		//m_addressToMapping = new smLRUCache(smS_ClientApp.ADDRESS_CACHE_SIZE, smS_ClientApp.ADDRESS_CACHE_EXPIRATION, smClientApp.getInstance());
		//m_mappingToAddress = new smLRUCache(smS_ClientApp.ADDRESS_CACHE_SIZE, smS_ClientApp.ADDRESS_CACHE_EXPIRATION, smClientApp.getInstance());
		
		m_addressToMapping = new LRUCache(cacheSize, cacheExpiration, timeSource);
		m_mappingToAddress = new LRUCache(cacheSize, cacheExpiration, timeSource);
	}
	
	public CellAddressMapping get(CellAddress address)
	{
		return (CellAddressMapping) m_addressToMapping.get(address.getRawAddressLeadSlash());
	}
	
	public CellAddress get(CellAddressMapping mapping)
	{
		return (CellAddress) m_mappingToAddress.get(mapping.writeString());
	}
	
	public void put(CellAddress key, CellAddressMapping value)
	{
		m_addressToMapping.put(key.getRawAddressLeadSlash(), value);
	}
	
	public void put(CellAddressMapping key, CellAddress value)
	{
		m_mappingToAddress.put(key.writeString(), value);
	}
	
	public void clear(CellAddressMapping mapping)
	{
		m_mappingToAddress.remove(mapping.writeString());
	}

	public void clear(CellAddress address)
	{
		m_addressToMapping.remove(address.getRawAddressLeadSlash());
	}
}