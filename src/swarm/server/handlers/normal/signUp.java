package swarm.server.handlers.normal;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaResponse;

import swarm.server.account.smServerAccountManager;
import swarm.server.account.smUserSession;
import swarm.server.account.sm_s;
import swarm.server.session.smSessionManager;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.account.smE_SignUpCredentialType;
import swarm.shared.account.smE_SignUpValidationError;
import swarm.shared.account.smSignUpCredentials;
import swarm.shared.account.smSignUpValidationResult;
import swarm.shared.account.smSignUpValidator;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class signUp implements smI_RequestHandler
{
	private final String m_publicRecaptchaKey;
	private final String m_privateRecaptchaKey;
	
	public signUp(String publicRecaptchaKey, String privateRecaptchaKey)
	{
		m_publicRecaptchaKey = publicRecaptchaKey;
		m_privateRecaptchaKey = privateRecaptchaKey;
	}
	
	private boolean isCaptchaValid(smSignUpCredentials creds, smSignUpValidationResult result_out, String remoteAddress)
	{
		String captchaChallenge = creds.getCaptchaChallenge();
		String captchaResponse = creds.get(smE_SignUpCredentialType.CAPTCHA_RESPONSE);
		
		ReCaptcha captcha = ReCaptchaFactory.newReCaptcha(m_publicRecaptchaKey, m_privateRecaptchaKey, false);
        ReCaptchaResponse recaptchaResponse = captcha.checkAnswer(remoteAddress, captchaChallenge, captchaResponse);

        if ( !recaptchaResponse.isValid())
        {
        	result_out.setNoErrors(); // just make sure all errors are filled in for json writing...kinda hacky.
        	result_out.setError(smE_SignUpCredentialType.CAPTCHA_RESPONSE, smE_SignUpValidationError.CAPTCHA_INCORRECT);
        	
        	return false;
        }
        
        return true;
	}
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		smServerAccountManager accountManager = sm_s.accountMngr;
		smSignUpCredentials creds = new smSignUpCredentials(request.getJson());
		smSignUpValidationResult result = new smSignUpValidationResult();
		String remoteAddress = ((HttpServletRequest) request.getNativeRequest()).getRemoteAddr();
		
		if( isCaptchaValid(creds, result, remoteAddress))
		{
			smUserSession userSession = accountManager.attemptSignUp(creds, result);
			
			if( userSession != null )
			{
				sm_s.sessionMngr.startSession(userSession, response, creds.rememberMe());
			}
		}
		
		result.writeJson(response.getJson());
	}
}
