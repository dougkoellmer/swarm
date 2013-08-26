package swarm.shared.debugging;

import swarm.shared.app.sm;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonEncodable;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

public class smTelemetryAssert extends smA_JsonEncodable
{
	protected String m_message;
	protected String m_platform;
	
	public smTelemetryAssert(String message, String browser)
	{
		m_message = message;
		m_platform = browser;
	}
	
	public smTelemetryAssert(smI_JsonObject json)
	{
		this.readJson(json);
	}
	
	@Override
	public void writeJson(smI_JsonObject json)
	{
		sm.jsonFactory.getHelper().putString(json, smE_JsonKey.assertMessage, m_message);
		sm.jsonFactory.getHelper().putString(json, smE_JsonKey.platform, m_platform);
	}

	@Override
	public void readJson(smI_JsonObject json)
	{
		m_message = sm.jsonFactory.getHelper().getString(json, smE_JsonKey.assertMessage);
		m_platform = sm.jsonFactory.getHelper().getString(json, smE_JsonKey.platform);
	}
}
