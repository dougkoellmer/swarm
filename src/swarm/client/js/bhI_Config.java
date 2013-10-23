package com.b33hive.client.data;

public interface bhI_Config
{
	int getInt(String property);
	
	float getFloat(String property);
	
	double getDouble(String property);
	
	boolean getBool(String property);
}
