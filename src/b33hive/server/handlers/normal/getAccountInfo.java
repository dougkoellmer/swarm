package b33hive.server.handlers.normal;

import b33hive.client.structs.bhAccountInfo;
import b33hive.server.account.bhE_Role;
import b33hive.server.account.bhUserSession;
import b33hive.server.account.bh_s;
import b33hive.server.session.bhSessionManager;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class getAccountInfo implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		if( !bh_s.sessionMngr.isAuthorized(request, response, bhE_Role.USER) )
		{
			return;
		}
		
		bhUserSession userSession = bh_s.sessionMngr.getSession(request, response);
		bhAccountInfo accountInfo = new bhAccountInfo(userSession.getUsername());
		accountInfo.writeJson(response.getJson());
	}
}
