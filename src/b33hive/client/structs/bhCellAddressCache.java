package com.b33hive.client.structs;

import com.b33hive.client.app.bhClientApp;
import com.b33hive.client.app.bhS_ClientApp;
import com.b33hive.client.entities.bhBufferCell;
import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.entities.bhA_Cell;
import com.b33hive.shared.entities.bhE_CodeType;
import com.b33hive.shared.memory.bhLRUCache;
import com.b33hive.shared.structs.bhCellAddress;
import com.b33hive.shared.structs.bhCellAddressMapping;
import com.b33hive.shared.structs.bhCode;
import com.b33hive.shared.structs.bhGridCoordinate;

public class bhCellAddressCache
{
	private final bhLRUCache m_addressToMapping;
	private final bhLRUCache m_mappingToAddress;
	
	public bhCellAddressCache() 
	{
		m_addressToMapping = new bhLRUCache(bhS_ClientApp.ADDRESS_CACHE_SIZE, bhS_ClientApp.ADDRESS_CACHE_EXPIRATION, bhClientApp.getInstance());
		m_mappingToAddress = new bhLRUCache(bhS_ClientApp.ADDRESS_CACHE_SIZE, bhS_ClientApp.ADDRESS_CACHE_EXPIRATION, bhClientApp.getInstance());
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