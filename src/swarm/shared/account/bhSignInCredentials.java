package swarm.shared.account;

import java.util.Locale;

import swarm.shared.app.sm;
import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhA_JsonFactory;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonArray;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;


public class bhSignInCredentials extends bhA_AccountCredentials
{
	private boolean m_isForNewPassword = false;
	
	public bhSignInCredentials(bhI_JsonObject json)
	{
		super(json);
		
		init();
	}
	
	public bhSignInCredentials(boolean rememberMe, String... args)
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
			m_credentials = new String[bhE_SignInCredentialType.values().length];
		}
	}
	
	public String get(bhE_SignInCredentialType eType)
	{
		return m_credentials[eType.ordinal()];
	}
	
	private void toLowerCase()
	{
		m_credentials[bhE_SignInCredentialType.EMAIL.ordinal()] = m_credentials[bhE_SignInCredentialType.EMAIL.ordinal()].toLowerCase();
	}

	@Override
	public void writeJson(bhI_JsonObject json)
	{
		super.writeJson(json);
		
		bhU_Account.cropPassword(m_credentials, bhE_SignInCredentialType.PASSWORD.ordinal());
		
		bhI_JsonArray creds = sm.jsonFactory.createJsonArray();
		
		for( int i = 0; i < m_credentials.length; i++ )
		{
			creds.addString(m_credentials[i]);
		}
		
		sm.jsonFactory.getHelper().putJsonArray(json, bhE_JsonKey.signInCredentials, creds);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		init();
		
		super.readJson(json);
		
		bhI_JsonArray creds = sm.jsonFactory.getHelper().getJsonArray(json, bhE_JsonKey.signInCredentials);
		
		for( int i = 0; i < creds.getSize(); i++ )
		{
			m_credentials[i] = creds.getString(i);
		}
		
		bhU_Account.cropPassword(m_credentials, bhE_SignInCredentialType.PASSWORD.ordinal());
		
		this.toLowerCase();
	}
}
