package swarm.client.thirdparty.json;

import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smA_JsonObject;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class smGwtJsonObject extends smA_JsonObject implements smI_JsonObject
{
	private JSONObject m_object = null;
	private final smA_JsonFactory m_factory;
	
	public smGwtJsonObject(smA_JsonFactory factory)
	{
		m_factory = factory;
		m_object = new JSONObject();
	}
	
	public smGwtJsonObject(smA_JsonFactory factory, JavaScriptObject nativeJson)
	{
		m_factory = factory;
		m_object = new JSONObject(nativeJson);
	}
	
	smGwtJsonObject(smA_JsonFactory factory, JSONObject source)
	{
		m_factory = factory;
		m_object = source;
	}
	
	public smA_JsonFactory getFactory()
	{
		return m_factory;
	}
	
	public JSONObject getNative()
	{
		return m_object;
	}
	
	@Override
	public boolean getBoolean(String key)
	{
		return m_object.get(key).isBoolean().booleanValue();
	}

	@Override
	public Double getDouble(String key)
	{
		JSONNumber number =  m_object.get(key).isNumber();
		return number != null ? number.doubleValue() : null;
	}

	@Override
	public String getString(String key)
	{
		return m_object.get(key).isString().stringValue();
	}

	@Override
	public smI_JsonArray getArray(String key)
	{
		return new smGwtJsonArray(m_factory, m_object.get(key).isArray());
	}

	@Override
	public smI_JsonObject getJsonObject(String key)
	{
		return new smGwtJsonObject(m_factory, m_object.get(key).isObject());
	}

	@Override
	public void putBoolean(String key, boolean value)
	{
		m_object.put(key, JSONBoolean.getInstance(value));
	}
	
	@Override
	public void putInt(String key, int value)
	{
		m_object.put(key, new JSONNumber(value));
	}

	@Override
	public void putDouble(String key, double value)
	{
		m_object.put(key, new JSONNumber(value));
	}

	@Override
	public void putString(String key, String value)
	{
		m_object.put(key, new JSONString(value));
	}

	@Override
	public void putArray(String key, smI_JsonArray value)
	{
		m_object.put(key, ((smGwtJsonArray) value).getNative());
	}

	@Override
	public void putJsonObject(String key, smI_JsonObject value)
	{
		m_object.put(key, ((smGwtJsonObject) value).getNative());
	}

	@Override
	public Integer getInt(String key)
	{
		Double doubleValue = this.getDouble(key);
		return doubleValue != null ? doubleValue.intValue() : null;
	}

	@Override
	public boolean containsKey(String key)
	{
		return m_object.containsKey(key);
	}
	
	@Override
	public String toString()
	{
		return m_object.toString();
	}

	@Override
	public Object getObject(String key)
	{
		JSONValue value = m_object.get(key);
		
		if( value != null )
		{
			JSONBoolean bool = value.isBoolean();
			
			if( bool != null )
			{
				return (Boolean) bool.booleanValue();
			}
			else
			{
				JSONNumber number = value.isNumber();
				if( number != null )
				{
					return number.doubleValue();
				}
				else
				{
					JSONString string = value.isString();
					
					if( string != null )
					{
						return string.stringValue();
					}
				}
			}
		}

		return null;
	}
	
	@Override
	public String writeString()
	{
		return m_object.toString();
	}
}
