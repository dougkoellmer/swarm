package swarm.shared.statemachine;

import java.util.logging.Logger;

import swarm.shared.debugging.smU_Debug;

public class smStateEvent
{
	private static final Logger s_logger = Logger.getLogger(smStateEvent.class.getName());
	
	static final int UNINITIALIZED_LISTENER_INDEX = Integer.MIN_VALUE;
	
	private smE_StateEventType m_eventType = null;
	smA_State m_state = null;
	smA_Action m_action = null; // we keep an instance, but only ever expose the class publicly.
	smA_ActionArgs m_actionArgs = null;
	private Class<? extends smA_State> m_blockingOrRevealingState;
	
	int m_listenerIndex = UNINITIALIZED_LISTENER_INDEX;
	
	smStateEvent(smE_StateEventType eventType, smA_State state)
	{
		m_eventType = eventType;
		m_state = state;
	}
	
	smStateEvent(smA_State state, smA_Action action, smA_ActionArgs args)
	{
		m_actionArgs = args;
		m_state = state;
		m_action = action;
		m_eventType = smE_StateEventType.DID_PERFORM_ACTION;
	}
	
	smStateEvent(smE_StateEventType eventType, smA_State state, Class<? extends smA_State> blockingOrRevealingState)
	{
		m_eventType = eventType;
		m_state = state;
		m_blockingOrRevealingState = blockingOrRevealingState;
	}
	
	public <T extends smA_ActionArgs> T getActionArgs()
	{
		return (T) m_actionArgs;
	}
	
	public Class<? extends smA_State> getBlockingState()
	{
		if( m_eventType == smE_StateEventType.DID_BACKGROUND )
		{
			return m_blockingOrRevealingState;
		}
		else
		{
			return null;
		}
	}
	
	public Class<? extends smA_State> getRevealingState()
	{
		if( m_eventType == smE_StateEventType.DID_FOREGROUND )
		{
			return m_blockingOrRevealingState;
		}
		else
		{
			return null;
		}
	}
	
	public Class<? extends smA_Action> getAction()
	{
		return m_action.getClass();
	}
	
	public <T extends smA_State> T getState()
	{
		return (T) (m_eventType != smE_StateEventType.DID_PERFORM_ACTION ? m_state : null);
	}
	
	public smStateContext getContext()
	{
		return m_state.getContext();
	}
	
	public smE_StateEventType getType()
	{
		return m_eventType;
	}
	
	void dispatch(smI_StateEventListener listener)
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
			else if( m_eventType != smE_StateEventType.DID_UPDATE )
			{
				//s_logger.info("'" + m_eventType.toString() + " " + m_state.getClass().getName()  + "' dispatching to last listener...");
			}
		}
		
		listener.onStateEvent(this);
	}
}
