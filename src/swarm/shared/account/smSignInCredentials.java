package swarm.shared.account;

import java.util.Locale;

import swarm.shared.app.sm;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;


public class smSignInCredentials extends smA_AccountCredentials
{
	private boolean m_isForNewPassword = false;
	
	public smSignInCredentials(smI_JsonObject json)
	{
		super(json);
		
		init();
	}
	
	public smSignInCredentials(boolean rememberMe, String... args)
	{
		super(rememberMe);
		
		init();
		
		for( int i = 0; i < args.length; i++ )
		{
			m_credentials[i] = args[i];
		}
	}
	
	public void setIsForNewPassword(boolean value)
	{
		m_isForNewPassword = value;
	}
	
	public boolean isForNewPassword()
	{
		return m_isForNewPassword;
	}
	
	private void init()
	{
		if( m_credentials == null )
		{
			m_credentials = new String[smE_SignInCredentialType.values().length];
		}
	}
	
	public String get(smE_SignInCredentialType eType)
	{
		return m_credentials[eType.ordinal()];
	}
	
	private void toLowerCase()
	{
		m_credentials[smE_SignInCredentialType.EMAIL.ordinal()] = m_credentials[smE_SignInCredentialType.EMAIL.ordinal()].toLowerCase();
	}

	@Override
	public void writeJson(smI_JsonObject json)
	{
		super.writeJson(json);
		
		smU_Account.cropPassword(m_credentials, smE_SignInCredentialType.PASSWORD.ordinal());
		
		smI_JsonArray creds = sm.jsonFactory.createJsonArray();
		
		for( int i = 0; i < m_credentials.length; i++ )
		{
			creds.addString(m_credentials[i]);
		}
		
		sm.jsonFactory.getHelper().putJsonArray(json, smE_JsonKey.signInCredentials, creds);
	}

	@Override
	public void readJson(smI_JsonObject json)
	{
		init();
		
		super.readJson(json);
		
		smI_JsonArray creds = sm.jsonFactory.getHelper().getJsonArray(json, smE_JsonKey.signInCredentials);
		
		for( int i = 0; i < creds.getSize(); i++ )
		{
			m_credentials[i] = creds.getString(i);
		}
		
		smU_Account.cropPassword(m_credentials, smE_SignInCredentialType.PASSWORD.ordinal());
		
		this.toLowerCase();
	}
}
