package swarm.shared.app;

import swarm.shared.smE_AppEnvironment;

public abstract class smA_App
{	
	private final smE_AppEnvironment m_environment;
	
	protected smA_App(smE_AppEnvironment environment)
	{
		m_environment = environment;
	}
	
	public smE_AppEnvironment getEnvironment()
	{
		return m_environment;
	}
}
