package com.b33hive.server.handlers;

import com.b33hive.server.account.bhServerAccountManager;
import com.b33hive.server.account.bhUserSession;
import com.b33hive.server.session.bhSessionManager;
import com.b33hive.server.transaction.bhI_RequestHandler;
import com.b33hive.server.transaction.bhTransactionContext;
import com.b33hive.shared.account.bhSignInCredentials;
import com.b33hive.shared.account.bhSignInValidationResult;
import com.b33hive.shared.account.bhSignInValidator;
import com.b33hive.shared.json.bhE_JsonKey;
import com.b33hive.shared.json.bhJsonHelper;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

public class signIn implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhSignInCredentials creds = new bhSignInCredentials(request.getJson());
		
		bhSignInValidationResult result = bhSignInValidator.getInstance().validate(creds);
		
		if( result.isEverythingOk() )
		{
			bhServerAccountManager accountManager = bhServerAccountManager.getInstance();
			
			String passwordResetToken = bhJsonHelper.getInstance().getString(request.getJson(), bhE_JsonKey.passwordChangeToken);
			
			bhUserSession userSession = null;
			
			if( passwordResetToken != null )
			{
				userSession = accountManager.confirmNewPassword(result, creds, passwordResetToken);
			}
			else
			{
				userSession = accountManager.attemptSignIn(result, creds);
			}
			
			if( result.isEverythingOk/*Still?*/() )
			{
				bhSessionManager.getInstance().startSession(userSession, response, creds.rememberMe());
			}
		}
		
		result.writeJson(response.getJson());
	}
}
