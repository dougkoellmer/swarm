package swarm.shared.statemachine;

import java.util.logging.Logger;

/**
 * 
 * @author dougkoellmer
 */
public abstract class A_BaseStateEvent extends A_StateContextForwarder implements I_StateArgForwarder
{
	private static final Logger s_logger = Logger.getLogger(A_BaseStateEvent.class.getName());
	
	static final int UNINITIALIZED_LISTENER_INDEX = Integer.MIN_VALUE;
	
	private E_Event m_eventType;
	private StateContext m_context;
	
	int m_listenerIndex = UNINITIALIZED_LISTENER_INDEX;

	void init(StateContext context, E_Event eventType)
	{
		m_context = context;
		m_eventType = eventType;
	}
	
	void clean()
	{
		m_context = null;
		m_eventType = null;
		m_listenerIndex = UNINITIALIZED_LISTENER_INDEX;
	}
	
	public abstract Class<? extends A_State> getStateClass();
	
	public abstract Class<? extends A_BaseStateObject> getTargetClass();
	
	public StateContext getContext()
	{
		return m_context;
	}
	
	@Override StateContext getContext_internal()
	{
		return getContext();
	}
	
	public E_Event getType()
	{
		return m_eventType;
	}
	
	public <T extends A_State> T getState()
	{
		return null;
	}
	
	public <T extends A_BaseStateEvent> T cast()
	{
		return (T) this;
	}
	
	
	void dispatch(I_StateEventListener listener)
	{
		if( m_listenerIndex == -1 )
		{
//			if( m_action != null )
//			{
//				if( !m_action.suppressLog() )
//				{
//					//s_logger.info("'Performed " + m_action.getClass().getName() + "' dispatching to last listener...");
//				}
//			}
//			else if( m_eventType != E_Event.DID_UPDATE )
//			{
//				//s_logger.info("'" + m_eventType.toString() + " " + m_state.getClass().getName()  + "' dispatching to last listener...");
//			}
		}
		
		listener.onStateEvent(this);
	}
	
	
	public boolean isFor(E_Event type)
	{
		return type == this.getType();
	}

	public boolean isFor(Class<? extends A_BaseStateObject> stateObject)
	{
		if( stateObject == this.getTargetClass() )
		{
			return true;
		}
		
		return false;
	}
	
	public boolean isForArg(Object arg )
	{
		return isForArg(null, arg);
	}
	
	public boolean isForArg(Class<? extends A_BaseStateObject> stateObject, Object arg )
	{
		return (stateObject == null || isFor(stateObject)) && getArgs().contains(arg);
	}
	
	public boolean isForArgs(Class<? extends A_BaseStateObject> stateObject, Object ... args )
	{
		if( stateObject == null || isFor(stateObject) )
		{
			if( getArgs().containsAll(args) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isForArgs(Object ... args )
	{
		return isForArgs((Class<? extends A_BaseStateObject>)null, args);
	}
	
	public boolean isFor(Class<? extends A_BaseStateObject> stateObject, E_Event ... types)
	{
		if( !isFor(stateObject) )  return false;
		
		for( int i = 0; i < types.length; i++ )
		{
			if( isFor(types[i]) )  return true;
		}
		
		return false;
	}
	
	public boolean isFor(E_Event type, Class<? extends A_BaseStateObject> ... stateObjects)
	{
		if( !isFor(type) )  return false;
		
		for( int i = 0; i < stateObjects.length; i++ )
		{
			if( isFor(stateObjects[i]) )  return true;
		}
		
		return false;
	}
	
	public boolean isFor(E_Event ... types)
	{
		for( int i = 0; i < types.length; i++ )
		{
			if( isFor(types[i]) )  return true;
		}
		
		return false;
	}
	
	public boolean isFor(Class<? extends A_BaseStateObject> ... stateObjects)
	{
		for( int i = 0; i < stateObjects.length; i++ )
		{
			if( isFor(stateObjects[i]) )  return true;
		}
		
		return false;
	}
	
	public boolean isFor(E_Event type, Class<? extends A_BaseStateObject> stateObject)
	{
		return isFor(stateObject, type);
	}
	
	public boolean isFor(Class<? extends A_BaseStateObject> stateObject, E_Event type)
	{
		return isFor(stateObject) && isFor(type);
	}
}
