package b33hive.shared.json;

import b33hive.shared.reflection.bhI_Class;


public abstract class bhA_JsonFactory
{	
	public bhI_JsonObject createJsonObject()
	{
		return this.getJsonObjectClass().newInstance();
	}
	
	public bhI_JsonArray createJsonArray()
	{
		return this.getJsonArrayClass().newInstance();
	}
	
	public abstract bhJsonHelper getHelper();
	
	public abstract bhI_JsonObject createJsonObject(String data);
	
	public abstract bhI_JsonArray createJsonArray(String data);
	
	public abstract bhI_Class<? extends bhI_JsonObject> getJsonObjectClass();
	
	public abstract bhI_Class<? extends bhI_JsonArray> getJsonArrayClass();
}
