package swarm.shared.json;

import java.util.List;


import swarm.shared.utils.smU_TypeConversion;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.reflection.smI_Class;

public class smJsonHelper
{
	private final boolean m_verboseKeys;
	
	public smJsonHelper(boolean verboseKeys)
	{
		m_verboseKeys = verboseKeys;
	}
	
	public boolean isVerbose()
	{
		return m_verboseKeys;
	}
	
	private String getString(smI_JsonKeySource key)
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
	
	public boolean containsAnyKeys(smI_JsonObject json, smI_JsonKeySource ... keys)
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
	
	public boolean containsAllKeys(smI_JsonObject json, smI_JsonKeySource ... keys)
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
	
	public void putEnum(smI_JsonObject json, smI_JsonKeySource key, Enum value)
	{
		if( isVerbose() )
		{
			putString(json, key, smU_TypeConversion.convertEnumToString(value));
		}
		else
		{
			putInt(json, key, value.ordinal());
		}
	}
	
	public <T> T getEnum(smI_JsonObject json, smI_JsonKeySource key, Enum[] values)
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
			return (T) smU_TypeConversion.convertStringToEnum(name, values);
		}
		
		return null;
	}
	
	public void putInt(smI_JsonObject json, smI_JsonKeySource key, int value)
	{
		String keyString = getString(key);
		
		json.putInt(keyString, value);
	}
	
	public void putBoolean(smI_JsonObject json, smI_JsonKeySource key, boolean value)
	{
		String keyString = getString(key);
		
		json.putBoolean(keyString, value);
	}
	
	public void putDouble(smI_JsonObject json, smI_JsonKeySource key, double value)
	{
		String keyString = getString(key);
		
		json.putDouble(keyString, value);
	}
	
	public void putString(smI_JsonObject json, smI_JsonKeySource key, String value)
	{
		String keyString = getString(key);
		
		json.putString(keyString, value);
	}
	
	public void putJsonObject(smI_JsonObject json, smI_JsonKeySource key, smI_JsonObject value)
	{
		String keyString = getString(key);
		
		json.putJsonObject(keyString, value);
	}
	
	public void putList(smA_JsonFactory factory, smI_JsonObject json, smI_JsonKeySource key, List<? extends Object> list)
	{
		smI_JsonArray array = factory.createJsonArray();
		
		for( int i = 0; i < list.size(); i++ )
		{
			addObjectToJsonArray(factory, array, list.get(i));
		}
		
		putJsonArray(json, key, array);
	}
	
	public void putJavaArray(smA_JsonFactory factory, smI_JsonObject json, smI_JsonKeySource key, Object[] values)
	{
		this.putJavaVarArgs(factory, json, key, values);
	}
	
	public void putJavaVarArgs(smA_JsonFactory factory, smI_JsonObject json, smI_JsonKeySource key, Object ... values)
	{
		smI_JsonArray array = factory.createJsonArray();
		
		for( int i = 0; i < values.length; i++ )
		{
			addObjectToJsonArray(factory, array, values[i]);
		}
		
		putJsonArray(json, key, array);
	}
	
	private void addObjectToJsonArray(smA_JsonFactory factory, smI_JsonArray array, Object object)
	{
		if( object instanceof String )
		{
			array.addString((String) object);
		}
		else if( object instanceof smI_WritesJson )
		{
			smI_WritesJson writesJson = ((smI_WritesJson)object);
			smI_JsonObject jsonObject = factory.createJsonObject();
			writesJson.writeJson(factory, jsonObject);
			array.addObject(jsonObject);
		}
		else if( object instanceof Integer )
		{
			array.addInt((Integer) object);
		}
	}
	
	public void putJsonArray(smI_JsonObject json, smI_JsonKeySource key, smI_JsonArray value)
	{
		String keyString = getString(key);
		
		json.putArray(keyString, value);
	}
	
	
	public Boolean getBoolean(smI_JsonObject json, smI_JsonKeySource key)
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
	
	public Integer getInt(smI_JsonObject json, smI_JsonKeySource key)
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
	
	public Double getDouble(smI_JsonObject json, smI_JsonKeySource key)
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
	
	public String getString(smI_JsonObject json, smI_JsonKeySource key)
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
	
	public Object getObject(smI_JsonObject json, smI_JsonKeySource key)
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
	
	public smI_JsonObject getJsonObject(smI_JsonObject json, smI_JsonKeySource key)
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
	
	public smI_JsonArray getJsonArray(smI_JsonObject json, smI_JsonKeySource key)
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
