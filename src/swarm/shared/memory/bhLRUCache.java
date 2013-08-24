package swarm.shared.memory;

import java.util.LinkedHashMap;
import java.util.Map;

import swarm.shared.time.bhI_TimeSource;

public class bhLRUCache
{
	private class Entry
	{
		private double m_entryTime = 0;
		private Object m_object = null;
		
		public Entry(Object object, double entryTime)
		{
			m_object = object;
			m_entryTime = entryTime;
		}
	};
	
	@SuppressWarnings("serial")
	private class Cache extends LinkedHashMap<String, Entry>
	{
		public Cache(int initialCapacity, float loadFactor, boolean accessOrder)
		{
			super(initialCapacity, loadFactor, accessOrder);
		}
		
		@Override
		public boolean removeEldestEntry(Map.Entry eldest)
		{
		    return size() > m_maxObjects;
		}
	}
	
	private double m_expirationTime = 0;
	private bhI_TimeSource m_timeSource = null;
	private int m_maxObjects = 0;
	
	private final Cache m_cache;
	
	public bhLRUCache(int maxObjects, double expirationTime, bhI_TimeSource timeSource)
	{
		m_maxObjects = maxObjects;
		m_expirationTime = expirationTime;
		m_timeSource = timeSource;
		
		m_cache = new Cache(m_maxObjects+1, .75F, true);
	}
	
	public void put(String key, Object object)
	{
		m_cache.put(key, new Entry(object, m_timeSource.getTime()));
	}
	
	public Object get(String key)
	{
		if( m_cache.containsKey(key) )
		{
			Entry entry = m_cache.get(key);
			
			if( (m_timeSource.getTime() - entry.m_entryTime) > m_expirationTime )
			{
				m_cache.remove(key);
				
				return null;
			}
			
			return entry.m_object;
		}
		else
		{
			return null;
		}
	}
	
	public void remove(String key)
	{
		m_cache.remove(key);
	}
}
