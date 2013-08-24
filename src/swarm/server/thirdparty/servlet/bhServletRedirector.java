package swarm.server.thirdparty.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import swarm.server.app.bhI_RequestRedirector;

public class bhServletRedirector implements bhI_RequestRedirector
{
	private final String m_mainPage;
	
	public bhServletRedirector(String mainPage)
	{
		m_mainPage = mainPage;
	}
	
	@Override
	public void redirectToMainPage(Object nativeResponse) throws IOException
	{
		((HttpServletResponse)nativeResponse).sendRedirect(m_mainPage);
	}
}
