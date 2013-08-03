package b33hive.shared.account;

import b33hive.shared.bhE_AppEnvironment;
import b33hive.shared.app.bhA_App;

public class bhSignInValidator
{	
	private static final bhSignInValidator s_instance = new bhSignInValidator();
	
	private final bhSignInValidationResult m_reusedResult = new bhSignInValidationResult();
	
	private bhSignInValidator()
	{
		
	}
	
	public static bhSignInValidator getInstance()
	{
		return s_instance;
	}
	
	public bhSignInValidationResult validate(bhSignInCredentials credentials)
	{
		bhSignInValidationResult result = null;
		
		if( bhA_App.getInstance().getEnvironment() == bhE_AppEnvironment.CLIENT )
		{
			result = m_reusedResult;
		}
		else
		{
			result = new bhSignInValidationResult();
		}
		
		for( int i = 0; i < bhE_SignInCredentialType.values().length; i++ )
		{
			bhE_SignInCredentialType type = bhE_SignInCredentialType.values()[i];
			String credential = credentials.get(type);
			
			bhE_SignInValidationError error = type.getValidator().validateCredential(credential);
			
			if( error == bhE_SignInValidationError.PASSWORD_TOO_SHORT && !credentials.isForNewPassword() )
			{
				error = bhE_SignInValidationError.NO_ERROR;
			}
			
			result.m_errors[i] = error;
		}
		
		return result;
	}
}
