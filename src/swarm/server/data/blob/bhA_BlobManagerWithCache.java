package swarm.server.data.blob;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

abstract class bhA_BlobManagerWithCache extends bhA_BlobManager
{
	private final bhI_BlobManager m_wrappedManager;
	protected final bhBlobTemplateManager m_templateMngr;
	
	bhA_BlobManagerWithCache(bhBlobTemplateManager templateMngr, bhI_BlobManager wrappedManager)
	{
		super();
		
		m_templateMngr = templateMngr;
		m_wrappedManager = wrappedManager;
	}
	
	protected abstract bhE_BlobCacheLevel getCacheLevel();
	
	//--- DRK > The following abstract methods somewhat mirror the interface methods, and are
	//---		overridden instead of the interface methods, which are finalized here.
	protected abstract void putBlobIntoCache(String generatedKey, bhI_Blob blob) throws bhBlobException;
	protected abstract <T extends bhI_Blob> bhI_Blob getBlobFromCache(String generatedKey, Class<? extends T> blobType) throws bhBlobException;
	protected abstract Map<bhI_BlobKey, bhI_Blob> getBlobsFromCache(Map<bhI_BlobKey, Class<? extends bhI_Blob>> values) throws bhBlobException;
	protected abstract void deleteBlobFromCache(String generatedKey) throws bhBlobException;
	protected abstract void deleteBlobsFromCache(Map<bhI_BlobKey, Class<? extends bhI_Blob>> values) throws bhBlobException;
	protected abstract void putBlobsIntoCache(Map<bhI_BlobKey, bhI_Blob> values) throws bhBlobException;
	
	private void private_putBlobIntoCache(String generatedKey, bhI_Blob blob) throws bhBlobException
	{
		if( this.isCacheable(blob) )
		{
			this.putBlobIntoCache(generatedKey, blob);
		}
	}
	
	protected boolean isCacheable(bhI_Blob blob)
	{
		return blob.getMaximumCacheLevel().ordinal() <= this.getCacheLevel().ordinal();
	}
	
	@Override
	public final void putBlob(bhI_BlobKey keySource, bhI_Blob blob) throws bhBlobException, ConcurrentModificationException
	{
		if( m_wrappedManager != null )
		{
			m_wrappedManager.putBlob(keySource, blob);
		}
		
		this.private_putBlobIntoCache(keySource.createBlobKey(blob), blob);
	}
	
	@Override
	public final <T extends bhI_Blob> T getBlob(bhI_BlobKey keySource, Class<? extends T> blobType) throws bhBlobException
	{
		bhI_Blob template = m_templateMngr.getTemplate(blobType);
		
		String generatedKey = keySource.createBlobKey(template);
		bhI_Blob cachedBlob = this.getBlobFromCache(generatedKey, blobType);
		
		if( cachedBlob != null )
		{
			return (T) cachedBlob;
		}
		
		if( m_wrappedManager == null )  return null;
		
		bhI_Blob blob = m_wrappedManager.getBlob(keySource, blobType);
		
		if( blob != null )
		{
			this.private_putBlobIntoCache(generatedKey, blob);
		}
		
		return (T) blob;
	}
	
	@Override
	public final void deleteBlob(bhI_BlobKey keySource, Class<? extends bhI_Blob> blobType) throws bhBlobException
	{
		bhI_Blob blobTemplate = m_templateMngr.getTemplate(blobType);
		
		this.deleteBlobFromCache(keySource.createBlobKey(blobTemplate));
		
		if( m_wrappedManager != null )
		{
			m_wrappedManager.deleteBlob(keySource, blobType);
		}
	}
	
	@Override
	public final void deleteBlobAsync(bhI_BlobKey keySource, Class<? extends bhI_Blob> blobType) throws bhBlobException
	{
		this.deleteBlob(keySource, blobType);
	}
	
	@Override
	public final void deleteBlobs(Map<bhI_BlobKey, Class<? extends bhI_Blob>> values) throws bhBlobException
	{
		this.deleteBlobsFromCache(values);
		
		if( m_wrappedManager != null )
		{
			m_wrappedManager.deleteBlobs(values);
		}
	}
	
