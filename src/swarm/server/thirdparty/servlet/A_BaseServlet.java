package swarm.server.thirdparty.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import swarm.server.account.ServerAccountManager;
import swarm.server.app.A_ServerApp;
import swarm.server.app.A_ServerJsonFactory;
import swarm.server.app.ServerContext;
import swarm.server.session.SessionManager;


@SuppressWarnings("serial")
public abstract class A_BaseServlet extends HttpServlet
{
	@Override
	public void init()
	{
		
	}
	
	private void startScope()
	{
		ServerContext context = A_ServerApp.getInstance().getContext();
		
		SessionManager sessionMngr = context.sessionMngr;
		sessionMngr.onEnterScope();
		((A_ServerJsonFactory)context.jsonFactory).startScope(true);
	}
	
	private void endScope()
	{
		ServerContext context = A_ServerApp.getInstance().getContext();
		
		SessionManager sessionMngr = context.sessionMngr;
		((A_ServerJsonFactory)context.jsonFactory).endScope();
		sessionMngr.onExitScope();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		startScope();

		try
		{
			doGetOrPost(req, resp, true);
		}
		finally
		{
			endScope();
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		startScope();
		try
		{
			doGetOrPost(req, resp, false);
		}
		finally
		{
			endScope();
		}
	}
	
	protected abstract void doGetOrPost(HttpServletRequest nativeRequest, HttpServletResponse nativeResponse, boolean isGet) throws ServletException, IOException;
}
