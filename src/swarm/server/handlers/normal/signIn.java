package swarm.server.handlers.normal;

import swarm.server.account.bhServerAccountManager;
import swarm.server.account.bhUserSession;
import swarm.server.account.sm_s;
import swarm.server.session.bhSessionManager;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.account.bhSignInCredentials;
import swarm.shared.account.bhSignInValidationResult;
import swarm.shared.account.bhSignInValidator;
import swarm.shared.app.sm;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class signIn implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhSignInCredentials creds = new bhSignInCredentials(request.getJson());
		bhSignInValidationResult result = new bhSignInValidationResult();
		String passwordChangeToken = sm.jsonFactory.getHelper().getString(request.getJson(), bhE_JsonKey.passwordChangeToken);
		
		bhUserSession userSession = null;
		
		if( passwordChangeToken != null )
		{
			userSession = sm_s.accountMngr.confirmNewPassword(creds, passwordChangeToken, result);
		}
		else
		{
			userSession = sm_s.accountMngr.attemptSignIn(creds, result);
		}
		
		if( userSession != null )
		{
			sm_s.sessionMngr.startSession(userSession, response, creds.rememberMe());
		}
		
		result.writeJson(response.getJson());
	}
}
