package swarm.client.thirdparty.json;

import swarm.shared.json.bhI_JsonArray;
import swarm.shared.json.bhI_JsonObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONString;

public class bhGwtJsonArray extends Object implements bhI_JsonArray
{
	private JSONArray m_object = null;
	
	public bhGwtJsonArray()
	{
		m_object = new JSONArray();
	}
	
	JSONArray getNative()
	{
		return m_object;
	}
	
	bhGwtJsonArray(JSONArray source)
	{
		m_object = source;
	}
	
	@Override
	public int getSize()
	{
		return m_object.size();
	}

	@Override
	public boolean getBoolean(int index)
	{
		return m_object.get(index).isBoolean().booleanValue();
	}

	@Override
	public double getDouble(int index)
	{
		return m_object.get(index).isNumber().doubleValue();
	}

	@Override
	public String getString(int index)
	{
		return m_object.get(index).isString().stringValue();
	}

	@Override
	public bhI_JsonArray getArray(int index)
	{
		return new bhGwtJsonArray(m_object.get(index).isArray());
	}

	@Override
	public bhI_JsonObject getObject(int index)
	{
		return new bhGwtJsonObject( m_object.get(index).isObject());
	}

	@Override
	public void addBoolean(boolean value)
	{
		m_object.set(m_object.size(), JSONBoolean.getInstance(value));
	}

	@Override
	public void addDouble(double value)
	{
		m_object.set(m_object.size(), new JSONNumber(value));
	}

	@Override
	public void addString(String value)
	{
		m_object.set(m_object.size(), new JSONString(value));
	}

	@Override
	public void addArray(bhI_JsonArray value)
	{
		m_object.set(m_object.size(), ((bhGwtJsonArray) value).getNative());
	}

	@Override
	public void addObject(bhI_JsonObject value)
	{
		m_object.set(m_object.size(), ((bhGwtJsonObject) value).getNative());
	}

	@Override
	public void addInt(int value)
	{
		this.addDouble(value);
	}
	
	@Override
	public String toString()
	{
		return m_object.toString();
	}

	@Override
	public int getInt(int index)
	{
		return (int) getDouble(index);
	}
}
