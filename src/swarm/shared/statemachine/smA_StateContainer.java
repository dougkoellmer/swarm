package swarm.shared.statemachine;

import java.util.ArrayList;
import java.util.HashMap;

import swarm.shared.debugging.smU_Debug;


/**
 * ...
 * @author 
 */
public abstract class smA_StateContainer extends smA_State
{
	private HashMap<Class<? extends smA_State>, smA_State> m_children = null;
	private HashMap<Class<? extends smA_State>, Boolean> m_childrenForegrounded = null;
	
	protected smA_StateContainer()
	{
	}
	
	private smA_State getChildForManipulation(Class<? extends smA_State> T)
	{
		smU_Debug.ASSERT(this.isForegrounded());
		
		smA_State state = m_children.get(T);
		
		if ( state == null )
		{
			smU_Debug.ASSERT(false);
			return null;
		}
		
		if ( !state.isEntered() )
		{
			smU_Debug.ASSERT(false);
			return null;
		}
		
		return state;
	}
	
	public smA_State getState(Class<? extends smA_State> T)
	{
		return m_children.get(T);
	}
	
	public boolean isStateForegrounded(Class<? extends smA_State> T)
	{
		smA_State state = this.getState(T);
		return state != null && state.isForegrounded();
	}
	
	public boolean isStateEntered(Class<? extends smA_State> T)
	{
		return m_children.get(T) != null;
	}
	
	void internal_enterState(Class<? extends smA_State> T, smA_StateConstructor constructor)
	{
		if ( !this.isForegrounded() )
		{
			smU_Debug.ASSERT(false);
			return;
		}
		
		if ( m_children.get(T) != null )
		{
			smU_Debug.ASSERT(false);
			return;
		}
		
		smA_State newState	= m_context.getInstance(T);
		m_children.put(T, newState);
		
		newState.m_parent = this;
		newState.m_context = this.m_context;
		newState.didEnter_internal(constructor);
		
		m_childrenForegrounded.put(T, false);
	}
	
	void internal_foregroundState(Class<? extends smA_State> T)
	{
		if ( !this.isForegrounded() )
		{
			smU_Debug.ASSERT(false);
			return;
		}
		
		smA_State state = getChildForManipulation(T);
	
		if ( state.isForegrounded() )
		{
			smU_Debug.ASSERT(false);
			return;
		}
		
		state.didForeground_internal(null, null);
		
		m_childrenForegrounded.put(T, true);
	}
	
	void internal_backgroundState(Class<? extends smA_State> T)
	{
		if ( !this.isForegrounded() )
		{
			smU_Debug.ASSERT(false);
			return;
		}
		
		smA_State state = getChildForManipulation(T);
	
		if ( !state.isForegrounded() )
		{
			smU_Debug.ASSERT(false);
			return;
		}
		
		state.willBackground_internal(null);
		
		m_childrenForegrounded.put(T, false);
	}
	
	void internal_exitState(Class<? extends smA_State> T)
	{
		if ( !this.isForegrounded() )
		{
			smU_Debug.ASSERT(false);
			return;
		}
		
		smA_State state = getChildForManipulation(T);
		
		if ( state.isForegrounded() )
		{
			state.willBackground_internal(null);
		}
		
		state.willExit_internal();
		
		state.m_parent = null;
		state.m_context = null;
		
		m_children.remove(T);
		m_childrenForegrounded.remove(T);
	}
	
	@Override
	void didEnter_internal(smA_StateConstructor constructor)
	{
		smU_Debug.ASSERT(m_children == null);
		smU_Debug.ASSERT(m_childrenForegrounded == null);
		
		super.didEnter_internal(constructor);
		
		m_children = new HashMap<Class<? extends smA_State>, smA_State>();
		m_childrenForegrounded = new HashMap<Class<? extends smA_State>, Boolean>();
	}
	
	@Override
	void didForeground_internal(Class<? extends smA_State> revealingState, Object[] args)
	{
		final ArrayList<smA_State> existingStates = new ArrayList<smA_State>();

		for ( Class<? extends smA_State> T : m_children.keySet() )
		{
			smA_State state = m_children.get(T);
			existingStates.add(state);
		}

		super.didForeground_internal(revealingState, args);

		for ( int i = 0; i < existingStates.size(); i++ )
		{
			smA_State existingState = existingStates.get(i);

			Class<? extends smA_State> T = existingState.getClass();

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
		
		for ( Class<? extends smA_State> T : m_children.keySet() )
		{
			smA_State state = m_children.get(T);
			state.update_internal(timeStep);
		}
	}
	
	@Override
	void willBackground_internal(Class<? extends smA_State> blockingState)
	{
		super.willBackground_internal(blockingState);
		
		for ( Class<? extends smA_State> T : m_children.keySet() )
		{
			smA_State state = m_children.get(T);
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
		
		HashMap<Class<? extends smA_State>, smA_State> children = m_children;
	
		m_children = null;
		m_childrenForegrounded = null;
		
		for ( Class<? extends smA_State> T : children.keySet() )
		{
			smA_State state = children.get(T);
			state.willExit_internal();
		}
	}
}