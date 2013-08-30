package swarm.shared.account;

import swarm.shared.app.smSharedAppContext;
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
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		super.writeJson(factory, json_out);
		
		smU_Account.cropPassword(m_credentials, smE_SignUpCredentialType.PASSWORD.ordinal());
		
		smI_JsonArray creds = factory.createJsonArray();
		
		for( int i = 0; i < m_credentials.length; i++ )
		{
			creds.addString(m_credentials[i]);
		}
		
		factory.getHelper().putJsonArray(json_out, smE_JsonKey.signUpCredentials, creds);
		factory.getHelper().putString(json_out, smE_JsonKey.captchaChallenge, m_captchaChallenge);
	}

	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		init();
		
		super.readJson(factory, json);
		
		smI_JsonArray creds = factory.getHelper().getJsonArray(json, smE_JsonKey.signUpCredentials);
		
		for( int i = 0; i < creds.getSize(); i++ )
		{
			m_credentials[i] = creds.getString(i);
		}
		
		smU_Account.cropPassword(m_credentials, smE_SignUpCredentialType.PASSWORD.ordinal());
		
		m_captchaChallenge = factory.getHelper().getString(json, smE_JsonKey.captchaChallenge);
		
		this.toLowerCase();
	}
}
