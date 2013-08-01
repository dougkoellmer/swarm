package com.b33hive.shared.memory;

import java.util.ArrayList;

import com.b33hive.shared.debugging.bhU_Debug;
import com.b33hive.shared.reflection.bhI_Class;

public class bhObjectPool<T>
{
	private final ArrayList<T> m_instances = new ArrayList<T>();
	
	private int m_logicalSize = 0;
	
	private final bhI_Class<T> m_class;
	
	public bhObjectPool(bhI_Class<T> type)
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
			bhU_Debug.ASSERT(false, "Tried to deallocate from pool at zero size - " + m_class);
		}
	}
}
