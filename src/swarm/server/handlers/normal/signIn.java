package swarm.server.handlers.normal;

import swarm.server.account.smServerAccountManager;
import swarm.server.account.smUserSession;
import swarm.server.account.sm_s;
import swarm.server.session.smSessionManager;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.account.smSignInCredentials;
import swarm.shared.account.smSignInValidationResult;
import swarm.shared.account.smSignInValidator;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smJsonHelper;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class signIn implements smI_RequestHandler
{
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		smSignInCredentials creds = new smSignInCredentials(request.getJsonArgs());
		smSignInValidationResult result = new smSignInValidationResult();
		String passwordChangeToken = factory.getHelper().getString(request.getJsonArgs(), smE_JsonKey.passwordChangeToken);
		
		smUserSession userSession = null;
		
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
		
		result.writeJson(null, response.getJsonArgs());
	}
}
