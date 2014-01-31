package swarm.shared.account;

import swarm.shared.E_AppEnvironment;
import swarm.shared.app.A_App;

public class SignInValidator
{	
	private final SignInValidationResult m_reusedResult = new SignInValidationResult();
	
	private final boolean m_clientSide;
	
	public SignInValidator(boolean clientSide)
	{
		m_clientSide = clientSide;
	}
	
	public SignInValidationResult validate(SignInCredentials credentials)
	{
		SignInValidationResult result;
		
		if( m_clientSide )
		{
			result = m_reusedResult;
		}
		else
		{
			result = new SignInValidationResult();
		}
		
		validate(credentials, result);
		
		return result;
	}
	
	public void validate(SignInCredentials credentials, SignInValidationResult result_out)
	{		
		for( int i = 0; i < E_SignInCredentialType.values().length; i++ )
		{
			E_SignInCredentialType type = E_SignInCredentialType.values()[i];
			String credential = credentials.get(type);
			
			E_SignInValidationError error = type.getValidator().validateCredential(credential);
			
			if( error == E_SignInValidationError.PASSWORD_TOO_SHORT && !credentials.isForNewPassword() )
			{
				error = E_SignInValidationError.NO_ERROR;
			}
			
			result_out.m_errors[i] = error;
		}
	}
}
