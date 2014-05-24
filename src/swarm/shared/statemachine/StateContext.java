package swarm.shared.statemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import swarm.shared.statemachine.P_StateObjectPool.I_Factory;

public class StateContext extends A_StateContextForwarder
{
	private static final Logger s_logger = Logger.getLogger(StateContext.class.getName());
	
	private final A_State m_rootState;
	
	private final ArrayList<I_StateEventListener> m_listeners = new ArrayList<I_StateEventListener>();
	private final ArrayList<StateEvent> m_eventQueue = new ArrayList<StateEvent>();
	private int m_eventQueueIndex = 0;
	
	private boolean m_processEventQueue_hasEntered = false;
	
	private int m_queueEvent_recursionDepth = 0;
	
	private final HashMap<Class<? extends A_State>, A_State> m_stateRegistry = new HashMap<Class<? extends A_State>, A_State>();
	private final HashMap<Class<? extends A_Action>, A_Action> m_actionRegistry = new HashMap<Class<? extends A_Action>, A_Action>();
	private I_StateFactory m_stateFactory;
	
	private int m_throttleCount = 0;
	private double m_minTimeStep = 0.0;
	private double m_timeAggregator = 0.0;
	
	private static class StackEntryFactoryV implements P_StateObjectPool.I_Factory<P_StackEntryV>
	{
		@Override
		public P_StackEntryV newInstance()
		{
			return new P_StackEntryV();
		}
	}
	
	private final P_StateObjectPool<P_StackEntryV> m_stackEntryPoolV = new P_StateObjectPool<P_StackEntryV>(new StackEntryFactoryV());
	
	
	private static class StackEntryFactoryH implements P_StateObjectPool.I_Factory<P_StackEntryH>
	{
		@Override
		public P_StackEntryH newInstance()
		{
			return new P_StackEntryH();
		}
	}
	
	private final P_StateObjectPool<P_StackEntryH> m_stackEntryPoolH = new P_StateObjectPool<P_StackEntryH>(new StackEntryFactoryH());
	
	public StateContext(A_State rootState, I_StateEventListener stateEventListener)
	{
		this(rootState, stateEventListener, null);
	}
	
	public StateContext(A_State rootState, I_StateEventListener stateEventListener, I_StateFactory stateFactory)
	{
		m_rootState = rootState;
		
		this.addListener(stateEventListener);
		
		register(m_rootState);
		
		m_stateFactory = stateFactory;
	}
	
	public A_State getRootState()
	{
		return m_rootState;
	}
	
	public void onDidEnter()
	{		
		m_rootState.didEnter_internal(null);
	}
	
	public void onDidForeground()
	{
		m_rootState.didForeground_internal(null, null);
	}
	
	public void onUpdate(double timeStep)
	{
		m_timeAggregator += timeStep;
		
		if( m_timeAggregator >= m_minTimeStep )
		{
			m_rootState.update_internal(m_timeAggregator);
			
			m_timeAggregator = 0.0;
		}
	}
	
	public void onWillBackground()
	{		
		m_rootState.willBackground_internal(null);
	}
	
	public void onWillExit()
	{
		m_rootState.willExit_internal();
	}
	
	void register(A_Action action)
	{
		this.register(action, null);
	}
	
	public void register(A_Action action, Class<? extends A_State> association)
	{		
		m_actionRegistry.put(action.getClass(), action);
		
		action.m_context = this;
		
		if( association != null )
		{
			action.m_association = association;
		}
	}
	
	A_Action getActionInstance(Class<? extends A_Action> T)
	{
		A_Action registeredAction = m_actionRegistry.get(T);
	
		if ( registeredAction != null )
		{			
			return registeredAction;
		}
		else if( m_stateFactory != null )
		{
			registeredAction = m_stateFactory.newAction(T);
			
			if( registeredAction != null )
			{
				this.register(registeredAction);
			}
			
			return registeredAction;
		}
		
		return null;
	}

	private A_State getEnteredStateForAction(Class<? extends A_Action> T)
	{
		A_Action action = getActionInstance(T);
		
		if( action != null )
		{
			Class<? extends A_State> state_T = action.getStateAssociation();
			
			A_State state = this.getEntered(state_T);
			
			return state;
		}
		
		return null;
	}
	
	@Override
	public boolean perform(Class<? extends A_Action> T, StateArgs args)
	{
		A_State state = getEnteredStateForAction(T);
		
		if( state == null )  return false;
		
		return state.performAction(T, args);
	}
	
