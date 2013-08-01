package com.b33hive.shared.json;

import com.b33hive.shared.reflection.bhI_Class;


public abstract class bhA_JsonFactory
{
	private static bhA_JsonFactory s_instance = null;
	
	public bhA_JsonFactory()
	{
		s_instance = this;
	}
	
	public static bhA_JsonFactory getInstance()
	{
		return s_instance;
	}
	
	public bhI_JsonObject createJsonObject()
	{
		return this.getJsonObjectClass().newInstance();
	}
	
	public bhI_JsonArray createJsonArray()
	{
		return this.getJsonArrayClass().newInstance();
	}
	
	public abstract bhI_JsonObject createJsonObject(String data);
	
	public abstract bhI_JsonArray createJsonArray(String data);
	
	public abstract bhI_Class<? extends bhI_JsonObject> getJsonObjectClass();
	
	public abstract bhI_Class<? extends bhI_JsonArray> getJsonArrayClass();
}
