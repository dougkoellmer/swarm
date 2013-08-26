package swarm.server.data.blob;

import java.util.HashMap;

class smLocalBlobCache
{
	private static final class Context
	{
		HashMap<String, smI_Blob> m_cache;
		
		Context()
		{
			
		}
	}
	
	private final ThreadLocal<Context> m_context = new ThreadLocal<Context>();
	
	smLocalBlobCache()
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
	
	private HashMap<String, smI_Blob> getCache(boolean forceCreateIfInContext)
	{
		Context context = m_context.get();
		
		if( context == null )
		{
			return null;
		}
		
		if( context.m_cache == null && forceCreateIfInContext )
		{
			context.m_cache = new HashMap<String, smI_Blob>();
		}
		
		return context.m_cache;
	}
	
	void putBlob(String generatedKey, smI_Blob blob)
	{
		HashMap<String, smI_Blob> cache = this.getCache(true);
		
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
		HashMap<String, smI_Blob> cache = this.getCache(false);
		
		if( cache == null )
		{
			return;
		}
		
		smI_Blob blob = cache.remove(generatedKey);
	}
	
	smI_Blob getBlob(String generatedKey)
	{
		HashMap<String, smI_Blob> cache = this.getCache(false);
		
		if( cache == null )
		{
			return null;
		}
		
		smI_Blob blob = cache.get(generatedKey);
		
		return blob;
	}
}
