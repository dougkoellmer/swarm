package swarm.shared.statemachine;

import java.util.logging.Logger;

import swarm.shared.debugging.U_Debug;

public class StateEvent
{
	private static final Logger s_logger = Logger.getLogger(StateEvent.class.getName());
	
	static final int UNINITIALIZED_LISTENER_INDEX = Integer.MIN_VALUE;
	
	private E_StateEventType m_eventType = null;
	A_State m_state = null;
	A_Action m_action = null; // we keep an instance, but only ever expose the class publicly.
	A_ActionArgs m_actionArgs = null;
	private Class<? extends A_State> m_blockingOrRevealingState;
	
	int m_listenerIndex = UNINITIALIZED_LISTENER_INDEX;
	
	StateEvent(E_StateEventType eventType, A_State state)
	{
		m_eventType = eventType;
		m_state = state;
	}
	
	StateEvent(A_State state, A_Action action, A_ActionArgs args)
	{
		m_actionArgs = args;
		m_state = state;
		m_action = action;
		m_eventType = E_StateEventType.DID_PERFORM_ACTION;
	}
	
	StateEvent(E_StateEventType eventType, A_State state, Class<? extends A_State> blockingOrRevealingState)
	{
		m_eventType = eventType;
		m_state = state;
		m_blockingOrRevealingState = blockingOrRevealingState;
	}
	
	public <T extends A_ActionArgs> T getActionArgs()
	{
		return (T) m_actionArgs;
	}
	
	public Class<? extends A_State> getBlockingState()
	{
		if( m_eventType == E_StateEventType.DID_BACKGROUND )
		{
			return m_blockingOrRevealingState;
		}
		else
		{
			return null;
		}
	}
	
	public Class<? extends A_State> getRevealingState()
	{
		if( m_eventType == E_StateEventType.DID_FOREGROUND )
		{
			return m_blockingOrRevealingState;
		}
		else
		{
			return null;
		}
	}
	
	public Class<? extends A_Action> getAction()
	{
		return m_action.getClass();
	}
	
	public <T extends A_State> T getState()
	{
		return (T) (m_eventType != E_StateEventType.DID_PERFORM_ACTION ? m_state : null);
	}
	
	public StateContext getContext()
	{
		return m_state.getContext();
	}
	
	public E_StateEventType getType()
	{
		return m_eventType;
	}
	
	void dispatch(I_StateEventListener listener)
	{
		if( m_listenerIndex == -1 )
		{
			if( m_action != null )
			{
				if( !m_action.suppressLog() )
				{
					//s_logger.info("'Performed " + m_action.getClass().getName() + "' dispatching to last listener...");
				}
			}
			else if( m_eventType != E_StateEventType.DID_UPDATE )
			{
				//s_logger.info("'" + m_eventType.toString() + " " + m_state.getClass().getName()  + "' dispatching to last listener...");
			}
		}
		
		listener.onStateEvent(this);
	}
}
