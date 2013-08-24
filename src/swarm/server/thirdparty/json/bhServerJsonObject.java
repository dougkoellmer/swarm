package swarm.server.thirdparty.json;

import org.json.JSONException;
import org.json.JSONObject;

import swarm.shared.json.bhA_JsonObject;
import swarm.shared.json.bhI_JsonArray;
import swarm.shared.json.bhI_JsonObject;

public class bhServerJsonObject extends bhA_JsonObject implements bhI_JsonObject
{
	private JSONObject m_object = null;
	
	public bhServerJsonObject()
	{
		m_object = new JSONObject();
	}
	
	public bhServerJsonObject(String data) throws JSONException
	{
		m_object = new JSONObject(data);
	}
	
	bhServerJsonObject(JSONObject source)
	{
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
	public bhI_JsonArray getArray(String key)
	{
		try
		{
			return new bhServerJsonArray(m_object.getJSONArray(key));
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
		}
		
		return null;
	}

	@Override
	public bhI_JsonObject getJsonObject(String key)
	{
		try
		{
			return new bhServerJsonObject(m_object.getJSONObject(key));
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
	public void putArray(String key, bhI_JsonArray value)
	{
		try
		{
			m_object.put(key, ((bhServerJsonArray)value).getNative());
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void putJsonObject(String key, bhI_JsonObject value)
	{
		try
		{
			m_object.put(key, ((bhServerJsonObject)value).getNative());
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
	public String writeString()
	{
		return m_object.toString();
	}
}
