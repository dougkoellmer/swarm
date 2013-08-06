package b33hive.server.handlers.admin;

import b33hive.server.account.bhE_Role;
import b33hive.server.account.bh_s;
import b33hive.server.session.bhSessionManager;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.transaction.bhE_HttpMethod;
import b33hive.shared.transaction.bhI_RequestPath;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

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
		if( !bh_s.sessionMngr.isAuthorized(request, response, bhE_Role.ADMIN) )
		{
			return;
		}
		
		m_inner.handleRequest(context, request, response);
	}
}
