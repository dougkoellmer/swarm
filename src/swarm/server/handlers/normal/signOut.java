package swarm.server.handlers.normal;

import swarm.server.account.bhE_Role;
import swarm.server.account.sm_s;
import swarm.server.session.bhSessionManager;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class signOut implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		if( !sm_s.sessionMngr.isAuthorized(request, response, bhE_Role.USER) )
		{
			return;
		}
		
		sm_s.sessionMngr.endSession(request, response);
	}
}
