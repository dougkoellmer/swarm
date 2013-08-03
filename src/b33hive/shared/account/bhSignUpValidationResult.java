package b33hive.shared.account;

import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhJsonHelper;

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

	@Override
	public void writeJson(bhI_JsonObject json)
	{
		bhI_JsonArray errors = bhA_JsonFactory.getInstance().createJsonArray();
		
		for( int i = 0; i < m_errors.length; i++ )
		{
			errors.addInt(m_errors[i].ordinal());
		}
		
		bhJsonHelper.getInstance().putJsonArray(json, bhE_JsonKey.signUpValidationErrors, errors);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		init();
		
		bhI_JsonArray errors = bhJsonHelper.getInstance().getJsonArray(json, bhE_JsonKey.signUpValidationErrors);
		
		for( int i = 0; i < errors.getSize(); i++ )
		{
			int ordinal = errors.getInt(i);
			
			m_errors[i] = bhE_SignUpValidationError.values()[ordinal];
		}
	}
}
