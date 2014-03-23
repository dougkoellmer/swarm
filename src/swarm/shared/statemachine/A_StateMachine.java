package swarm.shared.statemachine;



/**
 * ...
 * @author 
 */
public abstract class A_StateMachine extends A_State
{
	private A_State m_currentState;
	
	public A_StateMachine()
	{
	}
	
	public <T extends A_State> T getCurrentState()
	{
		return (T) m_currentState;
	}
	
	void pushState_internal(Class<? extends A_State> T, StateArgs constructor)
	{
		A_State newState		= m_context.getInstance(T);
		A_State currentState = this.getCurrentState();
		
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
		
		A_State stateToPop = this.getCurrentState();
		A_State stateBeneath = stateToPop.getStateBeneath();
		
		boolean canPop = stateToPop != null && stateBeneath != null;
		
		if( !canPop )
		{
			return;
		}
		
		this.exitSubState(stateToPop);
		
		m_currentState = stateBeneath;
		
		if( !stateToPop.isTransparent() )
		{
			m_currentState.didForeground_internal(stateToPop.getClass(), args);
		}
	}
	
	void setState_internal(Class<? extends A_State> T, StateArgs constructor)
	{
		A_State currentState = this.getCurrentState();
		
		if( currentState != null && currentState.getClass() == T )
		{
			return;
		}
		
		A_State stateBeneath = null;
		
		if( currentState != null )
		{
			stateBeneath = this.m_currentState.getStateBeneath();
			this.exitSubState(currentState);
		}
		
		A_State newState = m_context.getInstance(T);
		
		this.enterSubState(newState, stateBeneath, false, constructor);
	}
	
	@Override
	void didEnter_internal(StateArgs constructor)
	{
		super.didEnter_internal(constructor);
		
		// nothing to do!
	}
	
	@Override
	void didForeground_internal(Class<? extends A_State> revealingState, Object[] args)
	{
		A_State currentState = m_currentState;
		
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
		
		A_State currentState = m_currentState;
		while( currentState != null )
		{
			A_State stateBeneath = currentState.getStateBeneath();

			if( currentState != null )
			{
				currentState.update_internal(timeStep);
			}
			
			currentState = stateBeneath;
		}
	}
	
	@Override
	void willBackground_internal(Class<? extends A_State> blockingState)
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
		
		A_State currentState = m_currentState;
		
		m_currentState = null;
		
		while( currentState != null )
		{
			A_State stateBeneath = currentState.getStateBeneath();
			
			this.exitSubState(currentState);
			
			currentState = stateBeneath;
		}
	}
	
	private void enterSubState(A_State stateToEnter, A_State stateBeneath, boolean isPush, StateArgs constructor)
	{
		stateToEnter.m_stateBeneath = stateBeneath;
		stateToEnter.m_previousState = isPush ? null : (this.m_currentState != null ? this.m_currentState.getClass() : null);
		stateToEnter.m_parent = this;
		
		this.m_currentState = stateToEnter;

		this.m_currentState.didEnter_internal(constructor);
		this.m_currentState.didForeground_internal(null, null);
	}

	private void exitSubState(A_State stateToExit)
	{
		if( stateToExit.isForegrounded() )
		{
			stateToExit.willBackground_internal(null);
		}
		
		stateToExit.willExit_internal();
		
		stateToExit.m_parent = null;
		stateToExit.m_stateBeneath = null;
		stateToExit.m_previousState = null;
	}
}