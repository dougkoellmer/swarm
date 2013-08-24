package swarm.server.handlers.normal;

import swarm.client.structs.bhAccountInfo;
import swarm.server.account.bhE_Role;
import swarm.server.account.bhUserSession;
import swarm.server.account.sm_s;
import swarm.server.session.bhSessionManager;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class getAccountInfo implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		if( !sm_s.sessionMngr.isAuthorized(request, response, bhE_Role.USER) )
		{
			return;
		}
		
		bhUserSession userSession = sm_s.sessionMngr.getSession(request, response);
		bhAccountInfo accountInfo = new bhAccountInfo(userSession.getUsername());
		accountInfo.writeJson(response.getJson());
	}
}
