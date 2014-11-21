package swarm.shared.statemachine;




/**
 * 
 * @author dougkoellmer
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
	
	public int getHistoryIndex()
	{
		return m_stackEntryV.getHistoryIndex();
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
	
	boolean pushV_internal(Class<? extends A_State> stateClass, StateArgs args)
	{
		if( m_currentState == null )
		{
			return false;
		}
		else
		{
			if( !isEnterable(stateClass, args) )  return false;
			
			A_State newState = m_context.getStateInstance(stateClass);
			
			if( !newState.isTransparent() )
			{
				backgroundStack(stateClass);
			}
			
			P_StackEntryV newEntryV = m_context.checkOutStackEntryV();
			newEntryV.set(stateClass, args);
			newEntryV.m_entryBeneath = m_stackEntryV;
			m_stackEntryV = newEntryV;

			this.enterChildState(newState, m_currentState, true, args, E_TransitionCause.PUSH_V);
			
			return true;
		}
	}
	
	boolean popV_internal(StateArgs args)
	{		
		A_State stateToPop = this.getCurrentState();
		A_State stateBeneath = stateToPop.getStateBeneath();
		
		boolean canPop = stateToPop != null && stateBeneath != null;
		
		if( !canPop )
		{
			return false;
		}
		else
		{
			P_StackEntryV entryBeneath = m_stackEntryV.m_entryBeneath;
			m_context.checkInStackEntryV(m_stackEntryV);
			m_stackEntryV = entryBeneath;
			
			boolean stateToPopIsTransparent = stateToPop.isTransparent();
			this.exitChildState(stateToPop, E_TransitionCause.POP_V);
			
			m_currentState = stateBeneath;
			
			if( !stateToPopIsTransparent )
			{
				m_currentState.didForeground_internal(stateToPop.getClass(), args);
			}

			return true;
		}
	}
	
	boolean remove_internal(boolean justChecking, StateFilter.Match match, Class<? extends Object> stateClass, Object ... argValues)
	{
		return m_stackEntryV.remove(justChecking, match.getTarget(), match, stateClass, null, argValues);
	}
	
	boolean remove_internal(boolean justChecking, StateFilter.Target target, Class<? extends Object> stateClass, StateArgs args)
	{
		return m_stackEntryV.remove(justChecking, target, null, stateClass, args);
	}
	
	boolean queue_internal(Class<? extends A_State> stateClass, StateArgs args)
	{
		m_stackEntryV.queue(m_context.checkOutStackEntryH(stateClass, args));
		
		return true;
	}
	
	@Override public Class<? extends A_State> get(StateFilter.Target target, int offset)
	{
		return m_stackEntryV.get(target, offset);
	}
	
	boolean dequeue_internal()
	{
		P_StackEntryH entry = m_stackEntryV.dequeue();
		
		if( entry != null )
		{
			boolean result = this.set_private(entry.m_stateClass, entry.m_args, E_TransitionCause.DEQUEUE);
			m_context.checkInStackEntryH(entry);
			
			return result;
		}
		else
		{
			return false;
		}
	}
	
	boolean pop_internal(StateArgs args)
	{
		P_StackEntryH entry = m_stackEntryV.pop();
		
		if( entry != null )
		{
			entry.m_args = defaultArgs(entry.m_args).impose(args);
			
			return set_private(entry.m_stateClass, entry.m_args, E_TransitionCause.POP);
		}
		else
		{
			return false;
		}
	}
	
	boolean go_internal(int offset, StateArgs args)
	{
		P_StackEntryH entry = m_stackEntryV.go(offset);
		
		if( entry != null )
		{
			entry.m_args = defaultArgs(entry.m_args).impose(args);
			
			return set_private(entry.m_stateClass, entry.m_args, E_TransitionCause.GO);
		}
		else
		{
			return false;
		}
	}
	
	boolean push_internal(Class<? extends A_State> stateClass, StateArgs args)
	{
		P_StackEntryH entry = m_context.checkOutStackEntryH(stateClass, args);
		m_stackEntryV.push(entry);
		
		return set_private(stateClass, args, E_TransitionCause.PUSH);
	}
	
	boolean set_internal(Class<? extends A_State> stateClass, StateArgs args)
	{
		m_stackEntryV.set(stateClass, args);
		
		return set_private(stateClass, args, E_TransitionCause.SET);
	}
	
	protected boolean isEnterable(Class<? extends A_State> stateClass, StateArgs args)
	{
		return true;
	}
	
	protected void willEnter(Class<? extends A_State> stateClass, StateArgs args)
	{
		
	}
	
	private boolean set_private(Class<? extends A_State> stateClass, StateArgs args, E_TransitionCause cause)
	{
		args = defaultArgs(args);
		
		if( !isEnterable(stateClass, args) )  return false;
		
		A_State currentState = this.getCurrentState();
		
//		if( currentState != null && currentState.getClass() == stateClass )
//		{
//			return false;
//		}
		
		A_State stateBeneath = null;
		
		if( currentState != null )
		{
			stateBeneath = currentState.getStateBeneath();
			this.exitChildState(currentState, cause);
		}
		
		A_State newState = m_context.getStateInstance(stateClass);
		
		willEnter(stateClass, args);
		
		this.enterChildState(newState, stateBeneath, false, args, cause);

		return true;
	}
	
	@Override void didEnter_internal(StateArgs constructor, E_TransitionCause cause)
	{
		m_stackEntryV = m_context.checkOutStackEntryV();
		
		super.didEnter_internal(constructor, cause);
	}
	
	@Override
	void didForeground_internal(Class<? extends A_State> revealingState, StateArgs args)
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
	
	@Override void willBackground_internal(Class<? extends A_State> blockingState)
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
	
	@Override void willExit_internal(E_TransitionCause cause)
	{
		super.willExit_internal(cause);
		
		A_State currentState = m_currentState;
		P_StackEntryV currentStackEntryV = m_stackEntryV;
		
		while( currentState != null )
		{
			A_State stateBeneath = currentState.getStateBeneath();
			P_StackEntryV entryBeneath = currentStackEntryV.m_entryBeneath;
			
			m_context.checkInStackEntryV(currentStackEntryV);
			this.exitChildState(currentState, cause);
			
			currentState = stateBeneath;
			currentStackEntryV = entryBeneath;
		}
		
		m_stackEntryV = null;
		m_currentState = null;
	}
	
	private void enterChildState(A_State stateToEnter, A_State stateBeneath, boolean isPush, StateArgs args, E_TransitionCause cause)
	{
		stateToEnter.m_stateBeneath = stateBeneath;
		stateToEnter.m_previousState = isPush ? null : (this.m_currentState != null ? this.m_currentState.getClass() : null);
		stateToEnter.m_parent = this;
		
		A_State currentState = this.m_currentState = stateToEnter;

		args = defaultArgs(args);
		
		currentState.didEnter_internal(args, cause);
		
		boolean shouldForeground = true;
		A_State stateV = m_currentState;
		while( currentState != stateV )
		{
			if( !stateV.isTransparent() )
			{
				shouldForeground = false;
				break;
			}
			stateV = stateV.getStateBeneath();
		}
		
		if( this.isForegrounded() && currentState.isEntered() && !currentState.isForegrounded() && shouldForeground )
		{
			currentState.didForeground_internal(null, null);
		}
	}

	private void exitChildState(A_State stateToExit, E_TransitionCause cause)
	{
		if( stateToExit.isForegrounded() )
		{
			stateToExit.willBackground_internal(null);
		}
		
		stateToExit.willExit_internal(cause);
	}
}