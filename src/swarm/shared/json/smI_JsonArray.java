package swarm.shared.json;

public interface smI_JsonArray
{
	int getSize();
	
	
	
	boolean getBoolean(int index);
	
	double getDouble(int index);
	
	int getInt(int index);
	
	String getString(int index);
	
	smI_JsonArray getArray(int index);
	
	smI_JsonObject getObject(int index);
	
	
	void addInt(int value);
	
	void addBoolean(boolean value);
	
	void addDouble(double value);
	
	void addString(String value);
	
	void addArray(smI_JsonArray value);
	
	void addObject(smI_JsonObject value);
}
