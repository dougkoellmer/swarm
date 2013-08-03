package b33hive.shared.account;

import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.json.bhE_JsonKey;

public class bhSignUpCredentials extends bhA_AccountCredentials
{
	private String m_captchaChallenge;
	
	public bhSignUpCredentials(bhI_JsonObject json)
	{
		super(json);
		
		init();
	}
	
	public bhSignUpCredentials(boolean rememberMe, String... args)
	{
		super(rememberMe);
		
		init();
		
		for( int i = 0; i < m_credentials.length; i++ )
		{
			m_credentials[i] = args[i];
		}
		
		m_captchaChallenge = args[args.length-1];
	}
	
	private void init()
	{
		if( m_credentials == null )
		{
			m_credentials = new String[bhE_SignUpCredentialType.values().length];
		}
	}
	
	public String get(bhE_SignUpCredentialType eType)
	{
		return m_credentials[eType.ordinal()];
	}
	
	public String getCaptchaChallenge()
	{
		return m_captchaChallenge;
	}
	
	private void toLowerCase()
	{
		m_credentials[bhE_SignInCredentialType.EMAIL.ordinal()] = m_credentials[bhE_SignInCredentialType.EMAIL.ordinal()].toLowerCase();
		m_credentials[bhE_SignUpCredentialType.USERNAME.ordinal()] = m_credentials[bhE_SignUpCredentialType.USERNAME.ordinal()].toLowerCase();
	}

	@Override
	public void writeJson(bhI_JsonObject json)
	{
		super.writeJson(json);
		
		bhU_Account.cropPassword(m_credentials, bhE_SignUpCredentialType.PASSWORD.ordinal());
		
		bhI_JsonArray creds = bhA_JsonFactory.getInstance().createJsonArray();
		
		for( int i = 0; i < m_credentials.length; i++ )
		{
			creds.addString(m_credentials[i]);
		}
		
		bhJsonHelper.getInstance().putJsonArray(json, bhE_JsonKey.signUpCredentials, creds);
		bhJsonHelper.getInstance().putString(json, bhE_JsonKey.captchaChallenge, m_captchaChallenge);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		init();
		
		super.readJson(json);
		
		bhI_JsonArray creds = bhJsonHelper.getInstance().getJsonArray(json, bhE_JsonKey.signUpCredentials);
		
		for( int i = 0; i < creds.getSize(); i++ )
		{
			m_credentials[i] = creds.getString(i);
		}
		
		bhU_Account.cropPassword(m_credentials, bhE_SignUpCredentialType.PASSWORD.ordinal());
		
		m_captchaChallenge = bhJsonHelper.getInstance().getString(json, bhE_JsonKey.captchaChallenge);
		
		this.toLowerCase();
	}
}
