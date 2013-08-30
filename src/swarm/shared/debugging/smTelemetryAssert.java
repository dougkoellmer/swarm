package swarm.shared.debugging;

import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_ReadsJson;
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
		this.readJson(null, json);
	}
	
	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		factory.getHelper().putString(json_out, smE_JsonKey.assertMessage, m_message);
		factory.getHelper().putString(json_out, smE_JsonKey.platform, m_platform);
	}

	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		m_message = factory.getHelper().getString(json, smE_JsonKey.assertMessage);
		m_platform = factory.getHelper().getString(json, smE_JsonKey.platform);
	}
}
