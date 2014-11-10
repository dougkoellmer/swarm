package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */
public class ActionEvent extends A_BaseStateEvent
{
	private A_Action_Base m_action;
	private A_State	m_state;
	
	private StateArgs m_args_in = StateArgs.DEFAULT;
	private StateArgs m_args_returned = StateArgs.DEFAULT;
	
	void init(A_State state, A_Action_Base action)
	{
		super.init(state.getContext(), E_Event.DID_PERFORM_ACTION);
		
		m_action = action;
		m_state = state;
	}
	
	@Override void clean()
	{
		super.clean();
		
		m_action = null;
		m_state = null;
		m_args_in = StateArgs.DEFAULT;
		m_args_returned = StateArgs.DEFAULT;
	}
	
	@Override public <T extends A_State> T getState()
	{
		return (T) m_state;
	}
	
	void setArgsIn(StateArgs args)
	{
		m_args_in = args;
	}

	void setReturnedArgs(StateArgs args)
	{
		m_args_returned = args;
	}
	
	public <T extends StateArgs> T getArgsIn()
	{
		return (T) defaultArgs(m_args_in);
	}
	
	public <T extends StateArgs> T getArgsReturned()
	{
		return (T) defaultArgs(m_args_returned);
	}
	
	public Class<? extends A_Action_Base> getActionClass()
	{
		return m_action.getClass();
	}
	
	@Override public Class<? extends A_State> getStateClass()
	{
		return m_action.getStateAssociation();
	}
	
	@Override public Class<? extends A_BaseStateObject> getTargetClass()
	{
		return getActionClass();
	}
	
	@Override public <T extends StateArgs> T getArgs()
	{
		StateArgs args = m_args_in;
		
		if( args.count() == 0 )
		{
			args = m_args_returned;
		}
		
		return (T) args;
	}
	
	@Override public <T extends Object> T getArg(int index)
	{
		Object arg = (m_args_in.get(index) != null ? m_args_in.get(index) : m_args_returned.get(index));
		
		if( arg /*still*/== null )
		{
			arg = m_state.getArg(index);
		}
		
		return (T) arg;
	}

	@Override public <T extends Object> T getArg()
	{
		Object arg = (m_args_in.get() != null ? m_args_in.get() : m_args_returned.get());
		
		if( arg /*still*/== null )
		{
			arg = m_state.getArg();
		}
		
		return (T) arg;
	}
	
	@Override public <T extends Object> T getArg(Class<T> paramType)
	{
		Object arg = (m_args_in.get(paramType) != null ? m_args_in.get(paramType) : m_args_returned.get(paramType));
		
		if( arg /*still*/== null )
		{
			arg = m_state.getArg(paramType);
		}
		
		return (T) arg;
	}
	
	@Override public <T extends Object> T getArg(Class<T> paramType, int index)
	{
		Object arg = (T) (m_args_in.get(paramType, index) != null ? m_args_in.get(paramType, index) : m_args_returned.get(paramType, index));
		
		if( arg /*still*/== null )
		{
			arg = m_state.getArg(paramType, index);
		}
		
		return (T) arg;
	}
	
	@Override public String toString()
	{
		return getType() + " " + getActionClass().getName() + m_args_in.toString();
	}
}
