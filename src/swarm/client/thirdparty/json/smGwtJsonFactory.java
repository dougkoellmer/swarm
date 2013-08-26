package swarm.client.thirdparty.json;

import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.reflection.smI_Class;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

public class smGwtJsonFactory extends smA_JsonFactory
{
	private final smI_Class<smI_JsonObject> m_objectClass = new smI_Class<smI_JsonObject>()
	{
		@Override
		public smI_JsonObject newInstance()
		{
			return new smGwtJsonObject();
		}
	};
	
	private final smI_Class<smI_JsonArray> m_arrayClass = new smI_Class<smI_JsonArray>()
	{
		@Override
		public smI_JsonArray newInstance()
		{
			return new smGwtJsonArray();
		}
	};
	
	private final smJsonHelper m_jsonHelper;
	
	public smGwtJsonFactory(boolean verboseKeys)
	{
		m_jsonHelper = new smJsonHelper(verboseKeys);
	}
	
	@Override
	public smI_Class<? extends smI_JsonObject> getJsonObjectClass()
	{
		return m_objectClass;
	}
	
	@Override
	public smI_JsonObject createJsonObject(String data)
	{
		JSONObject jsonObject = (JSONObject)JSONParser.parseStrict(data);
		return new smGwtJsonObject(jsonObject);
	}
	
	@Override
	public smI_JsonArray createJsonArray(String data)
	{
		JSONArray jsonArray = (JSONArray)JSONParser.parseStrict(data);
		return new smGwtJsonArray(jsonArray);
	}

	@Override
	public smI_Class<? extends smI_JsonArray> getJsonArrayClass()
	{
		return m_arrayClass;
	}

	@Override
	public smJsonHelper getHelper()
	{
		return m_jsonHelper;
	}
}
