package swarm.client.thirdparty.json;

import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONString;

public class smGwtJsonArray extends Object implements smI_JsonArray
{
	private JSONArray m_object = null;
	private final smA_JsonFactory m_factory;
	
	public smGwtJsonArray(smA_JsonFactory factory)
	{
		m_factory = factory;
		m_object = new JSONArray();
	}
	
	smGwtJsonArray(smA_JsonFactory factory, JSONArray source)
	{
		m_factory = factory;
		m_object = source;
	}
	
	JSONArray getNative()
	{
		return m_object;
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
	public smI_JsonArray getArray(int index)
	{
		return new smGwtJsonArray(m_factory, m_object.get(index).isArray());
	}

	@Override
	public smI_JsonObject getObject(int index)
	{
		return new smGwtJsonObject(m_factory, m_object.get(index).isObject());
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
	public void addArray(smI_JsonArray value)
	{
		m_object.set(m_object.size(), ((smGwtJsonArray) value).getNative());
	}

	@Override
	public void addObject(smI_JsonObject value)
	{
		m_object.set(m_object.size(), ((smGwtJsonObject) value).getNative());
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
