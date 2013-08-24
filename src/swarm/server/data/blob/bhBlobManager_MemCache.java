package swarm.server.data.blob;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

class bhBlobManager_MemCache extends bhA_BlobManagerWithCache
{
	private static final Logger s_logger = Logger.getLogger(bhBlobManager_MemCache.class.getName());
	
	bhBlobManager_MemCache(bhBlobTemplateManager templateMngr, bhI_BlobManager wrappedManager)
	{
		super(templateMngr, wrappedManager);
	}
	
	protected bhE_BlobCacheLevel getCacheLevel()
	{
		return bhE_BlobCacheLevel.MEMCACHE;
	}
	
	@Override
	protected void putBlobIntoCache(String generatedKey, bhI_Blob blob) throws bhBlobException
	{
		AsyncMemcacheService memCache = MemcacheServiceFactory.getAsyncMemcacheService();
		
		byte[] blobBytes = bhU_Blob.convertToBytes(blob);
		
		try
		{
			memCache.put(generatedKey, blobBytes, null, MemcacheService.SetPolicy.SET_ALWAYS);
		}
		catch(Exception e)
		{
			s_logger.log(Level.WARNING, "Could not put value into memcache.", e);
		}
	}
	
	@Override
	protected <T extends bhI_Blob> bhI_Blob getBlobFromCache(String generatedKey, Class<? extends T> blobType) throws bhBlobException
	{
		MemcacheService memCache = MemcacheServiceFactory.getMemcacheService();
		
		byte[] blobBytes = null;
		
		try
		{
			blobBytes = (byte[]) memCache.get(generatedKey);
		}
		catch(Exception e)
		{
			s_logger.log(Level.WARNING, "Could not get value from memcache.", e);
		}
		
		if( blobBytes != null )
		{
			bhI_Blob blob = bhU_Blob.createBlobInstance(blobType);
			bhU_Blob.readBytes(blob, blobBytes);
			
			return blob;
		}
		
		return null;
	}
	
	@Override
	protected void deleteBlobFromCache(String generatedKey) throws bhBlobException
	{
		AsyncMemcacheService memCache = MemcacheServiceFactory.getAsyncMemcacheService();
		
		try
		{
			memCache.delete(generatedKey);
		}
		catch(Exception e)
		{
			s_logger.log(Level.WARNING, "Could not delete value from memcache.", e);
		}
	}
	
	@Override
	protected void deleteBlobsFromCache(Map<bhI_BlobKey, Class<? extends bhI_Blob>> values) throws bhBlobException
	{
		AsyncMemcacheService memCache = MemcacheServiceFactory.getAsyncMemcacheService();
		
		ArrayList<String> keys = null;
		
		Iterator<bhI_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			bhI_BlobKey keySource = iterator.next();
			bhI_Blob blob = m_templateMngr.getTemplate(values.get(keySource));
			
			if( !isCacheable(blob) )
			{
				continue;
			}
				
			keys = keys != null ? keys : new ArrayList<String>();
			
			keys.add(keySource.createBlobKey(blob));
		}
		
		if( keys == null )  return;

		try
		{
			memCache.deleteAll(keys);
		}
		catch(Exception e)
		{
			s_logger.log(Level.WARNING, "Could not delete batch from memcache.", e);
		}
	}
	
	@Override
	protected void putBlobsIntoCache(Map<bhI_BlobKey, bhI_Blob> values) throws bhBlobException
	{
		HashMap<String, byte[]> entries = null;
		
		Iterator<bhI_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			bhI_BlobKey keySource = iterator.next();
			bhI_Blob blob = values.get(keySource);
			
			if( !isCacheable(blob) )
			{
				continue;
			}
				
			entries = entries != null ? entries : new HashMap<String, byte[]>();
			
			byte[] blobBytes = bhU_Blob.convertToBytes(blob);
			entries.put(keySource.createBlobKey(blob), blobBytes);
		}
		
		if( entries == null )  return;
		
		AsyncMemcacheService memCache = MemcacheServiceFactory.getAsyncMemcacheService();
		
		try
		{
			memCache.putAll(entries, null);
		}
		catch(Exception e)
		{
			s_logger.log(Level.WARNING, "Could not put batch into memcache.", e);
		}
	}
	
	@Override
	protected Map<bhI_BlobKey, bhI_Blob> getBlobsFromCache(Map<bhI_BlobKey, Class<? extends bhI_Blob>> values) throws bhBlobException
	{
		ArrayList<String> generatedKeys = new ArrayList<String>();
		Map<String, bhBlobTuple> generatedKeysToTuples = new HashMap<String, bhBlobTuple>();
		
		Iterator<? extends bhI_BlobKey> keySourceIterator = values.keySet().iterator();
		while(keySourceIterator.hasNext() )
		{
			bhI_BlobKey keySource = keySourceIterator.next();
			Class<? extends bhI_Blob> blobType = values.get(keySource);
			bhI_Blob blobTemplate = m_templateMngr.getTemplate(blobType);
			
			String generatedKey = keySource.createBlobKey(blobTemplate);
			generatedKeys.add(keySource.createBlobKey(blobTemplate));
			generatedKeysToTuples.put(generatedKey, new bhBlobTuple(keySource, blobType));
		}
		
		MemcacheService memCache = MemcacheServiceFactory.getMemcacheService();
		Map<String, Object> result = null;
		try
		{
			result = memCache.getAll(generatedKeys);
		}
		catch(Exception e)
		{
			s_logger.log(Level.WARNING, "Could not get batch values from memcache.", e);
			
			return null;
		}
		
		if( result == null || result != null && result.size() == 0 )
		{
			return null;
		}
		
		HashMap<bhI_BlobKey, bhI_Blob> toReturn = new HashMap<bhI_BlobKey, bhI_Blob>();
		
		for( String generatedKey : result.keySet() )
		{
			byte[] blobBytes = (byte[]) result.get(generatedKey);
			
			if( blobBytes == null )  continue;
			
			bhBlobTuple tuple = generatedKeysToTuples.get(generatedKey);
	
			bhI_Blob blob = bhU_Blob.createBlobInstance(tuple.m_blobType);
			bhU_Blob.readBytes(blob, blobBytes);
			
			toReturn.put(tuple.m_keySource, blob);
		}
		
		return toReturn;
	}
}
