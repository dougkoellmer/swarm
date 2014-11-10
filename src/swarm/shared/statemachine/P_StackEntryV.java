package swarm.shared.statemachine;

import java.util.ArrayList;


/**
 * 
 * @author dougkoellmer
 */
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
	
	public int getHistoryIndex()
	{
		return m_historyIndex;
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
	
	Class<? extends A_State> get(StateFilter.Target target, int offset)
	{
		if( target == target.getScope().HISTORY )
		{
			int index = m_historyIndex + offset;
			
			if( index >= 0 && index < m_history.size() )
			{
				return m_history.get(index).m_stateClass;
			}
		}
		else
		{
			int index = offset > 0 ? offset : m_queue.size()-1-offset;
			
			if( index >= 0 && index < m_queue.size() )
			{
				return m_queue.get(index).m_stateClass;
			}
		}
		
		return null;
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
		
		cropHistory();
		
		return toReturn;
	}
	
	private void cropHistory()
	{
		while( m_history.size()-1 > m_historyIndex )
		{
			m_context.checkInStackEntryH(m_history.get(m_history.size()-1));
			m_history.remove(m_history.size()-1);
		}
	}
	
	void push(P_StackEntryH entry)
	{
		cropHistory();
		
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
	
	boolean remove(boolean justChecking, StateFilter.Target target, StateFilter.Match match_nullable, Class<? extends Object> stateClass_nullable, StateArgs args_nullable, Object ... argValues)
	{
		if( target == null )  return false;
		
		//--- DRK > Early out for the "remove all" case.
		if( match_nullable == null && stateClass_nullable == null && args_nullable == null )
		{
			if( target == target.getScope().QUEUE )
			{
				boolean hasQueue = m_queue.size() > 0;
				
				if( !justChecking )
				{
					clearQueue();
				}
				
				return hasQueue;
			}
			else if( target == target.getScope().HISTORY  )
			{
				if( justChecking && m_history.size() > 1 )
				{
					return true;
				}
			}
		}
		
		argValues = argValues == null ? DUMMY_ARGS : argValues;
		StateFilter.Scope scope = target.getScope();
		ArrayList<P_StackEntryH> list = target == scope.HISTORY ? m_history : m_queue;
		boolean isHistory = list == m_history;
		boolean isLimitOfOne = scope != A_BaseStateObject.ALL;
		int start, limit, inc, removalOffset;
		boolean removedSomething = false;
		
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
			else
			{
				if( argValues.length == 0 && args_nullable == null )
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
				if( !justChecking )
				{
					removedSomething = true;
					
					list.remove(i);
					
					if( isLimitOfOne )  return true;
					
					i += removalOffset;
					if( isHistory && i < m_historyIndex )
					{
						m_historyIndex--;
					}
				}
				else
				{
					return true;
				}
			}
		}
		
		return removedSomething;
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
