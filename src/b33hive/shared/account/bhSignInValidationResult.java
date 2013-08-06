package b33hive.shared.account;

import b33hive.shared.app.bh;
import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;


public class bhSignInValidationResult extends bhA_JsonEncodable
{
	bhE_SignInValidationError[] m_errors;
	
	public bhE_SignInValidationError getError(bhE_SignInCredentialType eType)
	{
		return m_errors[eType.ordinal()];
	}
	
	public bhSignInValidationResult()
	{
		init();
	}
	
	public bhSignInValidationResult(bhI_JsonObject json)
	{
		super(json);
		
		init();
	}
	
	private void init()
	{
		m_errors = m_errors != null ? m_errors : new bhE_SignInValidationError[bhE_SignInCredentialType.values().length];
	}
	
	public void setResponseError()
	{
		setError(bhE_SignInCredentialType.EMAIL,	bhE_SignInValidationError.RESPONSE_ERROR);
		setError(bhE_SignInCredentialType.PASSWORD,	bhE_SignInValidationError.NO_ERROR);
	}
	
	public void setBadCombinationError()
	{
		this.setError(bhE_SignInCredentialType.EMAIL,		bhE_SignInValidationError.UNKNOWN_COMBINATION);
		this.setError(bhE_SignInCredentialType.PASSWORD,	bhE_SignInValidationError.NO_ERROR);
	}
	
	public boolean isEverythingOk()
	{
		for( int i = 0; i < m_errors.length; i++ )
		{
			if( m_errors[i] != bhE_SignInValidationError.NO_ERROR )
			{
				return false;
			}
		}
		
		return true;
	}
	
	public void setError(bhE_SignInCredentialType eType, bhE_SignInValidationError error)
	{
		m_errors[eType.ordinal()] = error;
	}

	@Override
	public void writeJson(bhI_JsonObject json)
	{
		bhI_JsonArray errors = bh.jsonFactory.createJsonArray();
		
		for( int i = 0; i < m_errors.length; i++ )
		{
			errors.addInt(m_errors[i].ordinal());
		}
		
		bh.jsonFactory.getHelper().putJsonArray(json, bhE_JsonKey.signInValidationErrors, errors);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		init();
		
		bhI_JsonArray errors = bh.jsonFactory.getHelper().getJsonArray(json, bhE_JsonKey.signInValidationErrors);
		
		for( int i = 0; i < errors.getSize(); i++ )
		{
			int ordinal = errors.getInt(i);
			
			m_errors[i] = bhE_SignInValidationError.values()[ordinal];
		}
	}
}
