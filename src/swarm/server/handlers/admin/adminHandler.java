package swarm.server.handlers.admin;

import swarm.server.account.E_Role;

import swarm.server.session.SessionManager;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.transaction.E_HttpMethod;
import swarm.shared.transaction.I_RequestPath;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class adminHandler implements I_RequestHandler
{
	private final I_RequestHandler m_inner;
	private final SessionManager m_sessionMngr;
	
	public adminHandler(SessionManager sessionMngr, I_RequestHandler inner)
	{
		m_sessionMngr = sessionMngr;
		m_inner = inner;
	}
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		if( !m_sessionMngr.isAuthorized(request, response, E_Role.ADMIN) )
		{
			return;
		}
		
		m_inner.handleRequest(context, request, response);
	}
}
