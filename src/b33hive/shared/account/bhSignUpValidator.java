package b33hive.shared.account;

import b33hive.shared.bhE_AppEnvironment;
import b33hive.shared.app.bhA_App;

public class bhSignUpValidator
{	
	private static final bhSignUpValidator s_instance = new bhSignUpValidator();
	
	private final bhSignUpValidationResult m_reusedResult = new bhSignUpValidationResult();
	
	private bhSignUpValidator()
	{
		
	}
	
	public static bhSignUpValidator getInstance()
	{
		return s_instance;
	}
	
	public bhSignUpValidationResult validate(bhSignUpCredentials credentials)
	{
		bhSignUpValidationResult result = null;
		
		if( bhA_App.getInstance().getEnvironment() == bhE_AppEnvironment.CLIENT )
		{
			result = m_reusedResult;
		}
		else
		{
			result = new bhSignUpValidationResult();
		}
		
		if( credentials.getCaptchaChallenge() == null )
		{
			result.setResponseError();
			
			return result;
		}
		
		for( int i = 0; i < bhE_SignUpCredentialType.values().length; i++ )
		{
			bhE_SignUpCredentialType type = bhE_SignUpCredentialType.values()[i];
			String credential = credentials.get(type);
			bhE_SignUpValidationError error = type.getValidator().validateCredential(credential);
			
			result.setError(type, error);
		}
		
		return result;
	}
}
