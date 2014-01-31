package swarm.shared.statemachine;

import java.util.ArrayList;
import java.util.HashMap;

import swarm.shared.debugging.U_Debug;


/**
 * ...
 * @author 
 */
public abstract class A_StateContainer extends A_State
{
	private HashMap<Class<? extends A_State>, A_State> m_children = null;
	private HashMap<Class<? extends A_State>, Boolean> m_childrenForegrounded = null;
	
	protected A_StateContainer()
	{
	}
	
	private A_State getChildForManipulation(Class<? extends A_State> T)
	{
		U_Debug.ASSERT(this.isForegrounded());
		
		A_State state = m_children.get(T);
		
		if ( state == null )
		{
			U_Debug.ASSERT(false);
			return null;
		}
		
		if ( !state.isEntered() )
		{
			U_Debug.ASSERT(false);
			return null;
		}
		
		return state;
	}
	
	public A_State getState(Class<? extends A_State> T)
	{
		return m_children.get(T);
	}
	
	public boolean isStateForegrounded(Class<? extends A_State> T)
	{
		A_State state = this.getState(T);
		return state != null && state.isForegrounded();
	}
	
	public boolean isStateEntered(Class<? extends A_State> T)
	{
		return m_children.get(T) != null;
	}
	
	void internal_enterState(Class<? extends A_State> T, A_StateConstructor constructor)
	{
		if ( !this.isForegrounded() )
		{
			U_Debug.ASSERT(false);
			return;
		}
		
		if ( m_children.get(T) != null )
		{
			U_Debug.ASSERT(false);
			return;
		}
		
		A_State newState	= m_context.getInstance(T);
		m_children.put(T, newState);
		
		newState.m_parent = this;
		newState.didEnter_internal(constructor);
		
		m_childrenForegrounded.put(T, false);
	}
	
	void internal_foregroundState(Class<? extends A_State> T)
	{
		if ( !this.isForegrounded() )
		{
			U_Debug.ASSERT(false);
			return;
		}
		
		A_State state = getChildForManipulation(T);
	
		if ( state.isForegrounded() )
		{
			U_Debug.ASSERT(false);
			return;
		}
		
		state.didForeground_internal(null, null);
		
		m_childrenForegrounded.put(T, true);
	}
	
	void internal_backgroundState(Class<? extends A_State> T)
	{
		if ( !this.isForegrounded() )
		{
			U_Debug.ASSERT(false);
			return;
		}
		
		A_State state = getChildForManipulation(T);
	
		if ( !state.isForegrounded() )
		{
			U_Debug.ASSERT(false);
			return;
		}
		
		state.willBackground_internal(null);
		
		m_childrenForegrounded.put(T, false);
	}
	
	void internal_exitState(Class<? extends A_State> T)
	{
		if ( !this.isForegrounded() )
		{
			U_Debug.ASSERT(false);
			return;
		}
		
		A_State state = getChildForManipulation(T);
		
		if ( state.isForegrounded() )
		{
			state.willBackground_internal(null);
		}
		
		state.willExit_internal();
		
		state.m_parent = null;
		
		m_children.remove(T);
		m_childrenForegrounded.remove(T);
	}
	
	@Override
	void didEnter_internal(A_StateConstructor constructor)
	{
		U_Debug.ASSERT(m_children == null);
		U_Debug.ASSERT(m_childrenForegrounded == null);
		
		super.didEnter_internal(constructor);
		
		m_children = new HashMap<Class<? extends A_State>, A_State>();
		m_childrenForegrounded = new HashMap<Class<? extends A_State>, Boolean>();
	}
	
	@Override
	void didForeground_internal(Class<? extends A_State> revealingState, Object[] args)
	{
		final ArrayList<A_State> existingStates = new ArrayList<A_State>();

		for ( Class<? extends A_State> T : m_children.keySet() )
		{
			A_State state = m_children.get(T);
			existingStates.add(state);
		}

		super.didForeground_internal(revealingState, args);

		for ( int i = 0; i < existingStates.size(); i++ )
		{
			A_State existingState = existingStates.get(i);

			Class<? extends A_State> T = existingState.getClass();

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
		
		for ( Class<? extends A_State> T : m_children.keySet() )
		{
			A_State state = m_children.get(T);
			state.update_internal(timeStep);
		}
	}
	
	@Override
	void willBackground_internal(Class<? extends A_State> blockingState)
	{
		super.willBackground_internal(blockingState);
		
		for ( Class<? extends A_State> T : m_children.keySet() )
		{
			A_State state = m_children.get(T);
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
		
		HashMap<Class<? extends A_State>, A_State> children = m_children;
	
		m_children = null;
		m_childrenForegrounded = null;
		
		for ( Class<? extends A_State> T : children.keySet() )
		{
			A_State state = children.get(T);
			state.willExit_internal();
		}
	}
}