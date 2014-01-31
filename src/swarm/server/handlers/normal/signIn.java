package swarm.server.handlers.normal;

import swarm.server.account.ServerAccountManager;
import swarm.server.account.UserSession;

import swarm.server.session.SessionManager;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.account.SignInCredentials;
import swarm.shared.account.SignInValidationResult;
import swarm.shared.account.SignInValidator;
import swarm.shared.app.BaseAppContext;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.JsonHelper;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class signIn extends A_DefaultRequestHandler
{
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		SignInCredentials creds = new SignInCredentials(m_serverContext.jsonFactory, request.getJsonArgs());
		SignInValidationResult result = new SignInValidationResult();
		String passwordChangeToken = m_serverContext.jsonFactory.getHelper().getString(request.getJsonArgs(), E_JsonKey.passwordChangeToken);
		
		UserSession userSession = null;
		
		if( passwordChangeToken != null )
		{
			userSession = m_serverContext.accountMngr.confirmNewPassword(creds, passwordChangeToken, result);
		}
		else
		{
			userSession = m_serverContext.accountMngr.attemptSignIn(creds, result);
		}
		
		if( userSession != null )
		{
			m_serverContext.sessionMngr.startSession(userSession, response, creds.rememberMe());
		}
		
		result.writeJson(m_serverContext.jsonFactory, response.getJsonArgs());
	}
}
