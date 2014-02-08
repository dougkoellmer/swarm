package swarm.client.managers;

import swarm.shared.memory.LRUCache;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.CellSize;
import swarm.shared.time.I_TimeSource;

public class CellSizeCache
{
	private final LRUCache m_cache;
	
	public CellSizeCache(int cacheSize, double cacheExpiration, I_TimeSource timeSource)
	{
		m_cache = new LRUCache(cacheSize, cacheExpiration, timeSource);
	}
	
	public void put(CellAddressMapping mapping_copied, CellSize value_copied)
	{
		m_cache.put(mapping_copied.writeString(), new CellSize(value_copied));
	}
	
	public boolean get(CellAddressMapping mapping_copied, CellSize value_out)
	{
		CellSize cellSize = (CellSize) m_cache.get(mapping_copied.writeString());
		
		if( cellSize != null )
		{
			value_out.copy(cellSize);
			
			return true;
		}
		else
		{
			return false;
		}
	}
}
