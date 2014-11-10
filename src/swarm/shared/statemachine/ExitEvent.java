package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */
public class ExitEvent extends EnterOrExitEvent
{
	private Class<? extends A_State> m_parent;
	
	void init(A_State state, E_TransitionCause cause)
	{
		super.init(state, E_Event.DID_EXIT, cause);
		
		m_parent = super.getStateParentClass();
	}
	
	@Override void clean()
	{
		super.clean();
		
		m_parent = null;
	}
	
	@Override public Class<? extends A_State> getStateParentClass()
	{
		return m_parent;
	}
}
