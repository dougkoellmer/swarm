package swarm.shared.statemachine;

import java.util.ArrayList;

import swarm.shared.debugging.bhU_Debug;


/**
 * ...
 * @author 
 */
public abstract class bhA_StateMachine extends bhA_State
{
	private bhA_State m_currentState;
	
	public bhA_StateMachine()
	{
	}
	
	void internal_setState(Class<? extends bhA_State> T, bhA_StateConstructor constructor)
	{
		bhA_State currentState = this.getCurrentState();
		bhA_State stateBeneath = null;
		
		if( currentState != null )
		{
			stateBeneath = this.m_currentState.getStateBeneath();
			this.exitSubState(currentState);
		}
		
		bhA_State newState = bhA_State.getInstance(T);
		
		this.enterSubState(newState, stateBeneath, false, constructor);
	}
	
	void pushState_internal(Class<? extends bhA_State> T, bhA_StateConstructor constructor)
	{
		bhA_State newState		= bhA_State.getInstance(T);
		bhA_State currentState = this.getCurrentState();
		
		bhU_Debug.ASSERT(currentState != null);
		
		if( currentState == null )
		{
			return;
		}
		else
		{
			if( !newState.isTransparent() )
			{
				// TODO: Walk the entire stack and make sure all states are backgrounded.
				currentState.willBackground_internal(T);
			}
		}
		
		this.enterSubState(newState, currentState, true, constructor);
	}
	
	void popState_internal(Object[] args)
	{
		//TODO: Walk the entire stack and make sure to take into account state transparency.
		//		A 3-state stack with the middle state transparent would require us to foreground
		//		both the first and second state in the stack.
		
		bhA_State poppedState = this.getCurrentState();
		bhA_State stateBeneath = poppedState.getStateBeneath();
		
		boolean validPop = poppedState != null && stateBeneath != null;
		bhU_Debug.ASSERT(validPop, "internal_popState1");
		
		if( !validPop )
		{
			return;
		}
		
		this.exitSubState(poppedState);
		
		m_currentState = stateBeneath;
		
		if( !poppedState.isTransparent() )
		{
			m_currentState.didForeground_internal(poppedState.getClass(), args);
		}
	}
	
	public <T extends bhA_State> T getCurrentState()
	{
		return (T) m_currentState;
	}
	
	@Override
	void didEnter_internal(bhA_StateConstructor constructor)
	{
		bhU_Debug.ASSERT(m_currentState == null);

		super.didEnter_internal(constructor);
	}
	
	@Override
	void didForeground_internal(Class<? extends bhA_State> revealingState, Object[] args)
	{
		bhA_State currentState = m_currentState;
		
		super.didForeground_internal(revealingState, args);
		
		if( currentState != null && currentState == m_currentState)
		{
			m_currentState.didForeground_internal(revealingState, args);
		}
	}
	
	@Override
	void update_internal(double timeStep)
	{
		super.update_internal(timeStep);
		
		bhA_State currentState = m_currentState;
		while( currentState != null )
		{
			bhA_State stateBeneath = currentState.getStateBeneath();

			if( currentState != null )
			{
				currentState.update_internal(timeStep);
			}
			
			currentState = stateBeneath;
		}
	}
	
	@Override
	void willBackground_internal(Class<? extends bhA_State> blockingState)
	{
		super.willBackground_internal(blockingState);
		
		if( m_currentState != null )
		{
			m_currentState.willBackground_internal(blockingState);
		}
	}
	
	@Override
	void willExit_internal()
	{
		super.willExit_internal();
		
		bhA_State currentState = m_currentState;
		
		m_currentState = null;
		
		while( currentState != null )
		{
			bhA_State stateBeneath = currentState.getStateBeneath();
			
			this.exitSubState(currentState);
			
			currentState = stateBeneath;
		}
	}
	
	private void enterSubState(bhA_State stateToEnter, bhA_State stateBeneath, boolean isPush, bhA_StateConstructor constructor)
	{
		boolean invalidSet = (this.m_currentState != null) && m_currentState == stateToEnter && m_currentState.isEntered();
		
		bhU_Debug.ASSERT(!invalidSet, "enterSubState1");
		
		//bhU_Debug.ASSERT(this.checkLegalStateManipulation());
	
		stateToEnter.m_stateBeneath = stateBeneath;
		stateToEnter.m_previousState = isPush ? null : (this.m_currentState != null ? this.m_currentState.getClass() : null);
		stateToEnter.m_parent = this;
		
		this.m_currentState = stateToEnter;
		this.m_currentState.m_root = this.m_root;

		this.m_currentState.didEnter_internal(constructor);
		this.m_currentState.didForeground_internal(null, null);
	}

	private void exitSubState(bhA_State stateToExit)
	{
		if( stateToExit.isForegrounded() )
		{
			stateToExit.willBackground_internal(null);
		}
		
		stateToExit.willExit_internal();
		
		stateToExit.m_root = null;
		stateToExit.m_parent = null;
		stateToExit.m_stateBeneath = null;
		stateToExit.m_previousState = null;
	}
}