package swarm.shared.account;

import swarm.shared.app.S_CommonApp;
import swarm.shared.utils.U_Regex;

public interface I_SignUpCredentialValidator
{
	E_SignUpValidationError validateCredential(String credential);

	public static I_SignUpCredentialValidator EMAIL_VALIDATOR = new I_SignUpCredentialValidator()
	{
		public E_SignUpValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return E_SignUpValidationError.EMPTY;
			}
			else if( credential.length() > S_Account.MAX_EMAIL_LENGTH )
			{
				return E_SignUpValidationError.EMAIL_TOO_LONG;
			}
			else if( !U_Regex.calcIsMatch(credential, S_Account.EMAIL_REGEX) )
			{
				return E_SignUpValidationError.EMAIL_INVALID;
			}
			
			return E_SignUpValidationError.NO_ERROR;
		}
	};
	
	public static final I_SignUpCredentialValidator USERNAME_VALIDATOR = new I_SignUpCredentialValidator()
	{
		public E_SignUpValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return E_SignUpValidationError.EMPTY;
			}
			else if( credential.length() > S_Account.MAX_USERNAME_LENGTH )
			{
				return E_SignUpValidationError.USERNAME_TOO_LONG;
			}
			else if( !U_Regex.calcIsMatch(credential, S_Account.USERNAME_REGEX) )
			{
				return E_SignUpValidationError.USERNAME_INVALID;
			}
			
			return E_SignUpValidationError.NO_ERROR;
		}
	};
	
	public static final I_SignUpCredentialValidator PASSWORD_VALIDATOR = new I_SignUpCredentialValidator()
	{
		public E_SignUpValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return E_SignUpValidationError.EMPTY;
			}
			else if( credential.length() < S_Account.MIN_PASSWORD_LENGTH )
			{
				return E_SignUpValidationError.PASSWORD_TOO_SHORT;
			}
			
			return E_SignUpValidationError.NO_ERROR;
		}
	};
	
	public static final I_SignUpCredentialValidator CAPTCHA_RESPONSE_VALIDATOR = new I_SignUpCredentialValidator()
	{
		public E_SignUpValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return E_SignUpValidationError.EMPTY;
			}
			
			return E_SignUpValidationError.NO_ERROR;
		}
	};
}
