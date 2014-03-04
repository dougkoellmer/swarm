package swarm.client.js;


public class JsConfig implements I_Config
{
	private final String m_config;
	
	public JsConfig(String configObject)
	{
		m_config = configObject;
	}
	
	@Override
	public native int getInt(String property)
	/*-{
		return $wnd[this.@swarm.client.js.JsConfig::m_config][property];
	}-*/;

	@Override
	public native float getFloat(String property)
	/*-{
		var value = $wnd[this.@swarm.client.js.JsConfig::m_config][property];
		if( value )
		{
			return value;
		}
		
		return 0;
	}-*/;

	@Override
	public native double getDouble(String property)
	/*-{
		return $wnd[this.@swarm.client.js.JsConfig::m_config][property];
	}-*/;

	@Override
	public native boolean getBool(String property)
	/*-{
		return $wnd[this.@swarm.client.js.JsConfig::m_config][property];
	}-*/;
}
