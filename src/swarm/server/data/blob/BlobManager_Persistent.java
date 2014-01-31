package swarm.server.data.blob;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Transaction;

class BlobManager_Persistent extends A_BlobManager
{
	private final BlobTemplateManager m_templateMngr;
	private final BlobTransactionManager m_blobTxnMngr;
	
	BlobManager_Persistent(BlobTransactionManager blobTxnMngr, BlobTemplateManager templateMngr)
	{
		m_blobTxnMngr = blobTxnMngr;
		m_templateMngr = templateMngr;
	}
	
	private Entity createEntityForPut(I_BlobKey keySource, I_Blob blob) throws BlobException
	{
		Key keyObject = KeyFactory.createKey(blob.getKind(), keySource.createBlobKey(blob));
		Entity entity = new Entity(keyObject);
		byte[] blobBytes = U_Blob.convertToBytes(blob);
		Blob blobData = new Blob(blobBytes);
		entity.setUnindexedProperty(S_Blob.DATA_FIELD_NAME, blobData);
		
		Map<String, Object> queryableProperties = blob.getQueryableProperties();
		
		if( queryableProperties != null )
		{
			for( String key : queryableProperties.keySet() )
			{
				entity.setProperty(key, queryableProperties.get(key));
			}
		}
		
		return entity;
	}
	
	@Override
	public void putBlob(I_BlobKey keySource, I_Blob blob) throws BlobException, ConcurrentModificationException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Entity entity = createEntityForPut(keySource, blob);
		
