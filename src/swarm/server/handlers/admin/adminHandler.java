package swarm.server.handlers.admin;

import swarm.server.account.bhE_Role;
import swarm.server.account.sm_s;
import swarm.server.session.bhSessionManager;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.transaction.bhE_HttpMethod;
import swarm.shared.transaction.bhI_RequestPath;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class adminHandler implements bhI_RequestHandler
{
	private final bhI_RequestHandler m_inner;
	
	public adminHandler(bhI_RequestHandler inner)
	{
		m_inner = inner;
	}
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		if( !sm_s.sessionMngr.isAuthorized(request, response, bhE_Role.ADMIN) )
		{
			return;
		}
		
		m_inner.handleRequest(context, request, response);
	}
}
