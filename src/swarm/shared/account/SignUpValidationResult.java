package swarm.shared.account;

import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.JsonHelper;


public class SignUpValidationResult extends A_JsonEncodable
{
	private E_SignUpValidationError[] m_errors;
	
	public SignUpValidationResult()
	{
		init();
	}
	
	public SignUpValidationResult(A_JsonFactory jsonFactory, I_JsonObject json)
	{
		super(jsonFactory, json);
		
		init();
	}
	
	public void setResponseError()
	{
		setError(E_SignUpCredentialType.EMAIL,			E_SignUpValidationError.RESPONSE_ERROR);
		setError(E_SignUpCredentialType.USERNAME,			E_SignUpValidationError.NO_ERROR);
		setError(E_SignUpCredentialType.PASSWORD,			E_SignUpValidationError.NO_ERROR);
		setError(E_SignUpCredentialType.CAPTCHA_RESPONSE,	E_SignUpValidationError.NO_ERROR);
	}
	
	public void setError(E_SignUpCredentialType eType, E_SignUpValidationError error)
	{
		m_errors[eType.ordinal()] = error;
	}
	
	private void init()
	{
		m_errors = m_errors != null ? m_errors : new E_SignUpValidationError[E_SignUpCredentialType.values().length];
	}
	
	public E_SignUpValidationError getError(E_SignUpCredentialType eType)
	{
		return m_errors[eType.ordinal()];
	}
	
	public boolean isEverythingOk()
	{
		for( int i = 0; i < m_errors.length; i++ )
		{
			if( m_errors[i] != E_SignUpValidationError.NO_ERROR )
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
			m_errors[i] = E_SignUpValidationError.NO_ERROR;
		}
	}

	@Override
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		I_JsonArray errors = factory.createJsonArray();
		
		for( int i = 0; i < m_errors.length; i++ )
		{
			errors.addInt(m_errors[i].ordinal());
		}
		
		factory.getHelper().putJsonArray(json_out, E_JsonKey.signUpValidationErrors, errors);
	}

	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		init();
		
		I_JsonArray errors = factory.getHelper().getJsonArray(json, E_JsonKey.signUpValidationErrors);
		
		for( int i = 0; i < errors.getSize(); i++ )
		{
			int ordinal = errors.getInt(i);
			
			m_errors[i] = E_SignUpValidationError.values()[ordinal];
		}
	}
}
