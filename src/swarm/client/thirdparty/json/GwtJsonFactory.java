package swarm.client.thirdparty.json;

import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.reflection.I_Class;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

public class GwtJsonFactory extends A_JsonFactory
{
	private final I_Class<I_JsonObject> m_objectClass = new I_Class<I_JsonObject>()
	{
		@Override
		public I_JsonObject newInstance()
		{
			return new GwtJsonObject(GwtJsonFactory.this);
		}
	};
	
	private final I_Class<I_JsonArray> m_arrayClass = new I_Class<I_JsonArray>()
	{
		@Override
		public I_JsonArray newInstance()
		{
			return new GwtJsonArray(GwtJsonFactory.this);
		}
	};
	
	private final JsonHelper m_jsonHelper;
	
	public GwtJsonFactory(boolean verboseKeys)
	{
		m_jsonHelper = new JsonHelper(verboseKeys);
	}
	
	@Override
	public I_Class<? extends I_JsonObject> getJsonObjectClass()
	{
		return m_objectClass;
	}
	
	@Override
	public I_JsonObject createJsonObject(String data)
	{
		JSONObject jsonObject = (JSONObject)JSONParser.parseStrict(data);
		return new GwtJsonObject(this, jsonObject);
	}
	
	@Override
	public I_JsonArray createJsonArray(String data)
	{
		JSONArray jsonArray = (JSONArray)JSONParser.parseStrict(data);
		return new GwtJsonArray(this, jsonArray);
	}

	@Override
	public I_Class<? extends I_JsonArray> getJsonArrayClass()
	{
		return m_arrayClass;
	}

	@Override
	public JsonHelper getHelper()
	{
		return m_jsonHelper;
	}
}
