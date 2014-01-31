package swarm.server.data.blob;

import java.util.ConcurrentModificationException;

import com.google.appengine.api.datastore.BaseDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

public class BlobTransactionManager
{
	BlobTransactionManager()
	{
		
	}
	
	void commit(Transaction transaction)
	{
		if( transaction != null )
    	{
    		if( transaction.isActive() )
    		{
    			transaction.commit();
    		}
    	}
	}
	
	void rollback(Transaction transaction)
	{
		if( transaction != null )
    	{
    		if( transaction.isActive() )
    		{
    			try
    			{
    				transaction.rollback();
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    	}
	}
	
	Transaction getCurrentTransaction(BaseDatastoreService datastore)
	{
		Transaction transaction = datastore.getCurrentTransaction(null);
		
		return transaction;
	}
	
	void beginTransaction(E_BlobTransactionType type) throws BlobException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		if( this.getCurrentTransaction(datastore) != null )
		{
			throw new BlobException("Transaction already started.");
		}
		
	    TransactionOptions options = null;
	    
	    if( type == E_BlobTransactionType.MULTI_BLOB_TYPE )
	    {
	    	options = TransactionOptions.Builder.withXG(true);
	   	}
	    
	    Transaction transaction = null;
	    try
	    {
	    	transaction = options != null ? datastore.beginTransaction(options) : datastore.beginTransaction();
	    }
	    catch(Exception e)
	    {
	    	this.rollback(transaction);
	    	
	    	throw new BlobException("Could not begin transaction due to some error.", e);
	    }
	}
	
	void rollbackTransaction()
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Transaction transaction = this.getCurrentTransaction(datastore);
		
		if( transaction != null )
		{
			this.rollback(transaction);
		}
	}
	
	void endTransaction() throws BlobException, ConcurrentModificationException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Transaction transaction = this.getCurrentTransaction(datastore);
		
		if( transaction == null )
		{
			throw new BlobException("No blob transaction existed for endTransaction.");
		}
		
		try
		{
			this.commit(transaction);
		}
		catch(ConcurrentModificationException concurrencyException)
		{
			throw concurrencyException;
		}
		catch(Exception e)
		{
			throw new BlobException("Could not commit blob transaction.", e);
		}
		finally
		{
			this.rollback(transaction);
		}
	}
}

