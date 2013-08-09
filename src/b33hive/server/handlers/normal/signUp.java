package b33hive.server.handlers.normal;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaResponse;

import b33hive.server.account.bhServerAccountManager;
import b33hive.server.account.bhUserSession;
import b33hive.server.account.bh_s;
import b33hive.server.session.bhSessionManager;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.account.bhE_SignUpCredentialType;
import b33hive.shared.account.bhE_SignUpValidationError;
import b33hive.shared.account.bhSignUpCredentials;
import b33hive.shared.account.bhSignUpValidationResult;
import b33hive.shared.account.bhSignUpValidator;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class signUp implements bhI_RequestHandler
{
	private final String m_publicRecaptchaKey;
	private final String m_privateRecaptchaKey;
	
	public signUp(String publicRecaptchaKey, String privateRecaptchaKey)
	{
		m_publicRecaptchaKey = publicRecaptchaKey;
		m_privateRecaptchaKey = privateRecaptchaKey;
	}
	
	private void validateCaptcha(bhSignUpCredentials creds, bhSignUpValidationResult outResult, String remoteAddress)
	{
		String captchaChallenge = creds.getCaptchaChallenge();
		String captchaResponse = creds.get(bhE_SignUpCredentialType.CAPTCHA_RESPONSE);
		
		ReCaptcha captcha = ReCaptchaFactory.newReCaptcha(m_publicRecaptchaKey, m_privateRecaptchaKey, false);
        ReCaptchaResponse recaptchaResponse = captcha.checkAnswer(remoteAddress, captchaChallenge, captchaResponse);

        if ( !recaptchaResponse.isValid())
        {
        	outResult.setError(bhE_SignUpCredentialType.CAPTCHA_RESPONSE, bhE_SignUpValidationError.CAPTCHA_INCORRECT);
        }
	}
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhSignUpCredentials creds = new bhSignUpCredentials(request.getJson());
		
		bhSignUpValidationResult result = bhSignUpValidator.getInstance().validate(creds);
		
		if( result.isEverythingOk() )
		{
			String remoteAddress = ((HttpServletRequest) request.getNativeRequest()).getRemoteAddr();
			validateCaptcha(creds, result, remoteAddress);
			
			if( result.isEverythingOk/*Still?*/() )
			{
				bhServerAccountManager accountManager = bh_s.accountMngr;
				
				bhUserSession userSession = accountManager.attemptSignUp(result, creds);
				
				if( result.isEverythingOk/*STILL?*/() )
				{
					bh_s.sessionMngr.startSession(userSession, response, creds.rememberMe());
				}
			}
		}
		
		result.writeJson(response.getJson());
	}
}
