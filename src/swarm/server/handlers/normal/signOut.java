package swarm.server.handlers.normal;

import swarm.server.account.smE_Role;

import swarm.server.session.smSessionManager;
import swarm.server.transaction.smA_DefaultRequestHandler;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class signOut extends smA_DefaultRequestHandler
{
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		if( !m_context.sessionMngr.isAuthorized(request, response, smE_Role.USER) )
		{
			return;
		}
		
		m_context.sessionMngr.endSession(request, response);
	}
}
