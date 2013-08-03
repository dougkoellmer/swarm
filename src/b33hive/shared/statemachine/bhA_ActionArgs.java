package b33hive.shared.statemachine;

public abstract class bhA_ActionArgs
{
	private Object m_userData;
	
	public bhA_ActionArgs()
	{
		m_userData = null;
	}
	
	public bhA_ActionArgs(Object userData)
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
