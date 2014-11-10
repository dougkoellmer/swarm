package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */
public class LifecycleEvent extends A_BaseStateEvent
{
	private A_State m_state;
	
	void init(A_State state, E_Event eventType)
	{
		super.init(state.getContext(), eventType);
		
		m_state = state;
	}
	
	@Override void clean()
	{
		super.clean();
		
		m_state = null;
	}
	
	@Override public <T extends A_State> T getState()
	{
		return (T) m_state;
	}

	@Override public Class<? extends A_State> getStateClass()
	{
		return m_state.getClass();
	}
	
	public Class<? extends A_State> getStateParentClass()
	{
		return m_state.getParent() != null ? m_state.getParent().getClass() : null;
	}

	
	@Override public Class<? extends A_BaseStateObject> getTargetClass()
	{
		return getStateClass();
	}
	
	@Override public <T extends StateArgs> T getArgs()
	{
		return m_state.getArgs();
	}
	
	@Override public <T extends Object> T getArg(int index)
	{
		return m_state.getArg(index);
	}

	@Override public <T extends Object> T getArg()
	{
		return m_state.getArg();
	}
	
	@Override public <T extends Object> T getArg(Class<T> paramType)
	{
		return m_state.getArg(paramType);
	}
	
	@Override public <T extends Object> T getArg(Class<T> paramType, int index)
	{
		return m_state.getArg(paramType, index);
	}
	
	@Override public String toString()
	{
		return getType() + " " + getStateClass().getName();
	}
}
