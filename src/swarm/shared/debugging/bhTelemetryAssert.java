package swarm.shared.debugging;

import swarm.shared.app.sm;
import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonEncodable;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;

public class bhTelemetryAssert extends bhA_JsonEncodable
{
	protected String m_message;
	protected String m_platform;
	
	public bhTelemetryAssert(String message, String browser)
	{
		m_message = message;
		m_platform = browser;
	}
	
	public bhTelemetryAssert(bhI_JsonObject json)
	{
		this.readJson(json);
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		sm.jsonFactory.getHelper().putString(json, bhE_JsonKey.assertMessage, m_message);
		sm.jsonFactory.getHelper().putString(json, bhE_JsonKey.platform, m_platform);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_message = sm.jsonFactory.getHelper().getString(json, bhE_JsonKey.assertMessage);
		m_platform = sm.jsonFactory.getHelper().getString(json, bhE_JsonKey.platform);
	}
}
