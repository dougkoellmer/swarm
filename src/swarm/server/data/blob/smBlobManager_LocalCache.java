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

class smBlobManager_LocalCache extends smA_BlobManagerWithCache
{
	private final smLocalBlobCache m_localCache;
	
	smBlobManager_LocalCache(smBlobTemplateManager templateMngr, smLocalBlobCache localCache, smI_BlobManager wrappedManager)
	{
		super(templateMngr, wrappedManager);
		
		m_localCache = localCache;
	}
	
	protected smE_BlobCacheLevel getCacheLevel()
	{
		return smE_BlobCacheLevel.LOCAL;
	}
	
	/**
	 * WARNING: For minor performance reasons, this method may remove elements from the input lists (instead of internally
	 * 			just creating new lists) before passing it to the underlying blob manager.
	 * 
	 * @param keySources
	 * @param outBlobs
	 * @return
	 * @throws smBlobException
	 */
	@Override
	protected Map<smI_BlobKey, smI_Blob> getBlobsFromCache(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws smBlobException
	{
		Map<smI_BlobKey, smI_Blob> toReturn = null;
		
		Iterator<smI_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			smI_BlobKey keySource = iterator.next();
			Class<? extends smI_Blob> blobType = values.get(keySource);
			
			smI_Blob blobTemplate = m_templateMngr.getTemplate(blobType);
			
			String generatedKey = keySource.createBlobKey(blobTemplate);
			
			smI_Blob cachedBlob = m_localCache.getBlob(generatedKey);
			
			if( cachedBlob != null )
			{
				iterator.remove();
				
				toReturn = toReturn == null ? new HashMap<smI_BlobKey, smI_Blob>() : toReturn;
				toReturn.put(keySource, cachedBlob);
			}
		}
		
		return toReturn;
	}

	@Override
	protected void putBlobIntoCache(String generatedKey, smI_Blob blob) throws smBlobException
	{
		m_localCache.putBlob(generatedKey, blob);
	}

	@Override
	protected <T extends smI_Blob> smI_Blob getBlobFromCache(String generatedKey, Class<? extends T> blobType) throws smBlobException
	{
		return m_localCache.getBlob(generatedKey);
	}
	
	@Override
	protected void deleteBlobFromCache(String generatedKey) throws smBlobException
	{
		m_localCache.deleteBlob(generatedKey);
	}

	@Override
	protected void putBlobsIntoCache(Map<smI_BlobKey, smI_Blob> values) throws smBlobException
	{
		Iterator<smI_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			smI_BlobKey keySource = iterator.next();
			smI_Blob blob = values.get(keySource);
			
			if( this.isCacheable(blob) )
			{
				this.putBlobIntoCache(keySource.createBlobKey(blob), blob);
			}
		}
	}

	@Override
	protected void deleteBlobsFromCache(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws smBlobException
	{
		Iterator<smI_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			smI_BlobKey keySource = iterator.next();
			smI_Blob blob = m_templateMngr.getTemplate(values.get(keySource));
			
			if( !isCacheable(blob) )
			{
				continue;
			}

			String key  = keySource.createBlobKey(blob);
			
			this.deleteBlobFromCache(key);
		}
	}
}
