package swarm.shared.statemachine;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * 
 * @author dougkoellmer
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
		A_State state = m_children.get(T);
		
		if ( state == null )
		{
			return null;
		}
		
		if ( !state.isEntered() )
		{
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
	
	void enterState_internal(Class<? extends A_State> T, StateArgs constructor)
	{
		if ( !this.isForegrounded() )
		{
			return;
		}
		
		if ( m_children.get(T) != null )
		{
			return;
		}
		
		A_State newState	= m_context.getStateInstance(T);
		m_children.put(T, newState);
		
		newState.m_parent = this;
		newState.didEnter_internal(constructor, E_TransitionCause.SET);
		
		m_childrenForegrounded.put(T, false);
	}
	
	void foregroundState_internal(Class<? extends A_State> T)
	{
		if ( !this.isForegrounded() )
		{
			return;
		}
		
		A_State state = getChildForManipulation(T);
	
		if ( state.isForegrounded() )
		{
			return;
		}
		
		state.didForeground_internal(null, null);
		
		m_childrenForegrounded.put(T, true);
	}
	
	void backgroundState_internal(Class<? extends A_State> T)
	{
		if ( !this.isForegrounded() )
		{
			return;
		}
		
		A_State state = getChildForManipulation(T);
	
		if ( !state.isForegrounded() )
		{
			return;
		}
		
		state.willBackground_internal(null);
		
		m_childrenForegrounded.put(T, false);
	}
	
	void exitState_internal(Class<? extends A_State> T)
	{
		if ( !this.isForegrounded() )
		{
			return;
		}
		
		A_State state = getChildForManipulation(T);
		
		if ( state.isForegrounded() )
		{
			state.willBackground_internal(null);
		}
		
		state.willExit_internal(E_TransitionCause.SET);
		
		state.m_parent = null;
		
		m_children.remove(T);
		m_childrenForegrounded.remove(T);
	}
	
	@Override void didEnter_internal(StateArgs constructor, E_TransitionCause cause)
	{		
		super.didEnter_internal(constructor, cause);
		
		m_children = new HashMap<Class<? extends A_State>, A_State>();
		m_childrenForegrounded = new HashMap<Class<? extends A_State>, Boolean>();
	}
	
	@Override void didForeground_internal(Class<? extends A_State> revealingState, StateArgs args)
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
	
	@Override void willExit_internal(E_TransitionCause cause)
	{
		super.willExit_internal(cause);
		
		HashMap<Class<? extends A_State>, A_State> children = m_children;
	
		m_children = null;
		m_childrenForegrounded = null;
		
		for ( Class<? extends A_State> T : children.keySet() )
		{
			A_State state = children.get(T);
			state.willExit_internal(cause);
		}
	}
}