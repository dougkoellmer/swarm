package swarm.shared.memory;

import java.util.ArrayList;

import swarm.shared.debugging.smU_Debug;
import swarm.shared.reflection.smI_Class;

public class smObjectPool<T>
{
	private final ArrayList<T> m_instances = new ArrayList<T>();
	
	private int m_logicalSize = 0;
	
	private final smI_Class<T> m_class;
	
	public smObjectPool(smI_Class<T> type)
	{
		m_class = type;
	}
	
	public int getAllocCount()
	{
		return m_logicalSize;
	}
	
	public T allocate()
	{
		int physicalSize = m_instances.size();
		
		while ( m_logicalSize >= physicalSize )
		{
			m_instances.add(null);
			
			physicalSize++;
		}
		
		if (m_instances.get(m_logicalSize) == null)
		{
			T newInstance = m_class.newInstance();
			
			m_instances.set(m_logicalSize, newInstance);
		}
		
		T instance = m_instances.get(m_logicalSize);
		
		m_logicalSize++;
		
		return instance;
	}
	
	public void deallocate(T instance)
	{
		if ( m_logicalSize > 0 )
		{
			m_logicalSize--;
			
			m_instances.set(m_logicalSize, instance);
		}
		else
		{
			smU_Debug.ASSERT(false, "Tried to deallocate from pool at zero size - " + m_class);
		}
	}
}
