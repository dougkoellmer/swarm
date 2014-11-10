package swarm.shared.statemachine;

import swarm.shared.statemachine.P_StateObjectPool.I_Factory;

/**
 * 
 * @author dougkoellmer
 */
class P_EventPool
{
	private final P_StateObjectPool<A_BaseStateEvent>[] m_pools = new P_StateObjectPool[E_Event.values().length];
	
	P_EventPool()
	{
		for( int i = 0; i < m_pools.length; i++ )
		{
			E_Event eventType = E_Event.values()[i];
			
			I_Factory<A_BaseStateEvent> factory = newFactory(eventType);
			
			m_pools[i] = new P_StateObjectPool<A_BaseStateEvent>(factory);
		}
	}
	
	private <T extends A_BaseStateEvent> T checkOut(E_Event eventType)
	{
		P_StateObjectPool<A_BaseStateEvent> pool = m_pools[eventType.ordinal()];
		
		return (T) pool.checkOut();
	}
	
	BackgroundEvent checkOutBackgroundEvent(A_State state, Class<? extends A_State> blockingState)
	{
		BackgroundEvent event = checkOut(E_Event.DID_BACKGROUND);
		event.init(state, blockingState);
		
		return event;
	}
	
	EnterOrExitEvent checkOutEnterEvent(A_State state, E_Event eventType, E_TransitionCause cause)
	{
		EnterOrExitEvent event = checkOut(E_Event.DID_ENTER);
		event.init(state, eventType, cause);
		
		return event;
	}
	
	ExitEvent checkOutExitEvent(A_State state, E_TransitionCause cause)
	{
		ExitEvent event = checkOut(E_Event.DID_EXIT);
		event.init(state, cause);
		
		return event;
	}
	
	ForegroundEvent checkOutForegroundEvent(A_State state, Class<? extends A_State> revealingState, StateArgs args)
	{
		ForegroundEvent event = checkOut(E_Event.DID_FOREGROUND);
		event.init(state, revealingState, args);
		
		return event;
	}
	
	ActionEvent checkOutActionEvent(A_State state, A_Action_Base action)
	{
		ActionEvent event = checkOut(E_Event.DID_PERFORM_ACTION);
		event.init(state, action);
		
		return event;
	}
	
	UpdateEvent checkOutUpdateEvent(A_State state)
	{
		UpdateEvent event = checkOut(E_Event.DID_UPDATE);
		event.init(state);
		
		return event;
	}
	
	void checkIn(A_BaseStateEvent event)
	{
		P_StateObjectPool<A_BaseStateEvent> pool = m_pools[event.getType().ordinal()];
		event.clean();
		pool.checkIn(event);
	}
	
	private static I_Factory<A_BaseStateEvent> newFactory(E_Event eventType)
	{
		switch(eventType)
		{
			case DID_BACKGROUND:
			{
				return new I_Factory<A_BaseStateEvent>()
				{
					@Override public A_BaseStateEvent newInstance()
					{
						return new BackgroundEvent();
					}
				};
			}
			case DID_ENTER:
			{
				return new I_Factory<A_BaseStateEvent>()
				{
					@Override public A_BaseStateEvent newInstance()
					{
						return new EnterOrExitEvent();
					}
				};
			}
			case DID_EXIT:
			{
				return new I_Factory<A_BaseStateEvent>()
				{
					@Override public A_BaseStateEvent newInstance()
					{
						return new ExitEvent();
					}
				};
			}
			case DID_FOREGROUND:
			{
				return new I_Factory<A_BaseStateEvent>()
				{
					@Override public A_BaseStateEvent newInstance()
					{
						return new ForegroundEvent();
					}
				};
			}
			case DID_PERFORM_ACTION:
			{
				return new I_Factory<A_BaseStateEvent>()
				{
					@Override public A_BaseStateEvent newInstance()
					{
						return new ActionEvent();
					}
				};
			}
			case DID_UPDATE:
			{
				return new I_Factory<A_BaseStateEvent>()
				{
					@Override public A_BaseStateEvent newInstance()
					{
						return new UpdateEvent();
					}
				};
			}
		}
		
		return null;
	}
}
