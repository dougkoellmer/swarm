package swarm.server.data.blob;

import swarm.server.transaction.smI_TransactionScopeListener;

public class smBlobManagerFactory implements smI_TransactionScopeListener
{
	private final smLocalBlobCache m_localCache = new smLocalBlobCache();
	private final smBlobTransactionManager m_blobTxnMngr = new smBlobTransactionManager();
	private final smBlobTemplateManager m_templateMngr = new smBlobTemplateManager();
	
	public smBlobManagerFactory()
	{
	}
	
	public smBlobTransactionManager getBlobTxnMngr()
	{
		return m_blobTxnMngr;
	}
	
	private smI_BlobManager createInstance(smE_BlobCacheLevel cacheLevel, smI_BlobManager wrappedManager)
	{
		switch( cacheLevel )
		{
			case LOCAL:			return new smBlobManager_LocalCache(m_templateMngr, m_localCache, wrappedManager);
			case MEMCACHE:  	return new smBlobManager_MemCache(m_templateMngr, wrappedManager);
			case PERSISTENT:	return new smBlobManager_Persistent(m_blobTxnMngr, m_templateMngr);
		}
		
		return null;
	}
	
	private smI_BlobManager private_getInstance(smE_BlobCacheLevel ... cacheChain)
	{
		smI_BlobManager toReturn = null;
		int lastOrdinal = Integer.MAX_VALUE;
		for( int i = cacheChain.length-1; i >= 0; i-- )
		{
			smE_BlobCacheLevel cacheLevel = cacheChain[i];
			int currentOrdinal = cacheLevel.ordinal();
			
			if( currentOrdinal > lastOrdinal )
			{
				throw new RuntimeException("Tried to get a blob manager with a higher cache level than a previous manager.");
			}
			
			lastOrdinal = currentOrdinal;
			
			smI_BlobManager ithBlobManager = createInstance(cacheLevel, toReturn);
			toReturn = ithBlobManager;
		}
		
		return toReturn;
	}
	
	public smI_BlobManager create(smE_BlobCacheLevel ... cacheChain)
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
