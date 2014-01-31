package swarm.shared.account;

import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.json.E_JsonKey;


public class SignUpCredentials extends A_AccountCredentials
{
	private String m_captchaChallenge;
	
	public SignUpCredentials(A_JsonFactory jsonFactory, I_JsonObject json)
	{
		super(jsonFactory, json);
		
		init();
	}
	
	public SignUpCredentials(boolean rememberMe, String... args)
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
			m_credentials = new String[E_SignUpCredentialType.values().length];
		}
	}
	
	public String get(E_SignUpCredentialType eType)
	{
		return m_credentials[eType.ordinal()];
	}
	
	public String getCaptchaChallenge()
	{
		return m_captchaChallenge;
	}
	
	private void toLowerCase()
	{
		m_credentials[E_SignInCredentialType.EMAIL.ordinal()] = m_credentials[E_SignInCredentialType.EMAIL.ordinal()].toLowerCase();
		m_credentials[E_SignUpCredentialType.USERNAME.ordinal()] = m_credentials[E_SignUpCredentialType.USERNAME.ordinal()].toLowerCase();
	}

	@Override
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		super.writeJson(factory, json_out);
		
		U_Account.cropPassword(m_credentials, E_SignUpCredentialType.PASSWORD.ordinal());
		
		I_JsonArray creds = factory.createJsonArray();
		
		for( int i = 0; i < m_credentials.length; i++ )
		{
			creds.addString(m_credentials[i]);
		}
		
		factory.getHelper().putJsonArray(json_out, E_JsonKey.signUpCredentials, creds);
		factory.getHelper().putString(json_out, E_JsonKey.captchaChallenge, m_captchaChallenge);
	}

	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		init();
		
		super.readJson(factory, json);
		
		I_JsonArray creds = factory.getHelper().getJsonArray(json, E_JsonKey.signUpCredentials);
		
		for( int i = 0; i < creds.getSize(); i++ )
		{
			m_credentials[i] = creds.getString(i);
		}
		
		U_Account.cropPassword(m_credentials, E_SignUpCredentialType.PASSWORD.ordinal());
		
		m_captchaChallenge = factory.getHelper().getString(json, E_JsonKey.captchaChallenge);
		
		this.toLowerCase();
	}
}
