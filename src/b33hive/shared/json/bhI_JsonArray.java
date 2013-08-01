package com.b33hive.shared.json;

public interface bhI_JsonArray
{
	int getSize();
	
	
	
	boolean getBoolean(int index);
	
	double getDouble(int index);
	
	int getInt(int index);
	
	String getString(int index);
	
	bhI_JsonArray getArray(int index);
	
	bhI_JsonObject getObject(int index);
	
	
	void addInt(int value);
	
	void addBoolean(boolean value);
	
	void addDouble(double value);
	
	void addString(String value);
	
	void addArray(bhI_JsonArray value);
	
	void addObject(bhI_JsonObject value);
}
