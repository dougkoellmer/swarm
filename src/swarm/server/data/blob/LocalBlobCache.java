package swarm.server.data.blob;

import java.util.HashMap;

class LocalBlobCache
{
	private static final class Context
	{
		HashMap<String, I_Blob> m_cache;
		
		Context()
		{
			
		}
	}
	
	private final ThreadLocal<Context> m_context = new ThreadLocal<Context>();
	
	LocalBlobCache()
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
	
	private HashMap<String, I_Blob> getCache(boolean forceCreateIfInContext)
	{
		Context context = m_context.get();
		
		if( context == null )
		{
			return null;
		}
		
		if( context.m_cache == null && forceCreateIfInContext )
		{
			context.m_cache = new HashMap<String, I_Blob>();
		}
		
		return context.m_cache;
	}
	
	void putBlob(String generatedKey, I_Blob blob)
	{
		HashMap<String, I_Blob> cache = this.getCache(true);
		
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
		HashMap<String, I_Blob> cache = this.getCache(false);
		
		if( cache == null )
		{
			return;
		}
		
		I_Blob blob = cache.remove(generatedKey);
	}
	
	I_Blob getBlob(String generatedKey)
	{
		HashMap<String, I_Blob> cache = this.getCache(false);
		
		if( cache == null )
		{
			return null;
		}
		
		I_Blob blob = cache.get(generatedKey);
		
		return blob;
	}
}
