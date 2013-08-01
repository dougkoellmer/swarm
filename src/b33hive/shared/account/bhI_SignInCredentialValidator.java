package com.b33hive.shared.account;

import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.bhU_Regex;

public interface bhI_SignInCredentialValidator
{
	bhE_SignInValidationError validateCredential(String credential);

	public static bhI_SignInCredentialValidator EMAIL_VALIDATOR = new bhI_SignInCredentialValidator()
	{
		public bhE_SignInValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return bhE_SignInValidationError.EMPTY;
			}
			
			return bhE_SignInValidationError.NO_ERROR;
		}
	};
	
	public static final bhI_SignInCredentialValidator PASSWORD_VALIDATOR = new bhI_SignInCredentialValidator()
	{
		public bhE_SignInValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return bhE_SignInValidationError.EMPTY;
			}
			else if( credential.length() < bhS_Account.MIN_PASSWORD_LENGTH )
			{
				return bhE_SignInValidationError.PASSWORD_TOO_SHORT;
			}
			
			return bhE_SignInValidationError.NO_ERROR;
		}
	};
}
