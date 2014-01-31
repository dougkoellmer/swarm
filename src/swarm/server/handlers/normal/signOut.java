package swarm.server.handlers.normal;

import swarm.server.account.E_Role;

import swarm.server.session.SessionManager;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class signOut extends A_DefaultRequestHandler
{
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		if( !m_serverContext.sessionMngr.isAuthorized(request, response, E_Role.USER) )
		{
			return;
		}
		
		m_serverContext.sessionMngr.endSession(request, response);
	}
}
