package swarm.server.thirdparty.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import swarm.server.app.I_RequestRedirector;

public class ServletRedirector implements I_RequestRedirector
{
	private final String m_mainPage;
	
	public ServletRedirector(String mainPage)
	{
		m_mainPage = mainPage;
	}
	
	@Override
	public void redirectToMainPage(Object nativeResponse) throws IOException
	{
		((HttpServletResponse)nativeResponse).sendRedirect(m_mainPage);
	}
}
