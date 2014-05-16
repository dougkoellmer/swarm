package swarm.shared.statemachine;

import java.util.ArrayList;

class P_StackEntryV
{
	private final ArrayList<P_StackEntryH> m_queue = new ArrayList<P_StackEntryH>();
	private final ArrayList<P_StackEntryH> m_history = new ArrayList<P_StackEntryH>();
	
	private int m_historyIndex = 0;
	
	P_StackEntryV m_entryBeneath = null;
	private StateContext m_context;
	
	void init(StateContext context)
	{
		m_context = context;
	}
	
	void clean()
	{
		clearQueue();
		clearHistory();
		m_entryBeneath = null;
		m_context = null;
	}
	
	P_StackEntryV getEntryBeneath()
	{
		return m_entryBeneath;
	}
	
	void clearHistory()
	{
		for( int i = 0; i < m_history.size(); i++ )
		{
			m_context.checkInStackEntryH(m_history.get(i));
		}
		
		m_history.clear();
		m_historyIndex = 0;
	}
	
	private void clearQueue()
	{
		for( int i = 0; i < m_queue.size(); i++ )
		{
			m_context.checkInStackEntryH(m_queue.get(i));
		}
		
		m_queue.clear();
	}
	
	P_StackEntryH go(int offset)
	{
		if( offset == 0 )  return null;
		
		int target = m_historyIndex + offset;
		
		if( target >= 0 && target < m_history.size() )
		{
			clearQueue();
			
			m_historyIndex = target;
			P_StackEntryH toReturn = m_history.get(m_historyIndex);
			
			return toReturn;
		}
		else
		{
			return null;
		}
	}

	P_StackEntryH pop()
	{
		if( m_historyIndex == 0 )  return null;
		
		m_historyIndex--;
		P_StackEntryH toReturn = m_history.get(m_historyIndex);
		
		while( m_history.size()-1 > m_historyIndex )
		{
			m_context.checkInStackEntryH(m_history.get(m_history.size()-1));
			m_history.remove(m_history.size()-1);
		}
		
		return toReturn;
	}
	
	void push(P_StackEntryH entry)
	{
		m_queue.add(entry);
		m_historyIndex++;
	}
	
	void set(Class<? extends A_State> stateClass, StateArgs args)
	{
		//--- DRK > Should just go through this loop once for first state setting.
		//---		Just being pedantic making it a loop.
		while( m_history.size()-1 < m_historyIndex )
		{
			m_history.add(null);
		}
		
		P_StackEntryH existingEntry = m_history.get(m_historyIndex);
		existingEntry = existingEntry != null ? existingEntry : m_context.checkOutStackEntryH();
		existingEntry.init(stateClass, args);
		
		m_history.set(m_historyIndex, existingEntry);
	}
	
	boolean removeFromQueue(Class<? extends A_State> stateClass)
	{
		boolean foundSomething = false;
		
		for( int i = 0; i < m_queue.size(); i++ )
		{
			P_StackEntryH ithEntry = m_queue.get(i);
			
			if( ithEntry.m_stateClass == stateClass )
			{
				m_queue.remove(i);
				foundSomething = true;
			}
		}
		
		return foundSomething;
	}
	
	void queue(P_StackEntryH entry)
	{
		m_queue.add(entry);
	}
	
	P_StackEntryH dequeue()
	{
		if( m_queue.size() == 0 )  return null;
		
		P_StackEntryH entry = m_queue.get(0);
		
		m_queue.remove(0);
		
		return entry;
	}
}
