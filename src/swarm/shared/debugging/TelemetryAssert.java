package swarm.shared.debugging;

import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_ReadsJson;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

public class TelemetryAssert extends A_JsonEncodable
{
	protected String m_message;
	protected String m_platform;
	
	public TelemetryAssert(String message, String browser)
	{
		m_message = message;
		m_platform = browser;
	}
	
	public TelemetryAssert(A_JsonFactory jsonFactory, I_JsonObject json)
	{
		this.readJson(jsonFactory, json);
	}
	
	@Override
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		factory.getHelper().putString(json_out, E_JsonKey.assertMessage, m_message);
		factory.getHelper().putString(json_out, E_JsonKey.platform, m_platform);
	}

	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		m_message = factory.getHelper().getString(json, E_JsonKey.assertMessage);
		m_platform = factory.getHelper().getString(json, E_JsonKey.platform);
	}
}
