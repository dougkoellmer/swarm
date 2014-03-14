package swarm.shared.statemachine;

/**
 * ...
 * @author 
 */
public abstract class A_Action extends A_BaseStateObject
{
	A_State m_state = null;
	Class<? extends A_State> m_association;
	
	protected A_Action()
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
	
	public /*virtual*/ void prePerform(StateArgs args)
	{
		
	}

	public abstract void perform(StateArgs args);

	public Class<? extends A_State> getStateAssociation()
	{
		return m_association;
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