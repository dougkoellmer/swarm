package swarm.shared.statemachine;

public class EnterOrExitEvent extends LifecycleEvent
{
	private E_TransitionCause m_cause;
	
	void init(A_State state, E_Event eventType, E_TransitionCause cause)
	{
		super.init(state, eventType);
		
		m_cause = cause;
	}
	
	@Override void clean()
	{
		super.clean();
		
		m_cause = null;
	}
	
	public E_TransitionCause getTransitionCause()
	{
		return m_cause;
	}
}