		try
		{
			Transaction currentTransaction_canBeNull = m_blobTxnMngr.getCurrentTransaction(datastore);
			
			datastore.put(currentTransaction_canBeNull, entity);
		}
		catch(ConcurrentModificationException concurrencyException)
		{
			throw concurrencyException;
		}
		catch(Exception e)
		{
			throw new BlobException("Some error occured while putting blob.", e);
		}
	}

	@Override
	public void putBlobAsync(I_BlobKey keySource, I_Blob blob) throws BlobException
	{
		AsyncDatastoreService datastore = DatastoreServiceFactory.getAsyncDatastoreService();
		
		Transaction currentTransaction = m_blobTxnMngr.getCurrentTransaction(datastore);
		
		if( currentTransaction != null )
		{
			throw new BlobException("Blob manager does not support transactions for async puts.");
		}
		
		Entity entity = createEntityForPut(keySource, blob);
		
		try
		{
			datastore.put(entity);
		}
		catch(ConcurrentModificationException concurrencyException)
		{
			throw concurrencyException;
		}
		catch(Exception e)
		{
			throw new BlobException("Some error occured while putting blob.", e);
		}
	}
	
	@Override
	public <T extends I_Blob> T getBlob(I_BlobKey keySource, Class<? extends T> T) throws BlobException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		I_Blob blobTemplate = m_templateMngr.getTemplate(T);
		
		Key keyObject = KeyFactory.createKey(blobTemplate.getKind(), keySource.createBlobKey(blobTemplate));
	
		try
		{
			Transaction currentTransaction_canBeNull = m_blobTxnMngr.getCurrentTransaction(datastore);
			
			Entity entity = datastore.get(currentTransaction_canBeNull, keyObject);
			
			//--- DRK > As I understand it, entity should never be null if exception isn't thrown, but just making sure.
			if( entity != null )
			{
				I_Blob toReturn = U_Blob.createBlobInstance(T);
				
				U_Blob.readBytes(toReturn, entity);
				
				return (T) toReturn;
			}
			else
			{
				throw new BlobException("When getting blob, entity was null, but no exception was thrown.");
			}
		}
		catch (EntityNotFoundException e)
		{
			return null;
		}
		catch(Exception e)
		{
			throw new BlobException("Unknown error occurred while getting blob.", e);
		}
	}
	
	@Override
	public Map<I_BlobKey, I_Blob> getBlobs(Map<I_BlobKey, Class<? extends I_Blob>> values) throws BlobException
	{
		ArrayList<Key> keyObjects = new ArrayList<Key>();
		Map<String, BlobTuple> generatedKeysToTuples = new HashMap<String, BlobTuple>();
		
		Iterator<? extends I_BlobKey> keySourceIterator = values.keySet().iterator();
		while( keySourceIterator.hasNext() )
		{
			I_BlobKey keySource = keySourceIterator.next();
			Class<? extends I_Blob> nextBlobType = values.get(keySource);
			I_Blob blobTemplate = m_templateMngr.getTemplate(nextBlobType);
			
			String generatedKey = keySource.createBlobKey(blobTemplate);
			generatedKeysToTuples.put(generatedKey, new BlobTuple(keySource, nextBlobType));
			Key keyObject = KeyFactory.createKey(blobTemplate.getKind(), generatedKey);
			keyObjects.add(keyObject);
		}
	
		try
		{
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Transaction currentTransaction_canBeNull = m_blobTxnMngr.getCurrentTransaction(datastore);
			Map<Key, Entity> entities = datastore.get(currentTransaction_canBeNull, keyObjects);
			
			if( entities != null && entities.size() > 0 )
			{
				HashMap<I_BlobKey, I_Blob> toReturn = new HashMap<I_BlobKey, I_Blob>();
				
				for( Key keyObject : entities.keySet() )
				{
					Entity entity = entities.get(keyObject);
					
					String generatedKey = keyObject.getName();
					BlobTuple tuple = generatedKeysToTuples.get(generatedKey);
			
					I_Blob blob = U_Blob.createBlobInstance(tuple.m_blobType);
					U_Blob.readBytes(blob, entity);
					
					toReturn.put(tuple.m_keySource, blob);
				}
				
				return toReturn;
			}
		}
		catch(Exception e)
		{
			throw new BlobException("Error occurred while getting blob batch.", e);
		}
		
		return null;
	}
	
	private List<Entity> createEntitiesForPut(Map<I_BlobKey, I_Blob> values) throws BlobException
	{
		ArrayList<Entity> entities = new ArrayList<Entity>();
		
		Iterator<? extends I_BlobKey> keySourceIterator = values.keySet().iterator();
		while( keySourceIterator.hasNext() )
		{
			I_BlobKey keySource = keySourceIterator.next();
			I_Blob nextBlob = values.get(keySource);
			
			Entity entity = createEntityForPut(keySource, nextBlob);
			entities.add(entity);
		}
		
		return entities;
	}
	
	@Override
	public void putBlobs(Map<I_BlobKey, I_Blob> values) throws BlobException
	{
		List<Entity> entities = createEntitiesForPut(values);
		
		try
		{
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Transaction currentTransaction_canBeNull = m_blobTxnMngr.getCurrentTransaction(datastore);
			datastore.put(currentTransaction_canBeNull, entities);
		}
		catch(Exception e)
		{
			throw new BlobException("Error occurred while putting blob batch.", e);
		}
	}
	
	@Override
	public void putBlobsAsync(Map<I_BlobKey, I_Blob> values) throws BlobException
	{
		List<Entity> entities = createEntitiesForPut(values);
		
		AsyncDatastoreService datastore = DatastoreServiceFactory.getAsyncDatastoreService();
		Transaction currentTransaction = m_blobTxnMngr.getCurrentTransaction(datastore);
		
		if( currentTransaction != null )
		{
			throw new BlobException("Can't do async multi-put within a blob transaction.");
		}
		
		try
		{
			datastore.put(entities);
		}
		catch(Exception e)
		{
			throw new BlobException("Error occurred while putting blob batch.", e);
		}
	}

	@Override
	public void deleteBlob(I_BlobKey keySource, Class<? extends I_Blob> blobType) throws BlobException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		I_Blob blobTemplate = m_templateMngr.getTemplate(blobType);
		
		Key keyObject = KeyFactory.createKey(blobTemplate.getKind(), keySource.createBlobKey(blobTemplate));
	
		try
		{
			Transaction currentTransaction_canBeNull = m_blobTxnMngr.getCurrentTransaction(datastore);
			
			datastore.delete(currentTransaction_canBeNull, keyObject);
		}
		catch(Exception e)
		{
			throw new BlobException("Unknown error occurred while deleting blob (sync).", e);
		}
	}
	
	@Override
	public void deleteBlobAsync(I_BlobKey keySource, Class<? extends I_Blob> blobType) throws BlobException
	{
		AsyncDatastoreService datastore = DatastoreServiceFactory.getAsyncDatastoreService();
		
		Transaction currentTransaction = m_blobTxnMngr.getCurrentTransaction(datastore);
		
		if( currentTransaction != null )
		{
			throw new BlobException("Can't delete within transaction.");
		}
		
		I_Blob blobTemplate = m_templateMngr.getTemplate(blobType);
		
		Key keyObject = KeyFactory.createKey(blobTemplate.getKind(), keySource.createBlobKey(blobTemplate));
	
		try
		{
			datastore.delete(keyObject);
		}
		catch(Exception e)
		{
			throw new BlobException("Unknown error occurred while deleting blob (async).", e);
		}
	}
	
	private List<Key> createKeysForBatchDelete(Map<I_BlobKey, Class<? extends I_Blob>> values) throws BlobException
	{
		ArrayList<Key> keys = new ArrayList<Key>();
		
		Iterator<I_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			I_BlobKey keySource = iterator.next();
			I_Blob blob = m_templateMngr.getTemplate(values.get(keySource));
			
			Key keyObject = KeyFactory.createKey(blob.getKind(), keySource.createBlobKey(blob));
			
			keys.add(keyObject);
		}
		
		return keys;
	}
	
	@Override
	public void deleteBlobs(Map<I_BlobKey, Class<? extends I_Blob>> values) throws BlobException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		List<Key> keys = createKeysForBatchDelete(values);
		
		try
		{
			Transaction currentTransaction_canBeNull = m_blobTxnMngr.getCurrentTransaction(datastore);
			
			datastore.delete(currentTransaction_canBeNull, keys);
		}
		catch(Exception e)
		{
			throw new BlobException("Unknown error occurred while deleting blobs (sync).", e);
		}
	}
	
	@Override
	public void deleteBlobsAsync(Map<I_BlobKey, Class<? extends I_Blob>> values) throws BlobException
	{
		AsyncDatastoreService datastore = DatastoreServiceFactory.getAsyncDatastoreService();
		
		Transaction currentTransaction = m_blobTxnMngr.getCurrentTransaction(datastore);
		
		if( currentTransaction != null )
		{
			throw new BlobException("Can't delete within transaction.");
		}
		
		List<Key> keys = createKeysForBatchDelete(values);
		
		try
		{
			datastore.delete(keys);
		}
		catch(Exception e)
		{
			throw new BlobException("Unknown error occurred while deleting blobs (async).", e);
		}
	}

	@Override
	public Map<I_BlobKey, I_Blob> performQuery(BlobQuery query) throws BlobException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		I_Blob blobTemplate = m_templateMngr.getTemplate(query.getBlobType());
		
		/*Query query = new Query(blobTemplate.getKind());
		FilterOperator filter = new FilterOperator();
		query.addFilter(propertyName, operator, value)*/
		
		return null;
	}
}
