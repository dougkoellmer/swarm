package swarm.shared.account;

import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

public abstract class A_AccountCredentials extends A_JsonEncodable
{
	protected String[] m_credentials;
	private boolean m_rememberMe;
	
	public A_AccountCredentials(A_JsonFactory jsonFactory, I_JsonObject json)
	{
		super(jsonFactory, json);
	}
	
	public A_AccountCredentials(boolean rememberMe)
	{
		m_rememberMe = rememberMe;
	}
	
	public boolean rememberMe()
	{
		return m_rememberMe;
	}
	
	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		m_rememberMe = factory.getHelper().getBoolean(json, E_JsonKey.rememberMe);
	}
	
	@Override
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		factory.getHelper().putBoolean(json_out, E_JsonKey.rememberMe, m_rememberMe);
	}
}
