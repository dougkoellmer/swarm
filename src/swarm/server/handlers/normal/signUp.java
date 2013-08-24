package swarm.server.handlers.normal;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaResponse;

import swarm.server.account.bhServerAccountManager;
import swarm.server.account.bhUserSession;
import swarm.server.account.sm_s;
import swarm.server.session.bhSessionManager;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.account.bhE_SignUpCredentialType;
import swarm.shared.account.bhE_SignUpValidationError;
import swarm.shared.account.bhSignUpCredentials;
import swarm.shared.account.bhSignUpValidationResult;
import swarm.shared.account.bhSignUpValidator;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class signUp implements bhI_RequestHandler
{
	private final String m_publicRecaptchaKey;
	private final String m_privateRecaptchaKey;
	
	public signUp(String publicRecaptchaKey, String privateRecaptchaKey)
	{
		m_publicRecaptchaKey = publicRecaptchaKey;
		m_privateRecaptchaKey = privateRecaptchaKey;
	}
	
	private boolean isCaptchaValid(bhSignUpCredentials creds, bhSignUpValidationResult result_out, String remoteAddress)
	{
		String captchaChallenge = creds.getCaptchaChallenge();
		String captchaResponse = creds.get(bhE_SignUpCredentialType.CAPTCHA_RESPONSE);
		
		ReCaptcha captcha = ReCaptchaFactory.newReCaptcha(m_publicRecaptchaKey, m_privateRecaptchaKey, false);
        ReCaptchaResponse recaptchaResponse = captcha.checkAnswer(remoteAddress, captchaChallenge, captchaResponse);

        if ( !recaptchaResponse.isValid())
        {
        	result_out.setNoErrors(); // just make sure all errors are filled in for json writing...kinda hacky.
        	result_out.setError(bhE_SignUpCredentialType.CAPTCHA_RESPONSE, bhE_SignUpValidationError.CAPTCHA_INCORRECT);
        	
        	return false;
        }
        
        return true;
	}
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhServerAccountManager accountManager = sm_s.accountMngr;
		bhSignUpCredentials creds = new bhSignUpCredentials(request.getJson());
		bhSignUpValidationResult result = new bhSignUpValidationResult();
		String remoteAddress = ((HttpServletRequest) request.getNativeRequest()).getRemoteAddr();
		
		if( isCaptchaValid(creds, result, remoteAddress))
		{
			bhUserSession userSession = accountManager.attemptSignUp(creds, result);
			
			if( userSession != null )
			{
				sm_s.sessionMngr.startSession(userSession, response, creds.rememberMe());
			}
		}
		
		result.writeJson(response.getJson());
	}
}
