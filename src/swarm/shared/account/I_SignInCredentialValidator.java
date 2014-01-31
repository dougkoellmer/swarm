package swarm.shared.account;

import swarm.shared.app.S_CommonApp;
import swarm.shared.utils.U_Regex;

public interface I_SignInCredentialValidator
{
	E_SignInValidationError validateCredential(String credential);

	public static I_SignInCredentialValidator EMAIL_VALIDATOR = new I_SignInCredentialValidator()
	{
		public E_SignInValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return E_SignInValidationError.EMPTY;
			}
			
			return E_SignInValidationError.NO_ERROR;
		}
	};
	
	public static final I_SignInCredentialValidator PASSWORD_VALIDATOR = new I_SignInCredentialValidator()
	{
		public E_SignInValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return E_SignInValidationError.EMPTY;
			}
			else if( credential.length() < S_Account.MIN_PASSWORD_LENGTH )
			{
				return E_SignInValidationError.PASSWORD_TOO_SHORT;
			}
			
			return E_SignInValidationError.NO_ERROR;
		}
	};
}
