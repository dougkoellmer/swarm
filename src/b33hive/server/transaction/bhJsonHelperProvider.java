package b33hive.server.transaction;

import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.reflection.bhI_Class;

public class bhJsonHelperProvider implements bhI_Class<bhJsonHelper>
{
	private static bhJsonHelperProvider s_instance = null;
	private final ThreadLocal<bhJsonHelper> m_threadLocal = new ThreadLocal<bhJsonHelper>();
	
	static void startUp()
	{
		s_instance = new bhJsonHelperProvider();
	}
	
	public static bhJsonHelperProvider getInstance()
	{
		return s_instance;
	}
	
	@Override
	public bhJsonHelper newInstance()
	{
		return m_threadLocal.get();
	}
	
	public void startScope(boolean verbose)
	{
		m_threadLocal.set(new bhJsonHelper(verbose));
	}
	
	public void endScope()
	{
		m_threadLocal.remove();
	}
}
