package swarm.shared.statemachine;

import java.util.ArrayList;

import swarm.shared.statemachine.A_BaseStateObject.FilterMatch;
import swarm.shared.statemachine.A_BaseStateObject.FilterScope;
import swarm.shared.statemachine.A_BaseStateObject.FilterTarget;

class P_StackEntryV
{
	private static final Object[] DUMMY_ARGS = {};
	
	private final ArrayList<P_StackEntryH> m_queue = new ArrayList<P_StackEntryH>();
	private final ArrayList<P_StackEntryH> m_history = new ArrayList<P_StackEntryH>();
	
	private int m_historyIndex = 0;
	
	P_StackEntryV m_entryBeneath = null;
	private StateContext m_context;
	
	private boolean m_beingUsed = false;
	
	void init(StateContext context)
	{
		m_context = context;
		m_beingUsed = true;
	}
	
	void destruct()
	{
		clearQueue();
		clearHistory();
		m_entryBeneath = null;
		m_context = null;
		m_beingUsed = false;
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
		m_history.add(entry);
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
	
	void remove(FilterTarget target, FilterMatch match_nullable, Class<? extends Object> stateClass_nullable, StateArgs args_nullable, Object ... argValues)
	{
		//--- DRK > Early out for the "remove all queue" case.
		if( match_nullable == null && stateClass_nullable == null && args_nullable == null )
		{
			if( target == target.getScope().QUEUE )
			{
				clearQueue();
				
				return;
			}
		}
		
		argValues = argValues == null ? DUMMY_ARGS : argValues;
		FilterScope scope = target.getScope();
		ArrayList<P_StackEntryH> list = target == scope.HISTORY ? m_history : m_queue;
		boolean isHistory = list == m_history;
		boolean isLimitOfOne = scope != A_BaseStateObject.ALL;
		int start, limit, inc, removalOffset;
		
		if( scope == A_BaseStateObject.FIRST )
		{
			start = 0;
			inc = 1;
			limit = list.size();
			removalOffset = -1;
		}
		else
		{
			start = list.size()-1;
			inc = -1;
			limit = -1;
			removalOffset = 0;
		}
		
		for( int i = start; i != limit; i+=inc )
		{
			if( isHistory && i == m_historyIndex )  continue;
			
			P_StackEntryH ithEntry = list.get(i);
			StateArgs ithArgs = ithEntry.m_args;
			Class<? extends Object> ithClass = ithEntry.m_stateClass;
			boolean remove = false;
			
			if( stateClass_nullable != null )
			{
				//TODO: have instanceof-type functionality so user can pass in interfaces
				if( stateClass_nullable != ithClass )
				{
					continue;
				}
				else if( argValues.length == 0 && args_nullable == null )
				{
					remove = true;
				}
			}
			
			if( !remove )
			{
				if( match_nullable != null && ithArgs != null )
				{
					if( match_nullable == target.MATCHING )
					{
						remove = ithArgs.equals(argValues);
					}
					else if( match_nullable == target.WITH_ALL )
					{
						remove = ithArgs.containsAny(argValues);
					}
					else if( match_nullable == target.WITH_ANY )
					{
						remove = ithArgs.containsAll(argValues);
					}
				}
				else
				{
					remove = ithArgs.equals(args_nullable);
				}
			}
			
			if( remove )
			{
				list.remove(i);
				
				if( isLimitOfOne )  return;
				
				i += removalOffset;
				if( isHistory && i < m_historyIndex )
				{
					m_historyIndex--;
				}
			}
		}
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
