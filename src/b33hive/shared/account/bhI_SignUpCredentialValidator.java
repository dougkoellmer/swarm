package com.b33hive.shared.account;

import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.bhU_Regex;

public interface bhI_SignUpCredentialValidator
{
	bhE_SignUpValidationError validateCredential(String credential);

	public static bhI_SignUpCredentialValidator EMAIL_VALIDATOR = new bhI_SignUpCredentialValidator()
	{
		public bhE_SignUpValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return bhE_SignUpValidationError.EMPTY;
			}
			else if( credential.length() > bhS_Account.MAX_EMAIL_LENGTH )
			{
				return bhE_SignUpValidationError.EMAIL_TOO_LONG;
			}
			else if( !bhU_Regex.calcIsMatch(credential, bhS_Account.EMAIL_REGEX) )
			{
				return bhE_SignUpValidationError.EMAIL_INVALID;
			}
			
			return bhE_SignUpValidationError.NO_ERROR;
		}
	};
	
	public static bhI_SignUpCredentialValidator USERNAME_VALIDATOR = new bhI_SignUpCredentialValidator()
	{
		public bhE_SignUpValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return bhE_SignUpValidationError.EMPTY;
			}
			else if( credential.length() > bhS_Account.MAX_USERNAME_LENGTH )
			{
				return bhE_SignUpValidationError.USERNAME_TOO_LONG;
			}
			else if( !bhU_Regex.calcIsMatch(credential, bhS_Account.USERNAME_REGEX) )
			{
				return bhE_SignUpValidationError.USERNAME_INVALID;
			}
			
			return bhE_SignUpValidationError.NO_ERROR;
		}
	};
	
	public static final bhI_SignUpCredentialValidator PASSWORD_VALIDATOR = new bhI_SignUpCredentialValidator()
	{
		public bhE_SignUpValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return bhE_SignUpValidationError.EMPTY;
			}
			else if( credential.length() < bhS_Account.MIN_PASSWORD_LENGTH )
			{
				return bhE_SignUpValidationError.PASSWORD_TOO_SHORT;
			}
			
			return bhE_SignUpValidationError.NO_ERROR;
		}
	};
	
	public static final bhI_SignUpCredentialValidator CAPTCHA_RESPONSE_VALIDATOR = new bhI_SignUpCredentialValidator()
	{
		public bhE_SignUpValidationError validateCredential(String credential)
		{
			if( credential.length() == 0 )
			{
				return bhE_SignUpValidationError.EMPTY;
			}
			
			return bhE_SignUpValidationError.NO_ERROR;
		}
	};
}
