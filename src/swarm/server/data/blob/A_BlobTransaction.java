package swarm.server.data.blob;

import java.util.ConcurrentModificationException;


import swarm.server.entities.BaseServerGrid;
import swarm.shared.app.S_CommonApp;
import swarm.shared.transaction.E_ResponseError;

public abstract class A_BlobTransaction
{
	protected BlobManagerFactory m_blobMngrFactory;
	
	
	protected abstract void performOperations() throws BlobException;
	
	protected abstract void onSuccess();
	
	protected void performNested(A_BlobTransaction blobTxn) throws BlobException
	{
		blobTxn.m_blobMngrFactory = m_blobMngrFactory;
		blobTxn.performOperations();
	}
	
	public void perform(BlobManagerFactory blobMngrFactory, E_BlobTransactionType transactionType, int maxTryCount) throws BlobException
	{
		BlobTransactionManager blobTransactionManager = blobMngrFactory.getBlobTxnMngr();
		
		m_blobMngrFactory = blobMngrFactory;
		
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
					throw new BlobException("Met try count for transaction.", e);
				}
			}
			finally
			{
				blobTransactionManager.rollbackTransaction();
			}
		}
	}
}
