package swarm.shared.statemachine;

public abstract class A_StateConstructor
{
	private final Object m_userData;
	
	public A_StateConstructor()
	{
		m_userData = null;
	}
	
	public A_StateConstructor(Object userData)
	{
		m_userData = userData;
	}
	
	public Object getUserData()
	{
		return m_userData;
	}
}
