package swarm.server.handlers.normal;

import swarm.client.structs.AccountInfo;
import swarm.server.account.E_Role;
import swarm.server.account.UserSession;

import swarm.server.session.SessionManager;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class getAccountInfo extends A_DefaultRequestHandler
{
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		if( !m_serverContext.sessionMngr.isAuthorized(request, response, E_Role.USER) )
		{
			return;
		}
		
		UserSession userSession = m_serverContext.sessionMngr.getSession(request, response);
		AccountInfo accountInfo = new AccountInfo(userSession.getUsername());
		accountInfo.writeJson(response.getJsonArgs(), m_serverContext.jsonFactory);
	}
}
