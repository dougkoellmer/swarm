package swarm.shared.account;

import swarm.shared.app.smS_App;
import swarm.shared.utils.smU_Regex;

public interface smI_SignInCredentialValidator
{
	smE_SignInValidationError validateCredential(String credential);

	public static smI_SignInCredentialValidator EMAIL_VALIDATOR = new smI_SignInCredentialValidator()
	{
		public smE_SignInValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return smE_SignInValidationError.EMPTY;
			}
			
			return smE_SignInValidationError.NO_ERROR;
		}
	};
	
	public static final smI_SignInCredentialValidator PASSWORD_VALIDATOR = new smI_SignInCredentialValidator()
	{
		public smE_SignInValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return smE_SignInValidationError.EMPTY;
			}
			else if( credential.length() < smS_Account.MIN_PASSWORD_LENGTH )
			{
				return smE_SignInValidationError.PASSWORD_TOO_SHORT;
			}
			
			return smE_SignInValidationError.NO_ERROR;
		}
	};
}
