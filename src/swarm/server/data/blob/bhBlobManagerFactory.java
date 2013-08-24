package swarm.server.data.blob;

import swarm.server.transaction.bhI_TransactionScopeListener;

public class bhBlobManagerFactory implements bhI_TransactionScopeListener
{
	private final bhLocalBlobCache m_localCache = new bhLocalBlobCache();
	private final bhBlobTransactionManager m_txnMngr = new bhBlobTransactionManager();
	private final bhBlobTemplateManager m_templateMngr = new bhBlobTemplateManager();
	
	public bhBlobManagerFactory()
	{
		bhBlobTransactionManager.startUp(); // TODO(DRK): Shouldn't be singleton.
	}
	
	private bhI_BlobManager createInstance(bhE_BlobCacheLevel cacheLevel, bhI_BlobManager wrappedManager)
	{
		switch( cacheLevel )
		{
			case LOCAL:			return new bhBlobManager_LocalCache(m_templateMngr, m_localCache, wrappedManager);
			case MEMCACHE:  	return new bhBlobManager_MemCache(m_templateMngr, wrappedManager);
			case PERSISTENT:	return new bhBlobManager_Persistent(m_templateMngr);
		}
		
		return null;
	}
	
	private bhI_BlobManager private_getInstance(bhE_BlobCacheLevel ... cacheChain)
	{
		bhI_BlobManager toReturn = null;
		int lastOrdinal = Integer.MAX_VALUE;
		for( int i = cacheChain.length-1; i >= 0; i-- )
		{
			bhE_BlobCacheLevel cacheLevel = cacheChain[i];
			int currentOrdinal = cacheLevel.ordinal();
			
			if( currentOrdinal > lastOrdinal )
			{
				throw new RuntimeException("Tried to get a blob manager with a higher cache level than a previous manager.");
			}
			
			lastOrdinal = currentOrdinal;
			
			bhI_BlobManager ithBlobManager = createInstance(cacheLevel, toReturn);
			toReturn = ithBlobManager;
		}
		
		return toReturn;
	}
	
	public bhI_BlobManager create(bhE_BlobCacheLevel ... cacheChain)
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
