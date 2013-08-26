package swarm.shared.statemachine;

public abstract class smA_ActionArgs
{
	private Object m_userData;
	
	public smA_ActionArgs()
	{
		m_userData = null;
	}
	
	public smA_ActionArgs(Object userData)
	{
		m_userData = userData;
	}
	
	public Object getUserData()
	{
		return m_userData;
	}
	
	public void setUserData(Object userData)
	{
		m_userData = userData;
	}
}
