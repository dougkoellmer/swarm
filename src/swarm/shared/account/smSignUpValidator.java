package swarm.shared.account;

import swarm.shared.smE_AppEnvironment;
import swarm.shared.app.smA_App;

public class smSignUpValidator
{
	private final smSignUpValidationResult m_reusedResult = new smSignUpValidationResult();
	
	private final boolean m_clientSide;
	
	public smSignUpValidator(boolean clientSide)
	{
		m_clientSide =  clientSide;
	}
	
	public smSignUpValidationResult validate(smSignUpCredentials credentials)
	{
		smSignUpValidationResult result = null;
		
		if( m_clientSide )
		{
			result = m_reusedResult;
		}
		else
		{
			result = new smSignUpValidationResult();
		}
		
		validate( credentials, result);
		
		return result;
	}
	
	public void validate(smSignUpCredentials credentials, smSignUpValidationResult result_out)
	{
		if( credentials.getCaptchaChallenge() == null )
		{
			result_out.setResponseError();
		}
		
		for( int i = 0; i < smE_SignUpCredentialType.values().length; i++ )
		{
			smE_SignUpCredentialType type = smE_SignUpCredentialType.values()[i];
			String credential = credentials.get(type);
			smE_SignUpValidationError error = type.getValidator().validateCredential(credential);
			
			result_out.setError(type, error);
		}
	}
}
