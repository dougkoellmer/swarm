package swarm.server.handlers.admin;

import swarm.server.account.smE_Role;

import swarm.server.session.smSessionManager;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.transaction.smE_HttpMethod;
import swarm.shared.transaction.smI_RequestPath;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class adminHandler implements smI_RequestHandler
{
	private final smI_RequestHandler m_inner;
	private final smSessionManager m_sessionMngr;
	
	public adminHandler(smSessionManager sessionMngr, smI_RequestHandler inner)
	{
		m_sessionMngr = sessionMngr;
		m_inner = inner;
	}
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		if( !m_sessionMngr.isAuthorized(request, response, smE_Role.ADMIN) )
		{
			return;
		}
		
		m_inner.handleRequest(context, request, response);
	}
}
