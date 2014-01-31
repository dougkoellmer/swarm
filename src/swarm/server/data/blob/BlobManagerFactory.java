package swarm.server.data.blob;

import swarm.server.transaction.I_TransactionScopeListener;

public class BlobManagerFactory implements I_TransactionScopeListener
{
	private final LocalBlobCache m_localCache = new LocalBlobCache();
	private final BlobTransactionManager m_blobTxnMngr = new BlobTransactionManager();
	private final BlobTemplateManager m_templateMngr = new BlobTemplateManager();
	
	public BlobManagerFactory()
	{
	}
	
	public BlobTransactionManager getBlobTxnMngr()
	{
		return m_blobTxnMngr;
	}
	
	private I_BlobManager createInstance(E_BlobCacheLevel cacheLevel, I_BlobManager wrappedManager)
	{
		switch( cacheLevel )
		{
			case LOCAL:			return new BlobManager_LocalCache(m_templateMngr, m_localCache, wrappedManager);
			case MEMCACHE:  	return new BlobManager_MemCache(m_templateMngr, wrappedManager);
			case PERSISTENT:	return new BlobManager_Persistent(m_blobTxnMngr, m_templateMngr);
		}
		
		return null;
	}
	
	private I_BlobManager private_getInstance(E_BlobCacheLevel ... cacheChain)
	{
		I_BlobManager toReturn = null;
		int lastOrdinal = Integer.MAX_VALUE;
		for( int i = cacheChain.length-1; i >= 0; i-- )
		{
			E_BlobCacheLevel cacheLevel = cacheChain[i];
			int currentOrdinal = cacheLevel.ordinal();
			
			if( currentOrdinal > lastOrdinal )
			{
				throw new RuntimeException("Tried to get a blob manager with a higher cache level than a previous manager.");
			}
			
			lastOrdinal = currentOrdinal;
			
			I_BlobManager ithBlobManager = createInstance(cacheLevel, toReturn);
			toReturn = ithBlobManager;
		}
		
		return toReturn;
	}
	
	public I_BlobManager create(E_BlobCacheLevel ... cacheChain)
	{
		return private_getInstance(cacheChain);
	}
	
	@Override
	public void onEnterScope()
	{
		m_localCache.deleteContext();
	}

	@Override
	public void onBatchStart()
	{
		m_localCache.createContext();
	}

	@Override
	public void onBatchEnd()
	{
		m_localCache.deleteContext();
	}

	@Override
	public void onExitScope()
	{
		m_localCache.deleteContext();
	}
}
