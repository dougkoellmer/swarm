package com.b33hive.client.data;

public class bhU_Caching
{
	public static String calcRandomVersion()
	{
		return "?v=" + Math.random() * Integer.MAX_VALUE + "";
	}
}
