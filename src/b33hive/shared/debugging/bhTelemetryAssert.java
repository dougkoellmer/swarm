package b33hive.shared.debugging;

import b33hive.shared.app.bh;
import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonEncodable;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;

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
		bh.jsonFactory.getHelper().putString(json, bhE_JsonKey.assertMessage, m_message);
		bh.jsonFactory.getHelper().putString(json, bhE_JsonKey.platform, m_platform);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_message = bh.jsonFactory.getHelper().getString(json, bhE_JsonKey.assertMessage);
		m_platform = bh.jsonFactory.getHelper().getString(json, bhE_JsonKey.platform);
	}
}
