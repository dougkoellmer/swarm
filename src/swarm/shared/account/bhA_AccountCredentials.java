package swarm.shared.account;

import swarm.shared.app.sm;
import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;

public abstract class bhA_AccountCredentials extends bhA_JsonEncodable
{
	protected String[] m_credentials;
	private boolean m_rememberMe;
	
	public bhA_AccountCredentials(bhI_JsonObject json)
	{
		super(json);
	}
	
	public bhA_AccountCredentials(boolean rememberMe)
	{
		m_rememberMe = rememberMe;
	}
	
	public boolean rememberMe()
	{
		return m_rememberMe;
	}
	
	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_rememberMe = sm.jsonFactory.getHelper().getBoolean(json, bhE_JsonKey.rememberMe);
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		sm.jsonFactory.getHelper().putBoolean(json, bhE_JsonKey.rememberMe, m_rememberMe);
	}
}
