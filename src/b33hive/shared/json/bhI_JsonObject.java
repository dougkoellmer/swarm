package b33hive.shared.json;

public interface bhI_JsonObject
{
	boolean containsKey(String key);
	
	
	boolean getBoolean(String key);
	
	Double getDouble(String key);
	
	Integer getInt(String key);
	
	String getString(String key);
	
	Object getObject(String key);
	
	bhI_JsonArray getArray(String key);
	
	bhI_JsonObject getJsonObject(String key);
	
	
	
	void putBoolean(String key, boolean value);
	
	void putDouble(String key, double value);
	
	void putInt(String key, int value);
	
	void putString(String key, String value);
	
	void putArray(String key, bhI_JsonArray value);
	
	void putJsonObject(String key, bhI_JsonObject value);
	
	String writeString();
	
	boolean isEqualTo(bhI_JsonObject otherObject);
}
