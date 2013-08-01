package com.b33hive.server.json;

import org.json.JSONException;

import com.b33hive.client.json.bhGwtJsonArray;
import com.b33hive.client.json.bhGwtJsonObject;
import com.b33hive.shared.json.bhA_JsonFactory;
import com.b33hive.shared.json.bhI_JsonArray;
import com.b33hive.shared.json.bhI_JsonObject;
import com.b33hive.shared.reflection.bhI_Class;

public class bhServerJsonFactory extends bhA_JsonFactory
{
	private final bhI_Class<bhI_JsonObject> m_objectClass = new bhI_Class<bhI_JsonObject>()
	{
		@Override
		public bhI_JsonObject newInstance()
		{
			return new bhServerJsonObject();
		}
	};
	
	private final bhI_Class<bhI_JsonArray> m_arrayClass = new bhI_Class<bhI_JsonArray>()
	{
		@Override
		public bhI_JsonArray newInstance()
		{
			return new bhServerJsonArray();
		}
	};
	
	@Override
	public bhI_Class<? extends bhI_JsonObject> getJsonObjectClass()
	{
		return m_objectClass;
	}
	
	@Override
	public bhI_Class<? extends bhI_JsonArray> getJsonArrayClass()
	{
		return m_arrayClass;
	}
	
	@Override
	public bhI_JsonObject createJsonObject(String data)
	{
		try
		{
			return new bhServerJsonObject(data);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public bhI_JsonArray createJsonArray(String data)
	{
		try
		{
			return new bhServerJsonArray(data);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
