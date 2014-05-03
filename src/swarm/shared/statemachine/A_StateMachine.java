package swarm.shared.statemachine;



/**
 * ...
 * @author 
 */
public abstract class A_StateMachine extends A_State
{
	private P_StackEntryV m_stackEntryV;
	private A_State m_currentState;
	
	public A_StateMachine()
	{
	}
	
	public <T extends A_State> T getCurrentState()
	{
		return (T) m_currentState;
	}
	
	public <T extends A_State> T getBottomState()
	{
		A_State currentState = m_currentState;
		
		while( currentState != null )
		{
			A_State next = currentState.getStateBeneath();
			
			if( next == null )  break;
			
			currentState = next;
		}
		
		return (T) currentState;
	}
	
	StateOperationResult pushV_internal(Class<? extends A_State> stateClass, StateArgs args)
	{
		if( m_currentState == null )
		{
			return m_context.checkOutResult(this, false);
		}
		else
		{
			A_State newState = m_context.getInstance(stateClass);
			
			if( !newState.isTransparent() )
			{
				backgroundStack(stateClass);
			}
			
			P_StackEntryV newEntryV = m_context.checkOutStackEntryV();
			newEntryV.set(stateClass, args);
			newEntryV.m_entryBeneath = m_stackEntryV;
			m_stackEntryV = newEntryV;

			this.enterChildState(newState, m_currentState, true, args);
			
			return m_context.checkOutResult(this, true);
		}
	}
	
	StateOperationResult popV_internal(Object[] args)
	{		
		A_State stateToPop = this.getCurrentState();
		A_State stateBeneath = stateToPop.getStateBeneath();
		
		boolean canPop = stateToPop != null && stateBeneath != null;
		
		if( !canPop )
		{
			return m_context.checkOutResult(this, false);
		}
		else
		{
			P_StackEntryV entryBeneath = m_stackEntryV.m_entryBeneath;
			m_context.checkInStackEntryV(m_stackEntryV);
			m_stackEntryV = entryBeneath;
			
			boolean stateToPopIsTransparent = stateToPop.isTransparent();
			this.exitChildState(stateToPop);
			
			m_currentState = stateBeneath;
			
			if( !stateToPopIsTransparent )
			{
				m_currentState.didForeground_internal(stateToPop.getClass(), args);
			}
			
			return m_context.checkOutResult(this, true);
		}
	}
	
	StateOperationResult queue_internal(Class<? extends A_State> stateClass, StateArgs args)
	{
		m_stackEntryV.queue(m_context.checkOutStackEntryH(stateClass, args));
		
		return m_context.checkOutResult(this, true);
	}
	
	StateOperationResult dequeue_internal()
	{
		P_StackEntryH entry = m_stackEntryV.dequeue();
		
		if( entry != null )
		{
			StateOperationResult result = this.set_private(entry.m_stateClass, entry.m_args);
			m_context.checkInStackEntryH(entry);
			
			return result;
		}
		else
		{
			return m_context.checkOutResult(this, false);
		}
	}
	
	StateOperationResult pop_internal()
	{
		P_StackEntryH entry = m_stackEntryV.popH();
		
		if( entry != null )
		{
			return set_private(entry.m_stateClass, entry.m_args);
		}
		else
		{
			return m_context.checkOutResult(this, false);
		}
	}
	
	StateOperationResult clearHistory_internal()
	{
		m_stackEntryV.clearHistory();
		
		return m_context.checkOutResult(this, true);
	}
	
	StateOperationResult go_internal(int offset)
	{
		P_StackEntryH entry = m_stackEntryV.goH(offset);
		
		if( entry != null )
		{
			return set_private(entry.m_stateClass, entry.m_args);
		}
		else
		{
			return m_context.checkOutResult(this, false);
		}
	}
	
	StateOperationResult push_internal(Class<? extends A_State> stateClass, StateArgs args)
	{
		P_StackEntryH entry = m_context.checkOutStackEntryH(stateClass, args);
		m_stackEntryV.push(entry);
		
		return set_private(stateClass, args);
	}
	
	StateOperationResult set_internal(Class<? extends A_State> stateClass, StateArgs args)
	{
		m_stackEntryV.set(stateClass, args);
		
		return set_private(stateClass, args);
	}
	
	private StateOperationResult set_private(Class<? extends A_State> stateClass, StateArgs args)
	{
		A_State currentState = this.getCurrentState();
		
		if( currentState != null && currentState.getClass() == stateClass )
		{
			return m_context.checkOutResult(this, false);
		}
		
		A_State stateBeneath = null;
		
		if( currentState != null )
		{
			stateBeneath = currentState.getStateBeneath();
			this.exitChildState(currentState);
		}
		
		A_State newState = m_context.getInstance(stateClass);
		
		this.enterChildState(newState, stateBeneath, false, args);
		
		return m_context.checkOutResult(this, true);
	}
	
	@Override
	void didEnter_internal(StateArgs constructor)
	{
		super.didEnter_internal(constructor);
		
		m_stackEntryV = m_context.checkOutStackEntryV();
	}
	
	@Override
	void didForeground_internal(Class<? extends A_State> revealingState, Object[] args)
	{
		A_State oldCurrentState = m_currentState;
		
		super.didForeground_internal(revealingState, args);
		
		A_State newCurrentState = m_currentState;
		
		if( oldCurrentState != null && oldCurrentState == newCurrentState)
		{
			oldCurrentState.didForeground_internal(revealingState, args);
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

			if( currentState != null && currentState.isEntered() )
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

		backgroundStack(blockingState);
	}
	
	private void backgroundStack(Class<? extends A_State> blockingState)
	{
		A_State currentState = m_currentState;
		
		while( currentState != null )
		{
			A_State stateBeneath = currentState.getStateBeneath();

			//--- DRK > States further down the stack than the topmost can be foregrounded
			//---		if state(s) above them are transparent.
			if( currentState != null && currentState.isForegrounded())
			{
				currentState.willBackground_internal(blockingState);
			}
			
			currentState = stateBeneath;
		}
	}
	
	@Override
	void willExit_internal()
	{
		super.willExit_internal();
		
		A_State currentState = m_currentState;
		P_StackEntryV currentStackEntryV = m_stackEntryV;
		
		while( currentState != null )
		{
			A_State stateBeneath = currentState.getStateBeneath();
			P_StackEntryV entryBeneath = currentStackEntryV.m_entryBeneath;
			
			m_context.checkInStackEntryV(currentStackEntryV);
			this.exitChildState(currentState);
			
			currentState = stateBeneath;
			currentStackEntryV = entryBeneath;
		}
		
		m_stackEntryV = null;
		m_currentState = null;
	}
	
	private void enterChildState(A_State stateToEnter, A_State stateBeneath, boolean isPush, StateArgs args)
	{
		stateToEnter.m_stateBeneath = stateBeneath;
		stateToEnter.m_previousState = isPush ? null : (this.m_currentState != null ? this.m_currentState.getClass() : null);
		stateToEnter.m_parent = this;
		
		this.m_currentState = stateToEnter;

		this.m_currentState.didEnter_internal(args);
		
		if( this.isForegrounded() )
		{
			this.m_currentState.didForeground_internal(null, null);
		}
	}

	private void exitChildState(A_State stateToExit)
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