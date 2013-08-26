package swarm.shared.structs;

import java.util.ArrayList;

public class smOptHashMap<T>
{
	private final ArrayList<T> m_values = new ArrayList<T>();
	
	public smOptHashMap()
	{
		
	}
	
	public boolean contains(int key)
	{
		return get(key) != null;
	}
	
	public T get(int key)
	{
		if( key < m_values.size() )
		{
			return m_values.get(key);
		}
		
		return null;
	}
	
	public void put(int key, T value)
	{
		while( key >= m_values.size() )
		{
			m_values.add(null);
		}
		
		m_values.set(key, value);
	}
}
