package b33hive.server.handlers.normal;

import b33hive.server.account.bhServerAccountManager;
import b33hive.server.account.bhUserSession;
import b33hive.server.session.bhSessionManager;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.account.bhSignInCredentials;
import b33hive.shared.account.bhSignInValidationResult;
import b33hive.shared.account.bhSignInValidator;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

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
