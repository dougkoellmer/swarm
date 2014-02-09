package swarm.shared.json;

import java.util.List;


import swarm.shared.utils.U_TypeConversion;
import swarm.shared.app.BaseAppContext;
import swarm.shared.reflection.I_Class;

public class JsonHelper
{
	private final boolean m_verboseKeys;
	
	public JsonHelper(boolean verboseKeys)
	{
		m_verboseKeys = verboseKeys;
	}
	
	public boolean isVerbose()
	{
		return m_verboseKeys;
	}
	
	private String getString(I_JsonKeySource key)
	{
		boolean verbose = isVerbose();
		
		if( verbose )
		{
			return key.getVerboseKey();
		}
		else
		{
			return key.getCompiledKey();
		}
	}
	
	public boolean containsAnyKeys(I_JsonObject json, I_JsonKeySource ... keys)
	{
		for( int i = 0; i < keys.length; i++ )
		{
			if( json.containsKey(keys[i].getCompiledKey()) || json.containsKey(keys[i].getVerboseKey()) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean containsAllKeys(I_JsonObject json, I_JsonKeySource ... keys)
	{
		for( int i = 0; i < keys.length; i++ )
		{
			if( !json.containsKey(keys[i].getCompiledKey()) && !json.containsKey(keys[i].getVerboseKey()) )
			{
				return false;
			}
		}
		
		return true;
	}
	
	public void putEnum(I_JsonObject json, I_JsonKeySource key, Enum value)
	{
		if( isVerbose() )
		{
			putString(json, key, U_TypeConversion.convertEnumToString(value));
		}
		else
		{
			putInt(json, key, value.ordinal());
		}
	}
	
	public <T> T getEnum(I_JsonObject json, I_JsonKeySource key, Enum[] values)
	{
		Integer ordinal = getInt(json, key);
		
		if( ordinal != null )
		{
			Enum value = values[ordinal];
			
			return (T) value;
		}
		
		String name = getString(json, key);
		if( name != null )
		{
			return (T) U_TypeConversion.convertStringToEnum(name, values);
		}
		
		return null;
	}
	
	public void putInt(I_JsonObject json, I_JsonKeySource key, int value)
	{
		String keyString = getString(key);
		
		json.putInt(keyString, value);
	}
	
	public void putBoolean(I_JsonObject json, I_JsonKeySource key, boolean value)
	{
		String keyString = getString(key);
		
		json.putBoolean(keyString, value);
	}
	
	public void putDouble(I_JsonObject json, I_JsonKeySource key, double value)
	{
		String keyString = getString(key);
		
		json.putDouble(keyString, value);
	}
	
	public void putString(I_JsonObject json, I_JsonKeySource key, String value)
	{
		String keyString = getString(key);
		
		json.putString(keyString, value);
	}
	
	public void putJsonObject(I_JsonObject json, I_JsonKeySource key, I_JsonObject value)
	{
		String keyString = getString(key);
		
		json.putJsonObject(keyString, value);
	}
	
	public void putList(A_JsonFactory factory, I_JsonObject json, I_JsonKeySource key, List<? extends Object> list)
	{
		I_JsonArray array = factory.createJsonArray();
		
		for( int i = 0; i < list.size(); i++ )
		{
			addObjectToJsonArray(factory, array, list.get(i));
		}
		
		putJsonArray(json, key, array);
	}
	
	public void putJavaArray(A_JsonFactory factory, I_JsonObject json, I_JsonKeySource key, Object[] values)
	{
		this.putJavaVarArgs(factory, json, key, values);
	}
	
	public void putJavaVarArgs(A_JsonFactory factory, I_JsonObject json, I_JsonKeySource key, Object ... values)
	{
		I_JsonArray array = factory.createJsonArray();
		
		for( int i = 0; i < values.length; i++ )
		{
			addObjectToJsonArray(factory, array, values[i]);
		}
		
		putJsonArray(json, key, array);
	}
	
	private void addObjectToJsonArray(A_JsonFactory factory, I_JsonArray array, Object object)
	{
		if( object instanceof String )
		{
			array.addString((String) object);
		}
		else if( object instanceof I_WritesJson )
		{
			I_WritesJson writesJson = ((I_WritesJson)object);
			I_JsonObject jsonObject = factory.createJsonObject();
			writesJson.writeJson(jsonObject, factory);
			array.addObject(jsonObject);
		}
		else if( object instanceof Integer )
		{
			array.addInt((Integer) object);
		}
	}
	
	public void putJsonArray(I_JsonObject json, I_JsonKeySource key, I_JsonArray value)
	{
		String keyString = getString(key);
		
		json.putArray(keyString, value);
	}
	
	
	public Boolean getBoolean(I_JsonObject json, I_JsonKeySource key)
	{
		if( json.containsKey(key.getCompiledKey()) )
		{
			return json.getBoolean(key.getCompiledKey());
		}
		else if( json.containsKey(key.getVerboseKey()) )
		{
			return json.getBoolean(key.getVerboseKey());
		}
		
		return null;
	}
	
	public Integer getInt(I_JsonObject json, I_JsonKeySource key)
	{
		if( json.containsKey(key.getCompiledKey()) )
		{
			return json.getInt(key.getCompiledKey());
		}
		else if( json.containsKey(key.getVerboseKey()) )
		{
			return json.getInt(key.getVerboseKey());
		}
		
		return null;
	}
	
	public Double getDouble(I_JsonObject json, I_JsonKeySource key)
	{
		if( json.containsKey(key.getCompiledKey()) )
		{
			return json.getDouble(key.getCompiledKey());
		}
		else if( json.containsKey(key.getVerboseKey()) )
		{
			return json.getDouble(key.getVerboseKey());
		}
		
		return null;
	}
	
	public String getString(I_JsonObject json, I_JsonKeySource key)
	{
		if( json.containsKey(key.getCompiledKey()) )
		{
			return json.getString(key.getCompiledKey());
		}
		else if( json.containsKey(key.getVerboseKey()) )
		{
			return json.getString(key.getVerboseKey());
		}
		
		return null;
	}
	
	public Object getObject(I_JsonObject json, I_JsonKeySource key)
	{
		if( json.containsKey(key.getCompiledKey()) )
		{
			return json.getObject(key.getCompiledKey());
		}
		else if( json.containsKey(key.getVerboseKey()) )
		{
			return json.getObject(key.getVerboseKey());
		}
		
		return null;
	}
	
	public I_JsonObject getJsonObject(I_JsonObject json, I_JsonKeySource key)
	{
		if( json.containsKey(key.getCompiledKey()) )
		{
			return json.getJsonObject(key.getCompiledKey());
		}
		else if( json.containsKey(key.getVerboseKey()) )
		{
			return json.getJsonObject(key.getVerboseKey());
		}
		
		return null;
	}
	
	public I_JsonArray getJsonArray(I_JsonObject json, I_JsonKeySource key)
	{
		if( json.containsKey(key.getCompiledKey()) )
		{
			return json.getArray(key.getCompiledKey());
		}
		else if( json.containsKey(key.getVerboseKey()) )
		{
			return json.getArray(key.getVerboseKey());
		}
		
		return null;
	}
	
	/*public <T extends smI_JsonEncodable, U extends List<T>> U getList(smI_JsonObject json, smI_JsonKeySource keySource, Class<T> entryType, Class<U> listType)
	{
		smI_JsonArray array = getJsonArray(json, keySource);
		if( array != null )
		{
			List<T> toReturn = 
		}
	}*/
}
