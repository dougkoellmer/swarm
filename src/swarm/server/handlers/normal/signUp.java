package swarm.server.handlers.normal;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaResponse;

import swarm.server.account.ServerAccountManager;
import swarm.server.account.UserSession;

import swarm.server.session.SessionManager;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.account.E_SignUpCredentialType;
import swarm.shared.account.E_SignUpValidationError;
import swarm.shared.account.SignUpCredentials;
import swarm.shared.account.SignUpValidationResult;
import swarm.shared.account.SignUpValidator;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class signUp extends A_DefaultRequestHandler
{
	private final String m_publicRecaptchaKey;
	private final String m_privateRecaptchaKey;
	
	public signUp(String publicRecaptchaKey, String privateRecaptchaKey)
	{
		m_publicRecaptchaKey = publicRecaptchaKey;
		m_privateRecaptchaKey = privateRecaptchaKey;
	}
	
	private boolean isCaptchaValid(SignUpCredentials creds, SignUpValidationResult result_out, String remoteAddress)
	{
		String captchaChallenge = creds.getCaptchaChallenge();
		String captchaResponse = creds.get(E_SignUpCredentialType.CAPTCHA_RESPONSE);
		
		ReCaptcha captcha = ReCaptchaFactory.newReCaptcha(m_publicRecaptchaKey, m_privateRecaptchaKey, false);
        ReCaptchaResponse recaptchaResponse = captcha.checkAnswer(remoteAddress, captchaChallenge, captchaResponse);

        if ( !recaptchaResponse.isValid())
        {
        	result_out.setNoErrors(); // just make sure all errors are filled in for json writing...kinda hacky.
        	result_out.setError(E_SignUpCredentialType.CAPTCHA_RESPONSE, E_SignUpValidationError.CAPTCHA_INCORRECT);
        	
        	return false;
        }
        
        return true;
	}
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		ServerAccountManager accountManager = m_serverContext.accountMngr;
		SignUpCredentials creds = new SignUpCredentials(m_serverContext.jsonFactory, request.getJsonArgs());
		SignUpValidationResult result = new SignUpValidationResult();
		String remoteAddress = ((HttpServletRequest) request.getNativeRequest()).getRemoteAddr();
		
		if( isCaptchaValid(creds, result, remoteAddress))
		{
			UserSession userSession = accountManager.attemptSignUp(creds, result);
			
			if( userSession != null )
			{
				m_serverContext.sessionMngr.startSession(userSession, response, creds.rememberMe());
			}
		}
		
		result.writeJson(response.getJsonArgs(), m_serverContext.jsonFactory);
	}
}
