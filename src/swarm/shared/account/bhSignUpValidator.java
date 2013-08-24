package swarm.shared.account;

import swarm.shared.bhE_AppEnvironment;
import swarm.shared.app.bhA_App;

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
		
		validate( credentials, result);
		
		return result;
	}
	
	public void validate(bhSignUpCredentials credentials, bhSignUpValidationResult result_out)
	{
		if( credentials.getCaptchaChallenge() == null )
		{
			result_out.setResponseError();
		}
		
		for( int i = 0; i < bhE_SignUpCredentialType.values().length; i++ )
		{
			bhE_SignUpCredentialType type = bhE_SignUpCredentialType.values()[i];
			String credential = credentials.get(type);
			bhE_SignUpValidationError error = type.getValidator().validateCredential(credential);
			
			result_out.setError(type, error);
		}
	}
}
