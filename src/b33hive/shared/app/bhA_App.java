package b33hive.shared.app;

import b33hive.shared.bhE_AppEnvironment;

public abstract class bhA_App
{	
	protected static bhA_App s_instance = null;
	
	private final bhE_AppEnvironment m_environment;
	
	protected bhA_App(bhE_AppEnvironment environment)
	{
		m_environment = environment;
		s_instance = this;
	}
	
	public bhE_AppEnvironment getEnvironment()
	{
		return m_environment;
	}
	
	public static bhA_App getInstance()
	{
		return s_instance;
	}
}
