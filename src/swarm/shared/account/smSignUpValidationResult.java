package swarm.shared.account;

import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smJsonHelper;


public class smSignUpValidationResult extends smA_JsonEncodable
{
	private smE_SignUpValidationError[] m_errors;
	
	public smSignUpValidationResult()
	{
		init();
	}
	
	public smSignUpValidationResult(smA_JsonFactory jsonFactory, smI_JsonObject json)
	{
		super(jsonFactory, json);
		
		init();
	}
	
	public void setResponseError()
	{
		setError(smE_SignUpCredentialType.EMAIL,			smE_SignUpValidationError.RESPONSE_ERROR);
		setError(smE_SignUpCredentialType.USERNAME,			smE_SignUpValidationError.NO_ERROR);
		setError(smE_SignUpCredentialType.PASSWORD,			smE_SignUpValidationError.NO_ERROR);
		setError(smE_SignUpCredentialType.CAPTCHA_RESPONSE,	smE_SignUpValidationError.NO_ERROR);
	}
	
	public void setError(smE_SignUpCredentialType eType, smE_SignUpValidationError error)
	{
		m_errors[eType.ordinal()] = error;
	}
	
	private void init()
	{
		m_errors = m_errors != null ? m_errors : new smE_SignUpValidationError[smE_SignUpCredentialType.values().length];
	}
	
	public smE_SignUpValidationError getError(smE_SignUpCredentialType eType)
	{
		return m_errors[eType.ordinal()];
	}
	
	public boolean isEverythingOk()
	{
		for( int i = 0; i < m_errors.length; i++ )
		{
			if( m_errors[i] != smE_SignUpValidationError.NO_ERROR )
			{
				return false;
			}
		}
		
		return true;
	}
	
	public void setNoErrors()
	{
		for( int i = 0; i < m_errors.length; i++ )
		{
			m_errors[i] = smE_SignUpValidationError.NO_ERROR;
		}
	}

	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		smI_JsonArray errors = factory.createJsonArray();
		
		for( int i = 0; i < m_errors.length; i++ )
		{
			errors.addInt(m_errors[i].ordinal());
		}
		
		factory.getHelper().putJsonArray(json_out, smE_JsonKey.signUpValidationErrors, errors);
	}

	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		init();
		
		smI_JsonArray errors = factory.getHelper().getJsonArray(json, smE_JsonKey.signUpValidationErrors);
		
		for( int i = 0; i < errors.getSize(); i++ )
		{
			int ordinal = errors.getInt(i);
			
			m_errors[i] = smE_SignUpValidationError.values()[ordinal];
		}
	}
}
