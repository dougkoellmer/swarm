package swarm.shared.account;

import swarm.shared.app.sm;
import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhA_JsonFactory;
import swarm.shared.json.bhI_JsonArray;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhJsonHelper;


public class bhSignUpValidationResult extends bhA_JsonEncodable
{
	private bhE_SignUpValidationError[] m_errors;
	
	public bhSignUpValidationResult()
	{
		init();
	}
	
	public bhSignUpValidationResult(bhI_JsonObject json)
	{
		super(json);
		
		init();
	}
	
	public void setResponseError()
	{
		setError(bhE_SignUpCredentialType.EMAIL,			bhE_SignUpValidationError.RESPONSE_ERROR);
		setError(bhE_SignUpCredentialType.USERNAME,			bhE_SignUpValidationError.NO_ERROR);
		setError(bhE_SignUpCredentialType.PASSWORD,			bhE_SignUpValidationError.NO_ERROR);
		setError(bhE_SignUpCredentialType.CAPTCHA_RESPONSE,	bhE_SignUpValidationError.NO_ERROR);
	}
	
	public void setError(bhE_SignUpCredentialType eType, bhE_SignUpValidationError error)
	{
		m_errors[eType.ordinal()] = error;
	}
	
	private void init()
	{
		m_errors = m_errors != null ? m_errors : new bhE_SignUpValidationError[bhE_SignUpCredentialType.values().length];
	}
	
	public bhE_SignUpValidationError getError(bhE_SignUpCredentialType eType)
	{
		return m_errors[eType.ordinal()];
	}
	
	public boolean isEverythingOk()
	{
		for( int i = 0; i < m_errors.length; i++ )
		{
			if( m_errors[i] != bhE_SignUpValidationError.NO_ERROR )
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
			m_errors[i] = bhE_SignUpValidationError.NO_ERROR;
		}
	}

	@Override
	public void writeJson(bhI_JsonObject json)
	{
		bhI_JsonArray errors = sm.jsonFactory.createJsonArray();
		
		for( int i = 0; i < m_errors.length; i++ )
		{
			errors.addInt(m_errors[i].ordinal());
		}
		
		sm.jsonFactory.getHelper().putJsonArray(json, bhE_JsonKey.signUpValidationErrors, errors);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		init();
		
		bhI_JsonArray errors = sm.jsonFactory.getHelper().getJsonArray(json, bhE_JsonKey.signUpValidationErrors);
		
		for( int i = 0; i < errors.getSize(); i++ )
		{
			int ordinal = errors.getInt(i);
			
			m_errors[i] = bhE_SignUpValidationError.values()[ordinal];
		}
	}
}
