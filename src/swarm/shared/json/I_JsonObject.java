package swarm.shared.json;

public interface I_JsonObject
{	
	boolean containsKey(String key);
	
	
	boolean getBoolean(String key);
	
	Double getDouble(String key);
	
	Integer getInt(String key);
	
	String getString(String key);
	
	Object getObject(String key);
	
	I_JsonArray getArray(String key);
	
	I_JsonObject getJsonObject(String key);
	
	
	
	void putBoolean(String key, boolean value);
	
	void putDouble(String key, double value);
	
	void putInt(String key, int value);
	
	void putString(String key, String value);
	
	void putArray(String key, I_JsonArray value);
	
	void putJsonObject(String key, I_JsonObject value);
	
	String writeString();
	
	boolean isEqualTo(I_JsonObject otherObject);
}
