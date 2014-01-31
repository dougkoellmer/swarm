package swarm.shared.json;

import swarm.shared.reflection.I_Class;


public abstract class A_JsonFactory
{	
	public I_JsonObject createJsonObject()
	{
		return this.getJsonObjectClass().newInstance();
	}
	
	public I_JsonArray createJsonArray()
	{
		return this.getJsonArrayClass().newInstance();
	}
	
	public abstract JsonHelper getHelper();
	
	public abstract I_JsonObject createJsonObject(String data);
	
	public abstract I_JsonArray createJsonArray(String data);
	
	public abstract I_Class<? extends I_JsonObject> getJsonObjectClass();
	
	public abstract I_Class<? extends I_JsonArray> getJsonArrayClass();
}
