package swarm.client.js;


public class JsConfig implements I_Config
{
	private static JsConfig s_instance = null;
	
	public static void startUp()
	{
		s_instance = new JsConfig();
	}
	
	public static JsConfig getInstance()
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
