package swarm.shared.account;

import swarm.shared.app.sm;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.json.smE_JsonKey;


public class smSignUpCredentials extends smA_AccountCredentials
{
	private String m_captchaChallenge;
	
	public smSignUpCredentials(smI_JsonObject json)
	{
		super(json);
		
		init();
	}
	
	public smSignUpCredentials(boolean rememberMe, String... args)
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
			m_credentials = new String[smE_SignUpCredentialType.values().length];
		}
	}
	
	public String get(smE_SignUpCredentialType eType)
	{
		return m_credentials[eType.ordinal()];
	}
	
	public String getCaptchaChallenge()
	{
		return m_captchaChallenge;
	}
	
	private void toLowerCase()
	{
		m_credentials[smE_SignInCredentialType.EMAIL.ordinal()] = m_credentials[smE_SignInCredentialType.EMAIL.ordinal()].toLowerCase();
		m_credentials[smE_SignUpCredentialType.USERNAME.ordinal()] = m_credentials[smE_SignUpCredentialType.USERNAME.ordinal()].toLowerCase();
	}

	@Override
	public void writeJson(smI_JsonObject json)
	{
		super.writeJson(json);
		
		smU_Account.cropPassword(m_credentials, smE_SignUpCredentialType.PASSWORD.ordinal());
		
		smI_JsonArray creds = sm.jsonFactory.createJsonArray();
		
		for( int i = 0; i < m_credentials.length; i++ )
		{
			creds.addString(m_credentials[i]);
		}
		
		sm.jsonFactory.getHelper().putJsonArray(json, smE_JsonKey.signUpCredentials, creds);
		sm.jsonFactory.getHelper().putString(json, smE_JsonKey.captchaChallenge, m_captchaChallenge);
	}

	@Override
	public void readJson(smI_JsonObject json)
	{
		init();
		
		super.readJson(json);
		
		smI_JsonArray creds = sm.jsonFactory.getHelper().getJsonArray(json, smE_JsonKey.signUpCredentials);
		
		for( int i = 0; i < creds.getSize(); i++ )
		{
			m_credentials[i] = creds.getString(i);
		}
		
		smU_Account.cropPassword(m_credentials, smE_SignUpCredentialType.PASSWORD.ordinal());
		
		m_captchaChallenge = sm.jsonFactory.getHelper().getString(json, smE_JsonKey.captchaChallenge);
		
		this.toLowerCase();
	}
}
