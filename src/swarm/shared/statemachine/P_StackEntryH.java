package swarm.shared.statemachine;

class P_StackEntryH
{
	StateArgs m_args;
	Class<? extends A_State> m_stateClass;
	
	void init(Class<? extends A_State> stateClass, StateArgs args)
	{
		m_stateClass = stateClass;
		m_args = A_StateContextForwarder.defaultArgs(args);
	}
	
	void clean()
	{
		m_stateClass = null;
		m_args = StateArgs.DEFAULT;
	}
}
