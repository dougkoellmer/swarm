package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */
public class ForegroundEvent extends LifecycleEvent
{
	private Class<? extends A_State> m_revealingState;
	private StateArgs m_args;
	
	void init(A_State state, Class<? extends A_State> revealingState, StateArgs args)
	{
		super.init(state, E_Event.DID_FOREGROUND);
		
		m_revealingState = revealingState;
		m_args = A_StateContextForwarder.defaultArgs(args);
	}
	
	@Override void clean()
	{
		super.clean();
		
		m_revealingState = null;
		m_args = StateArgs.DEFAULT;
	}
	
	public Class<? extends A_State> getRevealingState()
	{
		return m_revealingState;
	}
	
	@Override public <T extends StateArgs> T getArgs()
	{
		return (T) m_args;
	}
	
	@Override public <T extends Object> T getArg(int index)
	{
		return m_args.get(index);
	}

	@Override public <T extends Object> T getArg()
	{
		return m_args.get();
	}
	
	@Override public <T extends Object> T getArg(Class<T> paramType)
	{
		return m_args.get(paramType);
	}
	
	@Override public <T extends Object> T getArg(Class<T> paramType, int index)
	{
		return m_args.get(paramType, index);
	}
}
