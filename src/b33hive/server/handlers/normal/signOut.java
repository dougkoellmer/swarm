package b33hive.server.handlers.normal;

import b33hive.server.account.bhE_Role;
import b33hive.server.account.bh_s;
import b33hive.server.session.bhSessionManager;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class signOut implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		if( !bh_s.sessionMngr.isAuthorized(request, response, bhE_Role.USER) )
		{
			return;
		}
		
		bh_s.sessionMngr.endSession(request, response);
	}
}