	@Override
	public boolean isPerformable(Class<? extends A_Action> T, StateArgs args)
	{
		A_State state = getEnteredStateForAction(T);
		
		return isPerformable_private(state, T, args);
	}
	
	private boolean isPerformable_private(A_State state, Class<? extends A_Action> T, StateArgs args)
	{
		if( state == null )
		{
			return false;
		}
		else
		{
			return state.isPerformable(T, args);
		}
	}
	
	@Override
	public <T extends A_State> T getEntered(Class<? extends A_State> T)
	{
		A_State registeredState = m_stateRegistry.get(T);
		if ( registeredState != null )
		{
			if ( registeredState.isEntered() )
			{
				return (T) registeredState;
			}
		}
		
		return null;
	}
	
	@Override
	public <T extends A_State> T getForegrounded(Class<? extends A_State> T)
	{
		A_State registeredState = m_stateRegistry.get(T);
		if ( registeredState != null )
		{
			if ( registeredState.isForegrounded() )
			{
				return (T) registeredState;
			}
		}
		
		return null;
	}
	
	
	
	
	public void register(A_State state)
	{
		m_stateRegistry.put(state.getClass(), state);
		state.m_context = this;
		
		state.onRegistered();
	}
	
	
	
	A_State getStateInstance(Class<? extends A_State> T)
	{
		A_State registeredState = m_stateRegistry.get(T);
		if ( registeredState != null )
		{
			if ( registeredState.isEntered() )
			{
				//"Tried to reuse state instance.";
			}
			
			return registeredState;
		}
		else if( m_stateFactory != null )
		{
			registeredState = m_stateFactory.newState(T);
			
			if( registeredState != null )
			{
				this.register(registeredState);
				return registeredState;
			}
		}
		
		return null;
	}
	
	void beginBatch()
	{
		m_queueEvent_recursionDepth++;
	}
	
	void endBatch()
	{
		processEventQueue();
	}
	
	void queueEvent(StateEvent newEvent)
	{
		boolean findAntiEvents = false;

		switch( newEvent.getType() )
		{
			case DID_EXIT:
			case DID_BACKGROUND:
			{
				findAntiEvents = true;
			}
		}
		
		boolean antiMatterExplosion = false;
		
		if( findAntiEvents )
		{
			for( int i = m_eventQueue.size()-1; i >= m_eventQueueIndex; i-- )
			{
				StateEvent pastEvent = m_eventQueue.get(i);
				
				if( pastEvent == null )
				{
					continue;
				}
				
				if( pastEvent.m_state == newEvent.getState() )
				{
					if( newEvent.getType() == E_Event.DID_EXIT )
					{
						if( pastEvent.getType() == E_Event.DID_ENTER )
						{
							antiMatterExplosion = true;
						}
						/*else if( pastEvent.getType() == smE_StateEventType.DID_PERFORM_ACTION )
						{
							antiMatterExplosion = true;
						}*/
					}
					else if( newEvent.getType() == E_Event.DID_BACKGROUND )
					{
						if( pastEvent.getType() == E_Event.DID_FOREGROUND )
						{
							antiMatterExplosion = true;
						}
						/*else if( pastEvent.getType() == smE_StateEventType.DID_PERFORM_ACTION )
						{
							if( !pastEvent.m_action.isPerformableInBackground() )
							{
								antiMatterExplosion = true;
							}
						}*/
					}
				}
				
				if( antiMatterExplosion )
				{
					m_eventQueue.set(i, null);
					
					break;
				}
			}
		}
		
		if( !antiMatterExplosion )
		{
			m_eventQueue.add(newEvent);
		}
		
		m_queueEvent_recursionDepth++;
	}
	
	void addListener(I_StateEventListener listener)
	{
		m_listeners.add(listener);
	}
	
	void removeListener(I_StateEventListener listener)
	{
		for( int i = m_listeners.size()-1; i >= 0; i-- )
		{
			if( m_listeners.get(i) == listener )
			{
				if( !m_processEventQueue_hasEntered )
				{
					m_listeners.remove(i);
				}
				else
				{
					m_listeners.set(i, null);
				}
				
				break;
			}
		}
	}
	
	private void cleanListeners()
	{
		if( !m_processEventQueue_hasEntered )
		{
			//assert
		}
		
		for( int i = m_listeners.size()-1; i >= 0; i-- )
		{
			if( m_listeners.get(i) == null )
			{
				m_listeners.remove(i);
				
				break;
			}
		}
	}
	
