package com.b33hive.shared.json;

import java.util.List;

import com.b33hive.shared.bhU_TypeConversion;
import com.b33hive.shared.reflection.bhI_Class;

public class bhJsonHelper
{
	private final boolean m_isVerbose;
	
	private static bhI_Class<bhJsonHelper> s_instanceProvider;
	
	public bhJsonHelper(boolean isVerbose)
	{
		m_isVerbose = isVerbose;
	}
	
	public static void startUp(bhI_Class<bhJsonHelper> instanceProvider)
	{
		s_instanceProvider = instanceProvider;
	}
	
	public static bhJsonHelper getInstance()
	{
		return s_instanceProvider.newInstance();
	}
	
	public boolean isVerbose()
	{
		// TODO: Somehow implement this so that for development, you can easily turn on verbose mode to debug json problems.
		return m_isVerbose;
	}
	
	private String getString(bhI_JsonKeySource key)
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
	
	public boolean containsAnyKeys(bhI_JsonObject json, bhI_JsonKeySource ... keys)
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
	
	public boolean containsAllKeys(bhI_JsonObject json, bhI_JsonKeySource ... keys)
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
	
	public void putEnum(bhI_JsonObject json, bhI_JsonKeySource key, Enum value)
	{
		if( isVerbose() )
		{
			putString(json, key, bhU_TypeConversion.convertEnumToString(value));
		}
		else
		{
			putInt(json, key, value.ordinal());
		}
	}
	
	public <T> T getEnum(bhI_JsonObject json, bhI_JsonKeySource key, Enum[] values)
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
			return (T) bhU_TypeConversion.convertStringToEnum(name, values);
		}
		
		return null;
	}
	
	public void putInt(bhI_JsonObject json, bhI_JsonKeySource key, int value)
	{
		String keyString = getString(key);
		
		json.putInt(keyString, value);
	}
	
	public void putBoolean(bhI_JsonObject json, bhI_JsonKeySource key, boolean value)
	{
		String keyString = getString(key);
		
		json.putBoolean(keyString, value);
	}
	
	public void putDouble(bhI_JsonObject json, bhI_JsonKeySource key, double value)
	{
		String keyString = getString(key);
		
		json.putDouble(keyString, value);
	}
	
	public void putString(bhI_JsonObject json, bhI_JsonKeySource key, String value)
	{
		String keyString = getString(key);
		
		json.putString(keyString, value);
	}
	
	public void putJsonObject(bhI_JsonObject json, bhI_JsonKeySource key, bhI_JsonObject value)
	{
		String keyString = getString(key);
		
		json.putJsonObject(keyString, value);
	}
	
	public void putList(bhI_JsonObject json, bhI_JsonKeySource key, List<? extends Object> list)
	{
		bhI_JsonArray array = bhA_JsonFactory.getInstance().createJsonArray();
		
		for( int i = 0; i < list.size(); i++ )
		{
			addObjectToJsonArray(array, list.get(i));
		}
		
		putJsonArray(json, key, array);
	}
	
	public void putJavaArray(bhI_JsonObject json, bhI_JsonKeySource key, Object[] values)
	{
		this.putJavaVarArgs(json, key, values);
	}
	
	public void putJavaVarArgs(bhI_JsonObject json, bhI_JsonKeySource key, Object ... values)
	{
		bhI_JsonArray array = bhA_JsonFactory.getInstance().createJsonArray();
		
		for( int i = 0; i < values.length; i++ )
		{
			addObjectToJsonArray(array, values[i]);
		}
		
		putJsonArray(json, key, array);
	}
	
	private void addObjectToJsonArray(bhI_JsonArray array, Object object)
	{
		if( object instanceof String )
		{
			array.addString((String) object);
		}
		else if( object instanceof bhI_JsonEncodable )
		{
			array.addObject(((bhI_JsonEncodable)object).writeJson());
		}
		else if( object instanceof Integer )
		{
			array.addInt((Integer) object);
		}
	}
	
	public void putJsonArray(bhI_JsonObject json, bhI_JsonKeySource key, bhI_JsonArray value)
	{
		String keyString = getString(key);
		
		json.putArray(keyString, value);
	}
	
	
	public Boolean getBoolean(bhI_JsonObject json, bhI_JsonKeySource key)
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
	
	public Integer getInt(bhI_JsonObject json, bhI_JsonKeySource key)
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
	
	public Double getDouble(bhI_JsonObject json, bhI_JsonKeySource key)
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
	
	public String getString(bhI_JsonObject json, bhI_JsonKeySource key)
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
	
	public Object getObject(bhI_JsonObject json, bhI_JsonKeySource key)
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
	
	public bhI_JsonObject getJsonObject(bhI_JsonObject json, bhI_JsonKeySource key)
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
	
	public bhI_JsonArray getJsonArray(bhI_JsonObject json, bhI_JsonKeySource key)
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
	
	/*public <T extends bhI_JsonEncodable, U extends List<T>> U getList(bhI_JsonObject json, bhI_JsonKeySource keySource, Class<T> entryType, Class<U> listType)
	{
		bhI_JsonArray array = getJsonArray(json, keySource);
		if( array != null )
		{
			List<T> toReturn = 
		}
	}*/
}
