package com.b33hive.server.data.blob;

import com.b33hive.server.transaction.bhI_TransactionScopeListener;

public class bhBlobManagerFactory implements bhI_TransactionScopeListener
{
	private static bhBlobManagerFactory s_instance = null;
	
	public static void startUp()
	{
		s_instance = new bhBlobManagerFactory();
		
		bhBlobTransactionManager.startUp();
		bhBlobTemplateManager.startUp();
		bhLocalBlobCache.startUp();
	}
	
	private bhI_BlobManager createInstance(bhE_BlobCacheLevel cacheLevel, bhI_BlobManager wrappedManager)
	{
		switch( cacheLevel )
		{
			case LOCAL:			return new bhBlobManager_LocalCache(wrappedManager);
			case MEMCACHE:  	return new bhBlobManager_MemCache(wrappedManager);
			case PERSISTENT:	return new bhBlobManager_Persistent();
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
	
	/**
	 * Cache level should be in order from most transient cache, to most persistent cache.
	 * 
	 * @param cacheChain
	 * @return
	 */
	public static bhBlobManagerFactory getInstance()
	{
		return s_instance;
	}

	@Override
	public void onEnterScope()
	{
		bhLocalBlobCache.getInstance().deleteContext();
	}

	@Override
	public void onBatchStart()
	{
		bhLocalBlobCache.getInstance().createContext();
	}

	@Override
	public void onBatchEnd()
	{
		bhLocalBlobCache.getInstance().deleteContext();
	}

	@Override
	public void onExitScope()
	{
		bhLocalBlobCache.getInstance().deleteContext();
	}
}
