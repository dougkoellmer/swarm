package com.b33hive.server.handlers;

import com.b33hive.server.account.bhE_Role;
import com.b33hive.server.session.bhSessionManager;
import com.b33hive.server.transaction.bhI_RequestHandler;
import com.b33hive.server.transaction.bhTransactionContext;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

public class signOut implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		if( !bhSessionManager.getInstance().isAuthorized(request, response, bhE_Role.USER) )
		{
			return;
		}
		
		bhSessionManager.getInstance().endSession(request, response);
	}
}
