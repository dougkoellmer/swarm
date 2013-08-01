package com.b33hive.server.handlers;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaResponse;

import com.b33hive.server.account.bhS_ServerRecaptcha;
import com.b33hive.server.account.bhServerAccountManager;
import com.b33hive.server.account.bhUserSession;
import com.b33hive.server.session.bhSessionManager;
import com.b33hive.server.transaction.bhI_RequestHandler;
import com.b33hive.server.transaction.bhTransactionContext;
import com.b33hive.shared.account.bhE_SignUpCredentialType;
import com.b33hive.shared.account.bhE_SignUpValidationError;
import com.b33hive.shared.account.bhS_Recaptcha;
import com.b33hive.shared.account.bhSignUpCredentials;
import com.b33hive.shared.account.bhSignUpValidationResult;
import com.b33hive.shared.account.bhSignUpValidator;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

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
