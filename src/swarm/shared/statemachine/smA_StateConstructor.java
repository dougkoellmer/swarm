package swarm.shared.statemachine;

public abstract class smA_StateConstructor
{
	private final Object m_userData;
	
	public smA_StateConstructor()
	{
		m_userData = null;
	}
	
	public smA_StateConstructor(Object userData)
	{
		m_userData = userData;
	}
	
	public Object getUserData()
	{
		return m_userData;
	}
}
