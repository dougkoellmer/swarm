package b33hive.shared.statemachine;

public abstract class bhA_StateConstructor
{
	private final Object m_userData;
	
	public bhA_StateConstructor()
	{
		m_userData = null;
	}
	
	public bhA_StateConstructor(Object userData)
	{
		m_userData = userData;
	}
	
	public Object getUserData()
	{
		return m_userData;
	}
}
