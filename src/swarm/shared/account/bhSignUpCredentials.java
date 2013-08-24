package swarm.shared.account;

import swarm.shared.app.sm;
import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhA_JsonFactory;
import swarm.shared.json.bhI_JsonArray;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.json.bhE_JsonKey;


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
		
		bhI_JsonArray creds = sm.jsonFactory.createJsonArray();
		
		for( int i = 0; i < m_credentials.length; i++ )
		{
			creds.addString(m_credentials[i]);
		}
		
		sm.jsonFactory.getHelper().putJsonArray(json, bhE_JsonKey.signUpCredentials, creds);
		sm.jsonFactory.getHelper().putString(json, bhE_JsonKey.captchaChallenge, m_captchaChallenge);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		init();
		
		super.readJson(json);
		
		bhI_JsonArray creds = sm.jsonFactory.getHelper().getJsonArray(json, bhE_JsonKey.signUpCredentials);
		
		for( int i = 0; i < creds.getSize(); i++ )
		{
			m_credentials[i] = creds.getString(i);
		}
		
		bhU_Account.cropPassword(m_credentials, bhE_SignUpCredentialType.PASSWORD.ordinal());
		
		m_captchaChallenge = sm.jsonFactory.getHelper().getString(json, bhE_JsonKey.captchaChallenge);
		
		this.toLowerCase();
	}
}
