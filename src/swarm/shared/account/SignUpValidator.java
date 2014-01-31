package swarm.shared.account;

import swarm.shared.E_AppEnvironment;
import swarm.shared.app.A_App;

public class SignUpValidator
{
	private final SignUpValidationResult m_reusedResult = new SignUpValidationResult();
	
	private final boolean m_clientSide;
	
	public SignUpValidator(boolean clientSide)
	{
		m_clientSide =  clientSide;
	}
	
	public SignUpValidationResult validate(SignUpCredentials credentials)
	{
		SignUpValidationResult result = null;
		
		if( m_clientSide )
		{
			result = m_reusedResult;
		}
		else
		{
			result = new SignUpValidationResult();
		}
		
		validate( credentials, result);
		
		return result;
	}
	
	public void validate(SignUpCredentials credentials, SignUpValidationResult result_out)
	{
		if( credentials.getCaptchaChallenge() == null )
		{
			result_out.setResponseError();
		}
		
		for( int i = 0; i < E_SignUpCredentialType.values().length; i++ )
		{
			E_SignUpCredentialType type = E_SignUpCredentialType.values()[i];
			String credential = credentials.get(type);
			E_SignUpValidationError error = type.getValidator().validateCredential(credential);
			
			result_out.setError(type, error);
		}
	}
}
