package b33hive.shared.statemachine;

import java.util.ArrayList;
import java.util.HashMap;

import b33hive.shared.debugging.bhU_Debug;


/**
 * ...
 * @author 
 */
public abstract class bhA_StateContainer extends bhA_State
{
	private HashMap<Class<? extends bhA_State>, bhA_State> m_children = null;
	private HashMap<Class<? extends bhA_State>, Boolean> m_childrenForegrounded = null;
	
	protected bhA_StateContainer()
	{
	}
	
	private bhA_State getChildForManipulation(Class<? extends bhA_State> T)
	{
		bhU_Debug.ASSERT(this.isForegrounded());
		
		bhA_State state = m_children.get(T);
		
		if ( state == null )
		{
			bhU_Debug.ASSERT(false);
			return null;
		}
		
		if ( !state.isEntered() )
		{
			bhU_Debug.ASSERT(false);
			return null;
		}
		
		return state;
	}
	
	public bhA_State getState(Class<? extends bhA_State> T)
	{
		return m_children.get(T);
	}
	
	public boolean isStateForegrounded(Class<? extends bhA_State> T)
	{
		bhA_State state = this.getState(T);
		return state != null && state.isForegrounded();
	}
	
	public boolean isStateEntered(Class<? extends bhA_State> T)
	{
		return m_children.get(T) != null;
	}
	
	void internal_enterState(Class<? extends bhA_State> T, bhA_StateConstructor constructor)
	{
		if ( !this.isForegrounded() )
		{
			bhU_Debug.ASSERT(false);
			return;
		}
		
		if ( m_children.get(T) != null )
		{
			bhU_Debug.ASSERT(false);
			return;
		}
		
		bhA_State newState	= bhA_State.getInstance(T);
		m_children.put(T, newState);
		
		newState.m_parent = this;
		newState.m_root = this.m_root;
		newState.didEnter_internal(constructor);
		
		m_childrenForegrounded.put(T, false);
	}
	
	void internal_foregroundState(Class<? extends bhA_State> T)
	{
		if ( !this.isForegrounded() )
		{
			bhU_Debug.ASSERT(false);
			return;
		}
		
		bhA_State state = getChildForManipulation(T);
	
		if ( state.isForegrounded() )
		{
			bhU_Debug.ASSERT(false);
			return;
		}
		
		state.didForeground_internal(null, null);
		
		m_childrenForegrounded.put(T, true);
	}
	
	void internal_backgroundState(Class<? extends bhA_State> T)
	{
		if ( !this.isForegrounded() )
		{
			bhU_Debug.ASSERT(false);
			return;
		}
		
		bhA_State state = getChildForManipulation(T);
	
		if ( !state.isForegrounded() )
		{
			bhU_Debug.ASSERT(false);
			return;
		}
		
		state.willBackground_internal(null);
		
		m_childrenForegrounded.put(T, false);
	}
	
	void internal_exitState(Class<? extends bhA_State> T)
	{
		if ( !this.isForegrounded() )
		{
			bhU_Debug.ASSERT(false);
			return;
		}
		
		bhA_State state = getChildForManipulation(T);
		
		if ( state.isForegrounded() )
		{
			state.willBackground_internal(null);
		}
		
		state.willExit_internal();
		
		state.m_parent = null;
		state.m_root = null;
		
		m_children.remove(T);
		m_childrenForegrounded.remove(T);
	}
	
	@Override
	void didEnter_internal(bhA_StateConstructor constructor)
	{
		bhU_Debug.ASSERT(m_children == null);
		bhU_Debug.ASSERT(m_childrenForegrounded == null);
		
		super.didEnter_internal(constructor);
		
		m_children = new HashMap<Class<? extends bhA_State>, bhA_State>();
		m_childrenForegrounded = new HashMap<Class<? extends bhA_State>, Boolean>();
	}
	
	@Override
	void didForeground_internal(Class<? extends bhA_State> revealingState, Object[] args)
	{
		final ArrayList<bhA_State> existingStates = new ArrayList<bhA_State>();

		for ( Class<? extends bhA_State> T : m_children.keySet() )
		{
			bhA_State state = m_children.get(T);
			existingStates.add(state);
		}

		super.didForeground_internal(revealingState, args);

		for ( int i = 0; i < existingStates.size(); i++ )
		{
			bhA_State existingState = existingStates.get(i);

			Class<? extends bhA_State> T = existingState.getClass();

			if ( this.isStateEntered(T) )
			{
				if ( m_childrenForegrounded.get(T) == true )
				{
					// DRK > NOTE: Might not want to forward the args here...don't know yet.
					existingState.didForeground_internal(revealingState, args);
				}
			}
		}
	}
	
	@Override
	void update_internal(double timeStep)
	{
		super.update_internal(timeStep);
		
		for ( Class<? extends bhA_State> T : m_children.keySet() )
		{
			bhA_State state = m_children.get(T);
			state.update_internal(timeStep);
		}
	}
	
	@Override
	void willBackground_internal(Class<? extends bhA_State> blockingState)
	{
		super.willBackground_internal(blockingState);
		
		for ( Class<? extends bhA_State> T : m_children.keySet() )
		{
			bhA_State state = m_children.get(T);
			if ( state.isForegrounded() )
			{
				state.willBackground_internal(blockingState);
			}
		}
	}
	
	@Override
	void willExit_internal()
	{
		super.willExit_internal();
		
		HashMap<Class<? extends bhA_State>, bhA_State> children = m_children;
	
		m_children = null;
		m_childrenForegrounded = null;
		
		for ( Class<? extends bhA_State> T : children.keySet() )
		{
			bhA_State state = children.get(T);
			state.willExit_internal();
		}
	}
}