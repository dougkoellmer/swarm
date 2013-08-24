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

class bhBlobManager_LocalCache extends bhA_BlobManagerWithCache
{
	private final bhLocalBlobCache m_localCache;
	
	bhBlobManager_LocalCache(bhBlobTemplateManager templateMngr, bhLocalBlobCache localCache, bhI_BlobManager wrappedManager)
	{
		super(templateMngr, wrappedManager);
		
		m_localCache = localCache;
	}
	
	protected bhE_BlobCacheLevel getCacheLevel()
	{
		return bhE_BlobCacheLevel.LOCAL;
	}
	
	/**
	 * WARNING: For minor performance reasons, this method may remove elements from the input lists (instead of internally
	 * 			just creating new lists) before passing it to the underlying blob manager.
	 * 
	 * @param keySources
	 * @param outBlobs
	 * @return
	 * @throws bhBlobException
	 */
	@Override
	protected Map<bhI_BlobKey, bhI_Blob> getBlobsFromCache(Map<bhI_BlobKey, Class<? extends bhI_Blob>> values) throws bhBlobException
	{
		Map<bhI_BlobKey, bhI_Blob> toReturn = null;
		
		Iterator<bhI_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			bhI_BlobKey keySource = iterator.next();
			Class<? extends bhI_Blob> blobType = values.get(keySource);
			
			bhI_Blob blobTemplate = m_templateMngr.getTemplate(blobType);
			
			String generatedKey = keySource.createBlobKey(blobTemplate);
			
			bhI_Blob cachedBlob = m_localCache.getBlob(generatedKey);
			
			if( cachedBlob != null )
			{
				iterator.remove();
				
				toReturn = toReturn == null ? new HashMap<bhI_BlobKey, bhI_Blob>() : toReturn;
				toReturn.put(keySource, cachedBlob);
			}
		}
		
		return toReturn;
	}

	@Override
	protected void putBlobIntoCache(String generatedKey, bhI_Blob blob) throws bhBlobException
	{
		m_localCache.putBlob(generatedKey, blob);
	}

	@Override
	protected <T extends bhI_Blob> bhI_Blob getBlobFromCache(String generatedKey, Class<? extends T> blobType) throws bhBlobException
	{
		return m_localCache.getBlob(generatedKey);
	}
	
	@Override
	protected void deleteBlobFromCache(String generatedKey) throws bhBlobException
	{
		m_localCache.deleteBlob(generatedKey);
	}

	@Override
	protected void putBlobsIntoCache(Map<bhI_BlobKey, bhI_Blob> values) throws bhBlobException
	{
		Iterator<bhI_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			bhI_BlobKey keySource = iterator.next();
			bhI_Blob blob = values.get(keySource);
			
			if( this.isCacheable(blob) )
			{
				this.putBlobIntoCache(keySource.createBlobKey(blob), blob);
			}
		}
	}

	@Override
	protected void deleteBlobsFromCache(Map<bhI_BlobKey, Class<? extends bhI_Blob>> values) throws bhBlobException
	{
		Iterator<bhI_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			bhI_BlobKey keySource = iterator.next();
			bhI_Blob blob = m_templateMngr.getTemplate(values.get(keySource));
			
			if( !isCacheable(blob) )
			{
				continue;
			}

			String key  = keySource.createBlobKey(blob);
			
			this.deleteBlobFromCache(key);
		}
	}
}