	void processEventQueue()
	{
		m_queueEvent_recursionDepth--;
		
		if( m_queueEvent_recursionDepth > 0 )
		{
			return;
		}
		
		boolean firstEntryIntoMethod = false;
		
		if( !m_processEventQueue_hasEntered )
		{
			m_eventQueueIndex = 0;
			
			firstEntryIntoMethod = true;

			m_processEventQueue_hasEntered = true;
		}
		
		if( !firstEntryIntoMethod )
		{
			return;
		}
		
		if( m_listeners.size() == 0 )
		{
			m_eventQueue.clear();
			m_eventQueueIndex = 0;
			
			m_processEventQueue_hasEntered = false;
			
			return;
		}
		
		//--- DRK > NOTE: Keeping the indices as members of this class is useless now,
		//---				but at some point was necessary as recursive calls to this method were possible.
		//---				Just keeping them as members for now because this class is a house of cards.
		while( m_eventQueueIndex < m_eventQueue.size() )
		{
			StateEvent event = m_eventQueue.get(m_eventQueueIndex);
			
			if( event == null )
			{
				m_eventQueueIndex++;
				
				continue;
			}

			if( event.m_listenerIndex == StateEvent.UNINITIALIZED_LISTENER_INDEX )
			{
				event.m_listenerIndex = m_listeners.size()-1;
			}
			
			//s_logger.severe(event.getType() + " " + event.getState());
			
			while( event.m_listenerIndex >= 0 )
			{
				int listenerIndex = event.m_listenerIndex;
				event.m_listenerIndex--;
				
				if( event.m_listenerIndex == -1 )
				{
					m_eventQueueIndex++;
				}
				
				//--- DRK > Listener at this index can be null if it was removed while m_processEventQueue_hasEntered == true.
				I_StateEventListener listener = m_listeners.get(listenerIndex);
				if( listener != null )
				{
					event.dispatch(listener);
				}

				//--- DRK > If we still have listeners to go, and it appears that this event was cancelled out inside
				//---		the last dispatch, we continue our merry way along the event queue.
				if( event.m_listenerIndex >= 0 )
				{
					if( m_eventQueue.get(m_eventQueueIndex) == null )
					{
						break;
					}
				}
			}
		}
		
		if( m_eventQueueIndex >= m_eventQueue.size() )
		{
			m_eventQueue.clear();
			m_eventQueueIndex= 0;
		}
		
		if( firstEntryIntoMethod )
		{
			m_processEventQueue_hasEntered = false;
			
			cleanListeners();
		}
	}
	
	P_StackEntryV checkOutStackEntryV()
	{
		P_StackEntryV entryV = m_stackEntryPoolV.checkOut();
		entryV.init(this);
		
		return entryV;
	}
	
	void checkInStackEntryV(P_StackEntryV entry)
	{
		entry.destruct();
		m_stackEntryPoolV.checkIn(entry);
	}
	
	P_StackEntryH checkOutStackEntryH()
	{
		return m_stackEntryPoolH.checkOut();
	}
	
	P_StackEntryH checkOutStackEntryH(Class<? extends A_State> stateClass, StateArgs args)
	{
		P_StackEntryH entry = m_stackEntryPoolH.checkOut();
		entry.init(stateClass, args);
		
		return entry;
	}
	
	void checkInStackEntryH(P_StackEntryH entry)
	{
		entry.clean();
		m_stackEntryPoolH.checkIn(entry);
	}

	@Override
	StateContext getContext_internal()
	{
		return this;
	}
	
	public void pushUpdateThrottle(int fps)
	{
		double timeStep = 1.0/((double)fps);
		
		pushUpdateThrottle(timeStep);
	}
	
	public void pushUpdateThrottle(double minTimeStep)
	{
		if( m_minTimeStep == 0.0 )
		{
			m_minTimeStep = minTimeStep;
		}
		else
		{
			m_minTimeStep = minTimeStep < m_minTimeStep ? minTimeStep : m_minTimeStep;
		}
		
		m_throttleCount++;
	}
	
	public void popUpdateThrottle()
	{
		m_throttleCount--;
		
		if( m_throttleCount <= 0 )
		{
			m_minTimeStep = 0.0;
			m_throttleCount = 0;
		}
	}
}