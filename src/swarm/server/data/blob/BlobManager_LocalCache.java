package swarm.server.data.blob;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;

class BlobManager_LocalCache extends A_BlobManagerWithCache
{
	private final LocalBlobCache m_localCache;
	
	BlobManager_LocalCache(BlobTemplateManager templateMngr, LocalBlobCache localCache, I_BlobManager wrappedManager)
	{
		super(templateMngr, wrappedManager);
		
		m_localCache = localCache;
	}
	
	protected E_BlobCacheLevel getCacheLevel()
	{
		return E_BlobCacheLevel.LOCAL;
	}
	
	/**
	 * WARNING: For minor performance reasons, this method may remove elements from the input lists (instead of internally
	 * 			just creating new lists) before passing it to the underlying blob manager.
	 * 
	 * @param keySources
	 * @param outBlobs
	 * @return
	 * @throws BlobException
	 */
	@Override
	protected Map<I_BlobKey, I_Blob> getBlobsFromCache(Map<I_BlobKey, Class<? extends I_Blob>> values) throws BlobException
	{
		Map<I_BlobKey, I_Blob> toReturn = null;
		
		Iterator<I_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			I_BlobKey keySource = iterator.next();
			Class<? extends I_Blob> blobType = values.get(keySource);
			
			I_Blob blobTemplate = m_templateMngr.getTemplate(blobType);
			
			String generatedKey = keySource.createBlobKey(blobTemplate);
			
			I_Blob cachedBlob = m_localCache.getBlob(generatedKey);
			
			if( cachedBlob != null )
			{
				iterator.remove();
				
				toReturn = toReturn == null ? new HashMap<I_BlobKey, I_Blob>() : toReturn;
				toReturn.put(keySource, cachedBlob);
			}
		}
		
		return toReturn;
	}

	@Override
	protected void putBlobIntoCache(String generatedKey, I_Blob blob) throws BlobException
	{
		m_localCache.putBlob(generatedKey, blob);
	}

	@Override
	protected <T extends I_Blob> I_Blob getBlobFromCache(String generatedKey, Class<? extends T> blobType) throws BlobException
	{
		return m_localCache.getBlob(generatedKey);
	}
	
	@Override
	protected void deleteBlobFromCache(String generatedKey) throws BlobException
	{
		m_localCache.deleteBlob(generatedKey);
	}

	@Override
	protected void putBlobsIntoCache(Map<I_BlobKey, I_Blob> values) throws BlobException
	{
		Iterator<I_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			I_BlobKey keySource = iterator.next();
			I_Blob blob = values.get(keySource);
			
			if( this.isCacheable(blob) )
			{
				this.putBlobIntoCache(keySource.createBlobKey(blob), blob);
			}
		}
	}

	@Override
	protected void deleteBlobsFromCache(Map<I_BlobKey, Class<? extends I_Blob>> values) throws BlobException
	{
		Iterator<I_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			I_BlobKey keySource = iterator.next();
			I_Blob blob = m_templateMngr.getTemplate(values.get(keySource));
			
			if( !isCacheable(blob) )
			{
				continue;
			}

			String key  = keySource.createBlobKey(blob);
			
			this.deleteBlobFromCache(key);
		}
	}
}
