package swarm.shared.account;

import swarm.shared.smE_AppEnvironment;
import swarm.shared.app.smA_App;

public class smSignInValidator
{	
	private final smSignInValidationResult m_reusedResult = new smSignInValidationResult();
	
	private final boolean m_clientSide;
	
	public smSignInValidator(boolean clientSide)
	{
		m_clientSide = clientSide;
	}
	
	public smSignInValidationResult validate(smSignInCredentials credentials)
	{
		smSignInValidationResult result;
		
		if( m_clientSide )
		{
			result = m_reusedResult;
		}
		else
		{
			result = new smSignInValidationResult();
		}
		
		validate(credentials, result);
		
		return result;
	}
	
	public void validate(smSignInCredentials credentials, smSignInValidationResult result_out)
	{		
		for( int i = 0; i < smE_SignInCredentialType.values().length; i++ )
		{
			smE_SignInCredentialType type = smE_SignInCredentialType.values()[i];
			String credential = credentials.get(type);
			
			smE_SignInValidationError error = type.getValidator().validateCredential(credential);
			
			if( error == smE_SignInValidationError.PASSWORD_TOO_SHORT && !credentials.isForNewPassword() )
			{
				error = smE_SignInValidationError.NO_ERROR;
			}
			
			result_out.m_errors[i] = error;
		}
	}
}
