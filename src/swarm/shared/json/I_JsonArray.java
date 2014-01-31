package swarm.shared.json;

public interface I_JsonArray
{
	int getSize();
	
	
	
	boolean getBoolean(int index);
	
	double getDouble(int index);
	
	int getInt(int index);
	
	String getString(int index);
	
	I_JsonArray getArray(int index);
	
	I_JsonObject getObject(int index);
	
	
	void addInt(int value);
	
	void addBoolean(boolean value);
	
	void addDouble(double value);
	
	void addString(String value);
	
	void addArray(I_JsonArray value);
	
	void addObject(I_JsonObject value);
}
