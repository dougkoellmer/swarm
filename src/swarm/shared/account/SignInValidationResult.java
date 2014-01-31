package swarm.shared.account;

import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;


public class SignInValidationResult extends A_JsonEncodable
{
	E_SignInValidationError[] m_errors;
	
	public E_SignInValidationError getError(E_SignInCredentialType eType)
	{
		return m_errors[eType.ordinal()];
	}
	
	public SignInValidationResult()
	{
		init();
	}
	
	public SignInValidationResult(A_JsonFactory jsonFactory, I_JsonObject json)
	{
		super(jsonFactory, json);
		
		init();
	}
	
	private void init()
	{
		m_errors = m_errors != null ? m_errors : new E_SignInValidationError[E_SignInCredentialType.values().length];
	}
	
	public void setResponseError()
	{
		setError(E_SignInCredentialType.EMAIL,	E_SignInValidationError.RESPONSE_ERROR);
		setError(E_SignInCredentialType.PASSWORD,	E_SignInValidationError.NO_ERROR);
	}
	
	public void setBadCombinationError()
	{
		this.setError(E_SignInCredentialType.EMAIL,		E_SignInValidationError.UNKNOWN_COMBINATION);
		this.setError(E_SignInCredentialType.PASSWORD,	E_SignInValidationError.NO_ERROR);
	}
	
	public boolean isEverythingOk()
	{
		for( int i = 0; i < m_errors.length; i++ )
		{
			if( m_errors[i] != E_SignInValidationError.NO_ERROR )
			{
				return false;
			}
		}
		
		return true;
	}
	
	public void setError(E_SignInCredentialType eType, E_SignInValidationError error)
	{
		m_errors[eType.ordinal()] = error;
	}

	@Override
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		I_JsonArray errors = factory.createJsonArray();
		
		for( int i = 0; i < m_errors.length; i++ )
		{
			errors.addInt(m_errors[i].ordinal());
		}
		
		factory.getHelper().putJsonArray(json_out, E_JsonKey.signInValidationErrors, errors);
	}

	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		init();
		
		I_JsonArray errors = factory.getHelper().getJsonArray(json, E_JsonKey.signInValidationErrors);
		
		for( int i = 0; i < errors.getSize(); i++ )
		{
			int ordinal = errors.getInt(i);
			
			m_errors[i] = E_SignInValidationError.values()[ordinal];
		}
	}
}
