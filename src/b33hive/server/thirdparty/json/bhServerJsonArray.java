package b33hive.server.thirdparty.json;

import org.json.JSONArray;
import org.json.JSONException;
import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.json.bhI_JsonObject;

public class bhServerJsonArray extends Object implements bhI_JsonArray
{
	private int m_currentIndex = 0;
	
	private JSONArray m_object = null;
	
	public bhServerJsonArray()
	{
		m_object = new JSONArray();
	}
	
	bhServerJsonArray(JSONArray source)
	{
		m_object = source;
	}
	
	public bhServerJsonArray(String data) throws JSONException
	{
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
	public bhI_JsonArray getArray(int index)
	{
		try
		{
			return new bhServerJsonArray(m_object.getJSONArray(index));
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public bhI_JsonObject getObject(int index)
	{
		try
		{
			return new bhServerJsonObject(m_object.getJSONObject(index));
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
	public void addArray(bhI_JsonArray value)
	{
		m_object.put(((bhServerJsonArray)value).getNative());
	}

	@Override
	public void addObject(bhI_JsonObject value)
	{
		m_object.put(((bhServerJsonObject)value).getNative());
	}

	@Override
	public int getInt(int index)
	{
		return (int) getDouble(index);
	}
}
