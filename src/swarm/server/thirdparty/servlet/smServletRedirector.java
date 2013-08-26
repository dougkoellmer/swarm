package swarm.server.thirdparty.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import swarm.server.app.smI_RequestRedirector;

public class smServletRedirector implements smI_RequestRedirector
{
	private final String m_mainPage;
	
	public smServletRedirector(String mainPage)
	{
		m_mainPage = mainPage;
	}
	
	@Override
	public void redirectToMainPage(Object nativeResponse) throws IOException
	{
		((HttpServletResponse)nativeResponse).sendRedirect(m_mainPage);
	}
}
