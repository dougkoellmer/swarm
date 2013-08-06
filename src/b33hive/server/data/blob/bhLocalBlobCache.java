package b33hive.server.data.blob;

import java.util.HashMap;

class bhLocalBlobCache
{
	private static final class Context
	{
		HashMap<String, bhI_Blob> m_cache;
		
		Context()
		{
			
		}
	}
	
	private final ThreadLocal<Context> m_context = new ThreadLocal<Context>();
	
	bhLocalBlobCache()
	{
		
	}
	
	void createContext()
	{
		m_context.set(new Context());
	}
	
	void deleteContext()
	{
		m_context.remove();
	}
	
	private HashMap<String, bhI_Blob> getCache(boolean forceCreateIfInContext)
	{
		Context context = m_context.get();
		
		if( context == null )
		{
			return null;
		}
		
		if( context.m_cache == null && forceCreateIfInContext )
		{
			context.m_cache = new HashMap<String, bhI_Blob>();
		}
		
		return context.m_cache;
	}
	
	void putBlob(String generatedKey, bhI_Blob blob)
	{
		HashMap<String, bhI_Blob> cache = this.getCache(true);
		
		if( cache == null )
		{
			return;
		}
		
		cache.put(generatedKey, blob);
	}
	
	boolean containsBlob(String generatedKey)
	{
		return this.getBlob(generatedKey) != null;
	}
	
	void deleteBlob(String generatedKey)
	{
		HashMap<String, bhI_Blob> cache = this.getCache(false);
		
		if( cache == null )
		{
			return;
		}
		
		bhI_Blob blob = cache.remove(generatedKey);
	}
	
	bhI_Blob getBlob(String generatedKey)
	{
		HashMap<String, bhI_Blob> cache = this.getCache(false);
		
		if( cache == null )
		{
			return null;
		}
		
		bhI_Blob blob = cache.get(generatedKey);
		
		return blob;
	}
}
