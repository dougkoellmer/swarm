package swarm.server.thirdparty.json;

import org.json.JSONException;
import org.json.JSONObject;

import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.A_JsonObject;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;

public class ServerJsonObject extends A_JsonObject implements I_JsonObject
{
	private JSONObject m_object = null;
	private final A_JsonFactory m_factory;
	
	public ServerJsonObject(A_JsonFactory factory)
	{
		m_factory = factory;
		m_object = new JSONObject();
	}
	
	public ServerJsonObject(A_JsonFactory factory, String data) throws JSONException
	{
		m_factory = factory;
		m_object = new JSONObject(data);
	}
	
	ServerJsonObject(A_JsonFactory factory, JSONObject source)
	{
		m_factory = factory;
		m_object = source;
	}
	
	public JSONObject getNative()
	{
		return m_object;
	}
	
	@Override
	public boolean getBoolean(String key)
	{
		try
		{
			return m_object.getBoolean(key);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
		}
		
		return false;
	}

	@Override
	public Double getDouble(String key)
	{
		try
		{
			return m_object.getDouble(key);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
		}
		
		return null;
	}

	@Override
	public String getString(String key)
	{
		try
		{
			return m_object.getString(key);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
		}

		return null;
	}

	@Override
	public I_JsonArray getArray(String key)
	{
		try
		{
			return new ServerJsonArray(m_factory, m_object.getJSONArray(key));
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
		}
		
		return null;
	}

	@Override
	public I_JsonObject getJsonObject(String key)
	{
		try
		{
			return new ServerJsonObject(m_factory, m_object.getJSONObject(key));
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
		}
		
		return null;
	}
	
	@Override
	public Object getObject(String key)
	{
		try
		{
			return m_object.get(key);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
		}
		
		return null;
	}

	@Override
	public void putBoolean(String key, boolean value)
	{
		try
		{
			m_object.put(key, value);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void putInt(String key, int value)
	{
		try
		{
			m_object.put(key, value);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void putDouble(String key, double value)
	{
		try
		{
			m_object.put(key, value);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void putString(String key, String value)
	{
		try
		{
			m_object.put(key, value);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void putArray(String key, I_JsonArray value)
	{
		try
		{
			m_object.put(key, ((ServerJsonArray)value).getNative());
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void putJsonObject(String key, I_JsonObject value)
	{
		try
		{
			m_object.put(key, ((ServerJsonObject)value).getNative());
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Integer getInt(String key)
	{
		try
		{
			return m_object.getInt(key);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public boolean containsKey(String key)
	{
		return !m_object.isNull(key);
	}
	
	@Override
	public String toString()
	{
		return this.writeString();
	}

	@Override
	public String writeString()
	{
		return m_object.toString();
	}
}
