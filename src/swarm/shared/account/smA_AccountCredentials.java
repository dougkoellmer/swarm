package swarm.shared.account;

import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

public abstract class smA_AccountCredentials extends smA_JsonEncodable
{
	protected String[] m_credentials;
	private boolean m_rememberMe;
	
	public smA_AccountCredentials(smI_JsonObject json)
	{
		super(json);
	}
	
	public smA_AccountCredentials(boolean rememberMe)
	{
		m_rememberMe = rememberMe;
	}
	
	public boolean rememberMe()
	{
		return m_rememberMe;
	}
	
	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		m_rememberMe = factory.getHelper().getBoolean(json, smE_JsonKey.rememberMe);
	}
	
	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		factory.getHelper().putBoolean(json_out, smE_JsonKey.rememberMe, m_rememberMe);
	}
}
