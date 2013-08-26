package swarm.shared.account;

import swarm.shared.app.smS_App;
import swarm.shared.utils.smU_Regex;

public interface smI_SignUpCredentialValidator
{
	smE_SignUpValidationError validateCredential(String credential);

	public static smI_SignUpCredentialValidator EMAIL_VALIDATOR = new smI_SignUpCredentialValidator()
	{
		public smE_SignUpValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return smE_SignUpValidationError.EMPTY;
			}
			else if( credential.length() > smS_Account.MAX_EMAIL_LENGTH )
			{
				return smE_SignUpValidationError.EMAIL_TOO_LONG;
			}
			else if( !bhU_Regex.calcIsMatch(credential, smS_Account.EMAIL_REGEX) )
			{
				return smE_SignUpValidationError.EMAIL_INVALID;
			}
			
			return smE_SignUpValidationError.NO_ERROR;
		}
	};
	
	public static final smI_SignUpCredentialValidator USERNAME_VALIDATOR = new smI_SignUpCredentialValidator()
	{
		public smE_SignUpValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return smE_SignUpValidationError.EMPTY;
			}
			else if( credential.length() > smS_Account.MAX_USERNAME_LENGTH )
			{
				return smE_SignUpValidationError.USERNAME_TOO_LONG;
			}
			else if( !bhU_Regex.calcIsMatch(credential, smS_Account.USERNAME_REGEX) )
			{
				return smE_SignUpValidationError.USERNAME_INVALID;
			}
			
			return smE_SignUpValidationError.NO_ERROR;
		}
	};
	
	public static final smI_SignUpCredentialValidator PASSWORD_VALIDATOR = new smI_SignUpCredentialValidator()
	{
		public smE_SignUpValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return smE_SignUpValidationError.EMPTY;
			}
			else if( credential.length() < smS_Account.MIN_PASSWORD_LENGTH )
			{
				return smE_SignUpValidationError.PASSWORD_TOO_SHORT;
			}
			
			return smE_SignUpValidationError.NO_ERROR;
		}
	};
	
	public static final smI_SignUpCredentialValidator CAPTCHA_RESPONSE_VALIDATOR = new smI_SignUpCredentialValidator()
	{
		public smE_SignUpValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return smE_SignUpValidationError.EMPTY;
			}
			
			return smE_SignUpValidationError.NO_ERROR;
		}
	};
}
