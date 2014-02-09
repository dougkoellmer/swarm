package swarm.shared.account;

import java.util.Locale;

import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;


public class SignInCredentials extends A_AccountCredentials
{
	private boolean m_isForNewPassword = false;
	private String m_passwordChangeToken;
	
	public SignInCredentials(A_JsonFactory jsonFactory, I_JsonObject json)
	{
		super(jsonFactory, json);
		
		init();
	}
	
	public void setPasswordChangeToken(String token)
	{
		m_passwordChangeToken = token;
	}
	
	public SignInCredentials(boolean rememberMe, String... args)
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
			m_credentials = new String[E_SignInCredentialType.values().length];
		}
	}
	
	public String get(E_SignInCredentialType eType)
	{
		return m_credentials[eType.ordinal()];
	}
	
	private void toLowerCase()
	{
		m_credentials[E_SignInCredentialType.EMAIL.ordinal()] = m_credentials[E_SignInCredentialType.EMAIL.ordinal()].toLowerCase();
	}

	@Override
	public void writeJson(I_JsonObject json_out, A_JsonFactory factory)
	{
		super.writeJson(json_out, factory);
		
		U_Account.cropPassword(m_credentials, E_SignInCredentialType.PASSWORD.ordinal());
		
		I_JsonArray creds = factory.createJsonArray();
		
		for( int i = 0; i < m_credentials.length; i++ )
		{
			creds.addString(m_credentials[i]);
		}
		
		factory.getHelper().putJsonArray(json_out, E_JsonKey.signInCredentials, creds);
		
		if( m_passwordChangeToken != null )
		{
			factory.getHelper().putString(json_out, E_JsonKey.passwordChangeToken, m_passwordChangeToken);
		}
	}

	@Override
	public void readJson(I_JsonObject json, A_JsonFactory factory)
	{
		init();
		
		super.readJson(json, factory);
		
		I_JsonArray creds = factory.getHelper().getJsonArray(json, E_JsonKey.signInCredentials);
		
		for( int i = 0; i < creds.getSize(); i++ )
		{
			m_credentials[i] = creds.getString(i);
		}
		
		U_Account.cropPassword(m_credentials, E_SignInCredentialType.PASSWORD.ordinal());
		
		this.toLowerCase();
	}
}
