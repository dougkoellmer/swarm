package swarm.server.thirdparty.json;

import org.json.JSONArray;
import org.json.JSONException;

import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;

public class ServerJsonArray extends Object implements I_JsonArray
{
	private JSONArray m_object = null;
	private final A_JsonFactory m_factory;
	
	public ServerJsonArray(A_JsonFactory factory)
	{
		m_factory = factory;
		m_object = new JSONArray();
	}
	
	ServerJsonArray(A_JsonFactory factory, JSONArray source)
	{
		m_factory = factory;
		m_object = source;
	}
	
	public ServerJsonArray(A_JsonFactory factory, String data) throws JSONException
	{
		m_factory = factory;
		m_object = new JSONArray(data);
	}
	
	public JSONArray getNative()
	{
		return m_object;
	}
	
	@Override
	public int getSize()
	{
		return m_object.length();
	}

	@Override
	public boolean getBoolean(int index)
	{
		try
		{
			return m_object.getBoolean(index);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public double getDouble(int index)
	{
		try
		{
			return m_object.getDouble(index);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Double.NaN;
	}

	@Override
	public String getString(int index)
	{
		try
		{
			return m_object.getString(index);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public I_JsonArray getArray(int index)
	{
		try
		{
			return new ServerJsonArray(m_factory, m_object.getJSONArray(index));
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public I_JsonObject getObject(int index)
	{
		try
		{
			return new ServerJsonObject(m_factory, m_object.getJSONObject(index));
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public void addBoolean(boolean value)
	{
		m_object.put(value);
	}

	@Override
	public void addDouble(double value)
	{
		try
		{
			m_object.put(value);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void addInt(int value)
	{
		m_object.put(value);
	}

	@Override
	public void addString(String value)
	{
		m_object.put(value);
	}

	@Override
	public void addArray(I_JsonArray value)
	{
		m_object.put(((ServerJsonArray)value).getNative());
	}

	@Override
	public void addObject(I_JsonObject value)
	{
		m_object.put(((ServerJsonObject)value).getNative());
	}

	@Override
	public int getInt(int index)
	{
		return (int) getDouble(index);
	}
}
