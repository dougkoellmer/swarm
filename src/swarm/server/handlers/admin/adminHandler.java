package swarm.server.handlers.admin;

import swarm.server.account.smE_Role;
import swarm.server.account.sm_s;
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
	
	public adminHandler(smI_RequestHandler inner)
	{
		m_inner = inner;
	}
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		if( !sm_s.sessionMngr.isAuthorized(request, response, smE_Role.ADMIN) )
		{
			return;
		}
		
		m_inner.handleRequest(context, request, response);
	}
}
