package b33hive.server.app;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class bhServletContextListener implements ServletContextListener
{
	ServletContext m_context;
	
	public bhServletContextListener()
	{
		
	}
	
	@Override
	public void contextInitialized(ServletContextEvent contextEvent)
	{
		m_context = contextEvent.getServletContext();
		
		bhServerApp.entryPoint();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent contextEvent)
	{
		m_context = contextEvent.getServletContext();
	}
}