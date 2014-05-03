package swarm.shared.statemachine;

import java.util.ArrayList;

class P_StateObjectPool<T>
{
	public static interface I_Factory<T>
	{
		T newInstance();
	}
	
	protected final ArrayList<T> m_instances = new ArrayList<T>();
	
	private int m_logicalSize = 0;
	
	private final I_Factory<T> m_factory;
	
	public P_StateObjectPool(I_Factory<T> factory)
	{
		m_factory = factory;
	}
	
	public int getAllocCount()
	{
		return m_logicalSize;
	}
	
	public T checkOut()
	{
		int physicalSize = m_instances.size();
		
		while ( m_logicalSize >= physicalSize )
		{
			m_instances.add(null);
			
			physicalSize++;
		}
		
		if (m_instances.get(m_logicalSize) == null)
		{
			T newInstance = m_factory.newInstance();
			
			m_instances.set(m_logicalSize, newInstance);
		}
		
		T instance = m_instances.get(m_logicalSize);
		
		m_logicalSize++;
		
		return instance;
	}
	
	public void checkIn(T instance)
	{
		if ( m_logicalSize > 0 )
		{
			m_logicalSize--;
			
			m_instances.set(m_logicalSize, instance);
		}
	}
}
