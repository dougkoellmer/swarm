package swarm.server.data.blob;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

abstract class A_BlobManagerWithCache extends A_BlobManager
{
	private final I_BlobManager m_wrappedManager;
	protected final BlobTemplateManager m_templateMngr;
	
	A_BlobManagerWithCache(BlobTemplateManager templateMngr, I_BlobManager wrappedManager)
	{
		super();
		
		m_templateMngr = templateMngr;
		m_wrappedManager = wrappedManager;
	}
	
	protected abstract E_BlobCacheLevel getCacheLevel();
	
	//--- DRK > The following abstract methods somewhat mirror the interface methods, and are
	//---		overridden instead of the interface methods, which are finalized here.
	protected abstract void putBlobIntoCache(String generatedKey, I_Blob blob) throws BlobException;
	protected abstract <T extends I_Blob> I_Blob getBlobFromCache(String generatedKey, Class<? extends T> blobType) throws BlobException;
	protected abstract Map<I_BlobKey, I_Blob> getBlobsFromCache(Map<I_BlobKey, Class<? extends I_Blob>> values) throws BlobException;
	protected abstract void deleteBlobFromCache(String generatedKey) throws BlobException;
	protected abstract void deleteBlobsFromCache(Map<I_BlobKey, Class<? extends I_Blob>> values) throws BlobException;
	protected abstract void putBlobsIntoCache(Map<I_BlobKey, I_Blob> values) throws BlobException;
	
	private void private_putBlobIntoCache(String generatedKey, I_Blob blob) throws BlobException
	{
		if( this.isCacheable(blob) )
		{
			this.putBlobIntoCache(generatedKey, blob);
		}
	}
	
	protected boolean isCacheable(I_Blob blob)
	{
		return blob.getMaximumCacheLevel().ordinal() <= this.getCacheLevel().ordinal();
	}
	
	@Override
	public final void putBlob(I_BlobKey keySource, I_Blob blob) throws BlobException, ConcurrentModificationException
	{
		if( m_wrappedManager != null )
		{
			m_wrappedManager.putBlob(keySource, blob);
		}
		
		this.private_putBlobIntoCache(keySource.createBlobKey(blob), blob);
	}
	
	@Override
	public final <T extends I_Blob> T getBlob(I_BlobKey keySource, Class<? extends T> blobType) throws BlobException
	{
		I_Blob template = m_templateMngr.getTemplate(blobType);
		
		String generatedKey = keySource.createBlobKey(template);
		I_Blob cachedBlob = this.getBlobFromCache(generatedKey, blobType);
		
		if( cachedBlob != null )
		{
			return (T) cachedBlob;
		}
		
		if( m_wrappedManager == null )  return null;
		
		I_Blob blob = m_wrappedManager.getBlob(keySource, blobType);
		
		if( blob != null )
		{
			this.private_putBlobIntoCache(generatedKey, blob);
		}
		
		return (T) blob;
	}
	
	@Override
	public final void deleteBlob(I_BlobKey keySource, Class<? extends I_Blob> blobType) throws BlobException
	{
		I_Blob blobTemplate = m_templateMngr.getTemplate(blobType);
		
		this.deleteBlobFromCache(keySource.createBlobKey(blobTemplate));
		
		if( m_wrappedManager != null )
		{
			m_wrappedManager.deleteBlob(keySource, blobType);
		}
	}
	
	@Override
	public final void deleteBlobAsync(I_BlobKey keySource, Class<? extends I_Blob> blobType) throws BlobException
	{
		this.deleteBlob(keySource, blobType);
	}
	
	@Override
	public final void deleteBlobs(Map<I_BlobKey, Class<? extends I_Blob>> values) throws BlobException
	{
		this.deleteBlobsFromCache(values);
		
		if( m_wrappedManager != null )
		{
			m_wrappedManager.deleteBlobs(values);
		}
	}
	
	@Override
	public final void deleteBlobsAsync(Map<I_BlobKey, Class<? extends I_Blob>> values) throws BlobException
	{
		this.deleteBlobs(values);
	}
	
	@Override
	public final void putBlobs(Map<I_BlobKey, I_Blob> values) throws BlobException
	{
		if( this.m_wrappedManager != null)
		{
			m_wrappedManager.putBlobs(values);
		}
		
		this.putBlobsIntoCache(values);
	}
	
	@Override
	public final Map<I_BlobKey, I_Blob> getBlobs(Map<I_BlobKey, Class<? extends I_Blob>> values) throws BlobException
	{
		Map<I_BlobKey, I_Blob> cachedBlobs = this.getBlobsFromCache(values);
		
		if( values.size() == 0 )
		{
			return cachedBlobs;
		}
		else
		{
			if( m_wrappedManager == null )  return cachedBlobs;
			
			if( values.size() == 1 )
			{
				Iterator<I_BlobKey> iterator = values.keySet().iterator();
				I_BlobKey soloKeySource = iterator.next();
				Class<? extends I_Blob> soloBlobType = values.get(soloKeySource);
				
				I_Blob blobFromWrappedManager = m_wrappedManager.getBlob(soloKeySource, soloBlobType);
				
				Map<I_BlobKey, I_Blob> toReturn = null;
				
				if( cachedBlobs == null )
				{
					if( blobFromWrappedManager != null )
					{
						toReturn = new HashMap<I_BlobKey, I_Blob>();
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
				Map<I_BlobKey, I_Blob> blobsFromWrappedManager = m_wrappedManager.getBlobs(values);
				
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
						Iterator<I_BlobKey> iterator = cachedBlobs.keySet().iterator();
						while( iterator.hasNext() )
						{
							I_BlobKey keySourceFromCache = iterator.next();
							I_Blob blobFromCache = cachedBlobs.get(keySourceFromCache);
							
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
	public Map<I_BlobKey, I_Blob> performQuery(BlobQuery query) throws BlobException
	{
		//--- DRK > Probably never going to support queries here.
		if( this.m_wrappedManager != null )
		{
			return this.m_wrappedManager.performQuery(query);
		}
		
		return null;
	}
	
	@Override
	public final void putBlobAsync(I_BlobKey keySource, I_Blob blob) throws BlobException
	{
		//--- DRK > Probably never going to support queries here.
		if( this.m_wrappedManager != null )
		{
			this.m_wrappedManager.putBlobAsync(keySource, blob);
		}
		
		this.putBlobIntoCache(keySource.createBlobKey(blob), blob);
	}
	
	@Override
	public final void putBlobsAsync(Map<I_BlobKey, I_Blob> values) throws BlobException
	{
		//--- DRK > Probably never going to support queries here.
		if( this.m_wrappedManager != null )
		{
			this.m_wrappedManager.putBlobsAsync(values);
		}
		
		this.putBlobsIntoCache(values);
	}
}
