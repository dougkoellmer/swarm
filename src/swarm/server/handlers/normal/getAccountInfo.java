package swarm.server.handlers.normal;

import swarm.client.structs.smAccountInfo;
import swarm.server.account.smE_Role;
import swarm.server.account.smUserSession;

import swarm.server.session.smSessionManager;
import swarm.server.transaction.smA_DefaultRequestHandler;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class getAccountInfo extends smA_DefaultRequestHandler
{
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		if( !m_serverContext.sessionMngr.isAuthorized(request, response, smE_Role.USER) )
		{
			return;
		}
		
		smUserSession userSession = m_serverContext.sessionMngr.getSession(request, response);
		smAccountInfo accountInfo = new smAccountInfo(userSession.getUsername());
		accountInfo.writeJson(m_serverContext.jsonFactory, response.getJsonArgs());
	}
}
