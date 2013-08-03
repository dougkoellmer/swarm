package b33hive.server.handlers.normal;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaResponse;

import b33hive.server.account.bhS_ServerRecaptcha;
import b33hive.server.account.bhServerAccountManager;
import b33hive.server.account.bhUserSession;
import b33hive.server.session.bhSessionManager;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.account.bhE_SignUpCredentialType;
import b33hive.shared.account.bhE_SignUpValidationError;
import b33hive.shared.account.bhS_Recaptcha;
import b33hive.shared.account.bhSignUpCredentials;
import b33hive.shared.account.bhSignUpValidationResult;
import b33hive.shared.account.bhSignUpValidator;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class signUp implements bhI_RequestHandler
{
	private static void validateCaptcha(bhSignUpCredentials creds, bhSignUpValidationResult outResult, String remoteAddress)
	{
		String captchaChallenge = creds.getCaptchaChallenge();
		String captchaResponse = creds.get(bhE_SignUpCredentialType.CAPTCHA_RESPONSE);
		String publicKey = bhS_Recaptcha.PUBLIC_KEY;
		String privateKey = bhS_ServerRecaptcha.PRIVATE_KEY;
		
		ReCaptcha captcha = ReCaptchaFactory.newReCaptcha(publicKey, privateKey, false);
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
				bhServerAccountManager accountManager = bhServerAccountManager.getInstance();
				
				bhUserSession userSession = accountManager.attemptSignUp(result, creds);
				
				if( result.isEverythingOk/*STILL?*/() )
				{
					bhSessionManager.getInstance().startSession(userSession, response, creds.rememberMe());
				}
			}
		}
		
		result.writeJson(response.getJson());
	}
}
