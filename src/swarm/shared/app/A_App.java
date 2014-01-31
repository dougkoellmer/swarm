package swarm.shared.app;

import swarm.shared.E_AppEnvironment;

public abstract class A_App
{	
	private final E_AppEnvironment m_environment;
	
	protected A_App(E_AppEnvironment environment)
	{
		m_environment = environment;
	}
	
	public E_AppEnvironment getEnvironment()
	{
		return m_environment;
	}
}
