package com.b33hive.server.data.blob;

import java.util.ConcurrentModificationException;

import com.b33hive.server.entities.bhS_BlobKeyPrefix;
import com.b33hive.server.entities.bhServerGrid;
import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.transaction.bhE_ResponseError;

public abstract class bhA_BlobTransaction
{
	protected abstract void performOperations() throws bhBlobException;
	
	protected abstract void onSuccess();
	
	public void perform(bhE_BlobTransactionType transactionType, int maxTryCount) throws bhBlobException
	{
		bhBlobTransactionManager blobTransactionManager = bhBlobTransactionManager.getInstance();
		
		int tryCount = 0;
		while( tryCount < maxTryCount )
		{
			try
			{
				blobTransactionManager.beginTransaction(transactionType);

				this.performOperations();
				
				blobTransactionManager.endTransaction();
				
				this.onSuccess();
				
				break;
			}
			catch(ConcurrentModificationException e)
			{ 
				tryCount++;
				
				if( tryCount >= maxTryCount )
				{
					throw new bhBlobException("Met try count for transaction.", e);
				}
			}
			finally
			{
				blobTransactionManager.rollbackTransaction();
			}
		}
	}
}