	@Override
	public final void deleteBlobsAsync(Map<bhI_BlobKey, Class<? extends bhI_Blob>> values) throws bhBlobException
	{
		this.deleteBlobs(values);
	}
	
	@Override
	public final void putBlobs(Map<bhI_BlobKey, bhI_Blob> values) throws bhBlobException
	{
		if( this.m_wrappedManager != null)
		{
			m_wrappedManager.putBlobs(values);
		}
		
		this.putBlobsIntoCache(values);
	}
	
	@Override
	public final Map<bhI_BlobKey, bhI_Blob> getBlobs(Map<bhI_BlobKey, Class<? extends bhI_Blob>> values) throws bhBlobException
	{
		Map<bhI_BlobKey, bhI_Blob> cachedBlobs = this.getBlobsFromCache(values);
		
		if( values.size() == 0 )
		{
			return cachedBlobs;
		}
		else
		{
			if( m_wrappedManager == null )  return cachedBlobs;
			
			if( values.size() == 1 )
			{
				Iterator<bhI_BlobKey> iterator = values.keySet().iterator();
				bhI_BlobKey soloKeySource = iterator.next();
				Class<? extends bhI_Blob> soloBlobType = values.get(soloKeySource);
				
				bhI_Blob blobFromWrappedManager = m_wrappedManager.getBlob(soloKeySource, soloBlobType);
				
				Map<bhI_BlobKey, bhI_Blob> toReturn = null;
				
				if( cachedBlobs == null )
				{
					if( blobFromWrappedManager != null )
					{
						toReturn = new HashMap<bhI_BlobKey, bhI_Blob>();
					}
				}
				else
				{
					toReturn = cachedBlobs;
				}
				
				if( blobFromWrappedManager != null )
				{
					toReturn.put(soloKeySource, blobFromWrappedManager);
					
					this.private_putBlobIntoCache(soloKeySource.createBlobKey(blobFromWrappedManager), blobFromWrappedManager);
				}
				
				return toReturn;
			}
			else if( values.size() > 1 )
			{
				Map<bhI_BlobKey, bhI_Blob> blobsFromWrappedManager = m_wrappedManager.getBlobs(values);
				
				if( blobsFromWrappedManager != null )
				{
					this.putBlobsIntoCache(blobsFromWrappedManager);
				}
				
				if( cachedBlobs != null )
				{
					if( blobsFromWrappedManager == null )
					{
						return cachedBlobs;
					}
					else
					{
						Iterator<bhI_BlobKey> iterator = cachedBlobs.keySet().iterator();
						while( iterator.hasNext() )
						{
							bhI_BlobKey keySourceFromCache = iterator.next();
							bhI_Blob blobFromCache = cachedBlobs.get(keySourceFromCache);
							
							blobsFromWrappedManager.put(keySourceFromCache, blobFromCache);
						}
						
						return blobsFromWrappedManager;
					}
				}
				else
				{
					return blobsFromWrappedManager;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public Map<bhI_BlobKey, bhI_Blob> performQuery(bhBlobQuery query) throws bhBlobException
	{
		//--- DRK > Probably never going to support queries here.
		if( this.m_wrappedManager != null )
		{
			return this.m_wrappedManager.performQuery(query);
		}
		
		return null;
	}
	
	@Override
	public final void putBlobAsync(bhI_BlobKey keySource, bhI_Blob blob) throws bhBlobException
	{
		//--- DRK > Probably never going to support queries here.
		if( this.m_wrappedManager != null )
		{
			this.m_wrappedManager.putBlobAsync(keySource, blob);
		}
		
		this.putBlobIntoCache(keySource.createBlobKey(blob), blob);
	}
	
	@Override
	public final void putBlobsAsync(Map<bhI_BlobKey, bhI_Blob> values) throws bhBlobException
	{
		//--- DRK > Probably never going to support queries here.
		if( this.m_wrappedManager != null )
		{
			this.m_wrappedManager.putBlobsAsync(values);
		}
		
		this.putBlobsIntoCache(values);
	}
}
