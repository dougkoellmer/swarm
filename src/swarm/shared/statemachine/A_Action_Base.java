package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */
public abstract class A_Action_Base extends A_BaseStateObject
{
	A_State m_state = null;
	Class<? extends A_State> m_association;
	
	protected A_Action_Base()
	{
	}

	public boolean isPerformableInBackground()
	{
		return false;
	}
	
	public boolean suppressLog()
	{
		return false;
	}
	
	
	public StateArgs prePerform(StateArgs args)
	{
		return args;
	}

	public Class<? extends A_State> getStateAssociation()
	{
		return m_association;
	}
	
	public boolean isAssociatedWithState(Class<? extends A_State> stateClass)
	{
		return stateClass == this.getStateAssociation();
	}

	public boolean isPerformable(StateArgs args)
	{
		return true;
	}

	protected <T extends A_State> T getState()
	{
		return (T) m_state;
	}
}
