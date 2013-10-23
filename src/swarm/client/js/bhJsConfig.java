package com.b33hive.client.data;

import com.b33hive.client.data.*;

public class bhJsConfig implements bhI_Config
{
	private static bhJsConfig s_instance = null;
	
	public static void startUp()
	{
		s_instance = new bhJsConfig();
	}
	
	public static bhJsConfig getInstance()
	{
		return s_instance;
	}
	
	@Override
	public native int getInt(String property)
	/*-{
		return $wnd[property];
	}-*/;

	@Override
	public native float getFloat(String property)
	/*-{
		var value = $wnd[property];
		if( value )
		{
			return value;
		}
		
		return 0;
	}-*/;

	@Override
	public native double getDouble(String property)
	/*-{
		return $wnd[property];
	}-*/;

	@Override
	public native boolean getBool(String property)
	/*-{
		return $wnd[property];
	}-*/;
}
