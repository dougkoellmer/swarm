package swarm.server.handlers.normal;

import swarm.server.account.smE_Role;
import swarm.server.account.sm_s;
import swarm.server.session.smSessionManager;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class signOut implements smI_RequestHandler
{
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		if( !sm_s.sessionMngr.isAuthorized(request, response, smE_Role.USER) )
		{
			return;
		}
		
		sm_s.sessionMngr.endSession(request, response);
	}
}
