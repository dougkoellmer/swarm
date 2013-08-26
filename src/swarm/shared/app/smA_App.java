package swarm.shared.app;

import swarm.shared.smE_AppEnvironment;

public abstract class smA_App
{	
	protected static smA_App s_instance = null;
	
	private final smE_AppEnvironment m_environment;
	
	protected smA_App(smE_AppEnvironment environment)
	{
		m_environment = environment;
		s_instance = this;
	}
	
	public bhE_AppEnvironment getEnvironment()
	{
		return m_environment;
	}
	
	public static smA_App getInstance()
	{
		return s_instance;
	}
}
