package swarm.shared.account;

import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;


public class smSignInValidationResult extends smA_JsonEncodable
{
	smE_SignInValidationError[] m_errors;
	
	public smE_SignInValidationError getError(smE_SignInCredentialType eType)
	{
		return m_errors[eType.ordinal()];
	}
	
	public smSignInValidationResult()
	{
		init();
	}
	
	public smSignInValidationResult(smA_JsonFactory jsonFactory, smI_JsonObject json)
	{
		super(jsonFactory, json);
		
		init();
	}
	
	private void init()
	{
		m_errors = m_errors != null ? m_errors : new smE_SignInValidationError[smE_SignInCredentialType.values().length];
	}
	
	public void setResponseError()
	{
		setError(smE_SignInCredentialType.EMAIL,	smE_SignInValidationError.RESPONSE_ERROR);
		setError(smE_SignInCredentialType.PASSWORD,	smE_SignInValidationError.NO_ERROR);
	}
	
	public void setBadCombinationError()
	{
		this.setError(smE_SignInCredentialType.EMAIL,		smE_SignInValidationError.UNKNOWN_COMBINATION);
		this.setError(smE_SignInCredentialType.PASSWORD,	smE_SignInValidationError.NO_ERROR);
	}
	
	public boolean isEverythingOk()
	{
		for( int i = 0; i < m_errors.length; i++ )
		{
			if( m_errors[i] != smE_SignInValidationError.NO_ERROR )
			{
				return false;
			}
		}
		
		return true;
	}
	
	public void setError(smE_SignInCredentialType eType, smE_SignInValidationError error)
	{
		m_errors[eType.ordinal()] = error;
	}

	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		smI_JsonArray errors = factory.createJsonArray();
		
		for( int i = 0; i < m_errors.length; i++ )
		{
			errors.addInt(m_errors[i].ordinal());
		}
		
		factory.getHelper().putJsonArray(json_out, smE_JsonKey.signInValidationErrors, errors);
	}

	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		init();
		
		smI_JsonArray errors = factory.getHelper().getJsonArray(json, smE_JsonKey.signInValidationErrors);
		
		for( int i = 0; i < errors.getSize(); i++ )
		{
			int ordinal = errors.getInt(i);
			
			m_errors[i] = smE_SignInValidationError.values()[ordinal];
		}
	}
}
