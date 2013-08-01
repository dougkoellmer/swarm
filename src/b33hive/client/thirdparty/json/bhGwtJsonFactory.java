package com.b33hive.client.json;

import com.b33hive.shared.json.bhA_JsonFactory;
import com.b33hive.shared.json.bhI_JsonArray;
import com.b33hive.shared.json.bhI_JsonObject;
import com.b33hive.shared.reflection.bhI_Class;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

public class bhGwtJsonFactory extends bhA_JsonFactory
{
	private final bhI_Class<bhI_JsonObject> m_objectClass = new bhI_Class<bhI_JsonObject>()
	{
		@Override
		public bhI_JsonObject newInstance()
		{
			return new bhGwtJsonObject();
		}
	};
	
	private final bhI_Class<bhI_JsonArray> m_arrayClass = new bhI_Class<bhI_JsonArray>()
	{
		@Override
		public bhI_JsonArray newInstance()
		{
			return new bhGwtJsonArray();
		}
	};
	
	@Override
	public bhI_Class<? extends bhI_JsonObject> getJsonObjectClass()
	{
		return m_objectClass;
	}
	
	@Override
	public bhI_JsonObject createJsonObject(String data)
	{
		JSONObject jsonObject = (JSONObject)JSONParser.parseStrict(data);
		return new bhGwtJsonObject(jsonObject);
	}
	
	@Override
	public bhI_JsonArray createJsonArray(String data)
	{
		JSONArray jsonArray = (JSONArray)JSONParser.parseStrict(data);
		return new bhGwtJsonArray(jsonArray);
	}

	@Override
	public bhI_Class<? extends bhI_JsonArray> getJsonArrayClass()
	{
		return m_arrayClass;
	}
}
