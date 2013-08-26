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

class smBlobManager_MemCache extends smA_BlobManagerWithCache
{
	private static final Logger s_logger = Logger.getLogger(smBlobManager_MemCache.class.getName());
	
	bhBlobManager_MemCache(smBlobTemplateManager templateMngr, smI_BlobManager wrappedManager)
	{
		super(templateMngr, wrappedManager);
	}
	
	protected smE_BlobCacheLevel getCacheLevel()
	{
		return smE_BlobCacheLevel.MEMCACHE;
	}
	
	@Override
	protected void putBlobIntoCache(String generatedKey, smI_Blob blob) throws bhBlobException
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
	protected <T extends smI_Blob> smI_Blob getBlobFromCache(String generatedKey, Class<? extends T> blobType) throws bhBlobException
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
			smI_Blob blob = bhU_Blob.createBlobInstance(blobType);
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
	protected void deleteBlobsFromCache(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws bhBlobException
	{
		AsyncMemcacheService memCache = MemcacheServiceFactory.getAsyncMemcacheService();
		
		ArrayList<String> keys = null;
		
		Iterator<smI_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			smI_BlobKey keySource = iterator.next();
			smI_Blob blob = m_templateMngr.getTemplate(values.get(keySource));
			
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
	protected void putBlobsIntoCache(Map<smI_BlobKey, smI_Blob> values) throws bhBlobException
	{
		HashMap<String, byte[]> entries = null;
		
		Iterator<smI_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			smI_BlobKey keySource = iterator.next();
			smI_Blob blob = values.get(keySource);
			
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
	protected Map<smI_BlobKey, smI_Blob> getBlobsFromCache(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws bhBlobException
	{
		ArrayList<String> generatedKeys = new ArrayList<String>();
		Map<String, smBlobTuple> generatedKeysToTuples = new HashMap<String, smBlobTuple>();
		
		Iterator<? extends smI_BlobKey> keySourceIterator = values.keySet().iterator();
		while(keySourceIterator.hasNext() )
		{
			smI_BlobKey keySource = keySourceIterator.next();
			Class<? extends smI_Blob> blobType = values.get(keySource);
			smI_Blob blobTemplate = m_templateMngr.getTemplate(blobType);
			
			String generatedKey = keySource.createBlobKey(blobTemplate);
			generatedKeys.add(keySource.createBlobKey(blobTemplate));
			generatedKeysToTuples.put(generatedKey, new smBlobTuple(keySource, blobType));
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
		
		HashMap<smI_BlobKey, smI_Blob> toReturn = new HashMap<smI_BlobKey, smI_Blob>();
		
		for( String generatedKey : result.keySet() )
		{
			byte[] blobBytes = (byte[]) result.get(generatedKey);
			
			if( blobBytes == null )  continue;
			
			bhBlobTuple tuple = generatedKeysToTuples.get(generatedKey);
	
			smI_Blob blob = bhU_Blob.createBlobInstance(tuple.m_blobType);
			bhU_Blob.readBytes(blob, blobBytes);
			
			toReturn.put(tuple.m_keySource, blob);
		}
		
		return toReturn;
	}
}
