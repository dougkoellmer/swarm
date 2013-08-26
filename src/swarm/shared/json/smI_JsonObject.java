package swarm.shared.json;

public interface smI_JsonObject
{
	boolean containsKey(String key);
	
	
	boolean getBoolean(String key);
	
	Double getDouble(String key);
	
	Integer getInt(String key);
	
	String getString(String key);
	
	Object getObject(String key);
	
	smI_JsonArray getArray(String key);
	
	smI_JsonObject getJsonObject(String key);
	
	
	
	void putBoolean(String key, boolean value);
	
	void putDouble(String key, double value);
	
	void putInt(String key, int value);
	
	void putString(String key, String value);
	
	void putArray(String key, smI_JsonArray value);
	
	void putJsonObject(String key, smI_JsonObject value);
	
	String writeString();
	
	boolean isEqualTo(smI_JsonObject otherObject);
}
