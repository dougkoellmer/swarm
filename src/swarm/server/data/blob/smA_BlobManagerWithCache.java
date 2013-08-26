package swarm.server.data.blob;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

abstract class smA_BlobManagerWithCache extends smA_BlobManager
{
	private final smI_BlobManager m_wrappedManager;
	protected final smBlobTemplateManager m_templateMngr;
	
	smA_BlobManagerWithCache(smBlobTemplateManager templateMngr, smI_BlobManager wrappedManager)
	{
		super();
		
		m_templateMngr = templateMngr;
		m_wrappedManager = wrappedManager;
	}
	
	protected abstract smE_BlobCacheLevel getCacheLevel();
	
	//--- DRK > The following abstract methods somewhat mirror the interface methods, and are
	//---		overridden instead of the interface methods, which are finalized here.
	protected abstract void putBlobIntoCache(String generatedKey, smI_Blob blob) throws smBlobException;
	protected abstract <T extends smI_Blob> smI_Blob getBlobFromCache(String generatedKey, Class<? extends T> blobType) throws smBlobException;
	protected abstract Map<smI_BlobKey, smI_Blob> getBlobsFromCache(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws smBlobException;
	protected abstract void deleteBlobFromCache(String generatedKey) throws smBlobException;
	protected abstract void deleteBlobsFromCache(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws smBlobException;
	protected abstract void putBlobsIntoCache(Map<smI_BlobKey, smI_Blob> values) throws smBlobException;
	
	private void private_putBlobIntoCache(String generatedKey, smI_Blob blob) throws smBlobException
	{
		if( this.isCacheable(blob) )
		{
			this.putBlobIntoCache(generatedKey, blob);
		}
	}
	
	protected boolean isCacheable(smI_Blob blob)
	{
		return blob.getMaximumCacheLevel().ordinal() <= this.getCacheLevel().ordinal();
	}
	
	@Override
	public final void putBlob(smI_BlobKey keySource, smI_Blob blob) throws smBlobException, ConcurrentModificationException
	{
		if( m_wrappedManager != null )
		{
			m_wrappedManager.putBlob(keySource, blob);
		}
		
		this.private_putBlobIntoCache(keySource.createBlobKey(blob), blob);
	}
	
	@Override
	public final <T extends smI_Blob> T getBlob(smI_BlobKey keySource, Class<? extends T> blobType) throws smBlobException
	{
		smI_Blob template = m_templateMngr.getTemplate(blobType);
		
		String generatedKey = keySource.createBlobKey(template);
		smI_Blob cachedBlob = this.getBlobFromCache(generatedKey, blobType);
		
		if( cachedBlob != null )
		{
			return (T) cachedBlob;
		}
		
		if( m_wrappedManager == null )  return null;
		
		smI_Blob blob = m_wrappedManager.getBlob(keySource, blobType);
		
		if( blob != null )
		{
			this.private_putBlobIntoCache(generatedKey, blob);
		}
		
		return (T) blob;
	}
	
	@Override
	public final void deleteBlob(smI_BlobKey keySource, Class<? extends smI_Blob> blobType) throws smBlobException
	{
		smI_Blob blobTemplate = m_templateMngr.getTemplate(blobType);
		
		this.deleteBlobFromCache(keySource.createBlobKey(blobTemplate));
		
		if( m_wrappedManager != null )
		{
			m_wrappedManager.deleteBlob(keySource, blobType);
		}
	}
	
	@Override
	public final void deleteBlobAsync(smI_BlobKey keySource, Class<? extends smI_Blob> blobType) throws smBlobException
	{
		this.deleteBlob(keySource, blobType);
	}
	
	@Override
	public final void deleteBlobs(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws smBlobException
	{
		this.deleteBlobsFromCache(values);
		
		if( m_wrappedManager != null )
		{
			m_wrappedManager.deleteBlobs(values);
		}
	}
	
	@Override
	public final void deleteBlobsAsync(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws smBlobException
	{
		this.deleteBlobs(values);
	}
	
	@Override
	public final void putBlobs(Map<smI_BlobKey, smI_Blob> values) throws smBlobException
	{
		if( this.m_wrappedManager != null)
		{
			m_wrappedManager.putBlobs(values);
		}
		
		this.putBlobsIntoCache(values);
	}
	
	@Override
	public final Map<smI_BlobKey, smI_Blob> getBlobs(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws smBlobException
	{
		Map<smI_BlobKey, smI_Blob> cachedBlobs = this.getBlobsFromCache(values);
		
		if( values.size() == 0 )
		{
			return cachedBlobs;
		}
		else
		{
			if( m_wrappedManager == null )  return cachedBlobs;
			
			if( values.size() == 1 )
			{
				Iterator<smI_BlobKey> iterator = values.keySet().iterator();
				smI_BlobKey soloKeySource = iterator.next();
				Class<? extends smI_Blob> soloBlobType = values.get(soloKeySource);
				
				smI_Blob blobFromWrappedManager = m_wrappedManager.getBlob(soloKeySource, soloBlobType);
				
				Map<smI_BlobKey, smI_Blob> toReturn = null;
				
				if( cachedBlobs == null )
				{
					if( blobFromWrappedManager != null )
					{
						toReturn = new HashMap<smI_BlobKey, smI_Blob>();
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
				Map<smI_BlobKey, smI_Blob> blobsFromWrappedManager = m_wrappedManager.getBlobs(values);
				
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
						Iterator<smI_BlobKey> iterator = cachedBlobs.keySet().iterator();
						while( iterator.hasNext() )
						{
							smI_BlobKey keySourceFromCache = iterator.next();
							smI_Blob blobFromCache = cachedBlobs.get(keySourceFromCache);
							
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
	public Map<smI_BlobKey, smI_Blob> performQuery(smBlobQuery query) throws smBlobException
	{
		//--- DRK > Probably never going to support queries here.
		if( this.m_wrappedManager != null )
		{
			return this.m_wrappedManager.performQuery(query);
		}
		
		return null;
	}
	
	@Override
	public final void putBlobAsync(smI_BlobKey keySource, smI_Blob blob) throws smBlobException
	{
		//--- DRK > Probably never going to support queries here.
		if( this.m_wrappedManager != null )
		{
			this.m_wrappedManager.putBlobAsync(keySource, blob);
		}
		
		this.putBlobIntoCache(keySource.createBlobKey(blob), blob);
	}
	
	@Override
	public final void putBlobsAsync(Map<smI_BlobKey, smI_Blob> values) throws smBlobException
	{
		//--- DRK > Probably never going to support queries here.
		if( this.m_wrappedManager != null )
		{
			this.m_wrappedManager.putBlobsAsync(values);
		}
		
		this.putBlobsIntoCache(values);
	}
}
