package com.b33hive.shared.statemachine;

import java.util.logging.Logger;

import com.b33hive.shared.debugging.bhU_Debug;

public class bhStateEvent
{
	private static final Logger s_logger = Logger.getLogger(bhStateEvent.class.getName());
	
	static final int UNINITIALIZED_LISTENER_INDEX = Integer.MIN_VALUE;
	
	private bhE_StateEventType m_eventType = null;
	bhA_State m_state = null;
	bhA_Action m_action = null; // we keep an instance, but only ever expose the class publicly.
	bhA_ActionArgs m_actionArgs = null;
	private Class<? extends bhA_State> m_blockingOrRevealingState;
	
	int m_listenerIndex = UNINITIALIZED_LISTENER_INDEX;
	
	bhStateEvent(bhE_StateEventType eventType, bhA_State state)
	{
		m_eventType = eventType;
		m_state = state;
	}
	
	bhStateEvent(bhA_Action action, bhA_ActionArgs args)
	{
		m_actionArgs = args;
		m_state = action.getState();
		m_action = action;
		m_eventType = bhE_StateEventType.DID_PERFORM_ACTION;
	}
	
	bhStateEvent(bhE_StateEventType eventType, bhA_State state, Class<? extends bhA_State> blockingOrRevealingState)
	{
		m_eventType = eventType;
		m_state = state;
		m_blockingOrRevealingState = blockingOrRevealingState;
	}
	
	public <T extends bhA_ActionArgs> T getActionArgs()
	{
		return (T) m_actionArgs;
	}
	
	public Class<? extends bhA_State> getBlockingState()
	{
		if( m_eventType == bhE_StateEventType.DID_BACKGROUND )
		{
			return m_blockingOrRevealingState;
		}
		else
		{
			return null;
		}
	}
	
	public Class<? extends bhA_State> getRevealingState()
	{
		if( m_eventType == bhE_StateEventType.DID_FOREGROUND )
		{
			return m_blockingOrRevealingState;
		}
		else
		{
			return null;
		}
	}
	
	public Class<? extends bhA_Action> getAction()
	{
		return m_action.getClass();
	}
	
	public <T extends bhA_State> T getState()
	{
		return (T) (m_eventType != bhE_StateEventType.DID_PERFORM_ACTION ? m_state : null);
	}
	
	public bhE_StateEventType getType()
	{
		return m_eventType;
	}
	
	void dispatch(bhI_StateEventListener listener)
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
			else if( m_eventType != bhE_StateEventType.DID_UPDATE )
			{
				//s_logger.info("'" + m_eventType.toString() + " " + m_state.getClass().getName()  + "' dispatching to last listener...");
			}
		}
		
		listener.onStateEvent(this);
	}
}
