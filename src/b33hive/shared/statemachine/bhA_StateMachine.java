package b33hive.shared.statemachine;

import java.util.ArrayList;

import b33hive.shared.debugging.bhU_Debug;


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
	
	void internal_pushState(Class<? extends bhA_State> T, bhA_StateConstructor constructor)
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
				currentState.internal_willBackground(T);
			}
		}
		
		this.enterSubState(newState, currentState, true, constructor);
	}
	
	void internal_popState(Object[] args)
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
			m_currentState.internal_didForeground(poppedState.getClass(), args);
		}
	}
	
	public <T extends bhA_State> T getCurrentState()
	{
		return (T) m_currentState;
	}
	
	@Override
	void internal_didEnter(bhA_StateConstructor constructor)
	{
		bhU_Debug.ASSERT(m_currentState == null);

		super.internal_didEnter(constructor);
	}
	
	@Override
	void internal_didForeground(Class<? extends bhA_State> revealingState, Object[] args)
	{
		bhA_State currentState = m_currentState;
		
		super.internal_didForeground(revealingState, args);
		
		if( currentState != null && currentState == m_currentState)
		{
			m_currentState.internal_didForeground(revealingState, args);
		}
	}
	
	@Override
	void internal_update(double timeStep)
	{
		super.internal_update(timeStep);
		
		bhA_State currentState = m_currentState;
		while( currentState != null )
		{
			bhA_State stateBeneath = currentState.getStateBeneath();

			if( currentState != null )
			{
				currentState.internal_update(timeStep);
			}
			
			currentState = stateBeneath;
		}
	}
	
	@Override
	void internal_willBackground(Class<? extends bhA_State> blockingState)
	{
		super.internal_willBackground(blockingState);
		
		if( m_currentState != null )
		{
			m_currentState.internal_willBackground(blockingState);
		}
	}
	
	@Override
	void internal_willExit()
	{
		super.internal_willExit();
		
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

		this.m_currentState.internal_didEnter(constructor);
		this.m_currentState.internal_didForeground(null, null);
	}

	private void exitSubState(bhA_State stateToExit)
	{
		if( stateToExit.isForegrounded() )
		{
			stateToExit.internal_willBackground(null);
		}
		
		stateToExit.internal_willExit();
		
		stateToExit.m_root = null;
		stateToExit.m_parent = null;
		stateToExit.m_stateBeneath = null;
		stateToExit.m_previousState = null;
	}
}