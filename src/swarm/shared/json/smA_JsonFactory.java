package swarm.shared.json;

import swarm.shared.reflection.smI_Class;


public abstract class smA_JsonFactory
{	
	public smI_JsonObject createJsonObject()
	{
		return this.getJsonObjectClass().newInstance();
	}
	
	public smI_JsonArray createJsonArray()
	{
		return this.getJsonArrayClass().newInstance();
	}
	
	public abstract smJsonHelper getHelper();
	
	public abstract smI_JsonObject createJsonObject(String data);
	
	public abstract smI_JsonArray createJsonArray(String data);
	
	public abstract smI_Class<? extends smI_JsonObject> getJsonObjectClass();
	
	public abstract smI_Class<? extends smI_JsonArray> getJsonArrayClass();
}
