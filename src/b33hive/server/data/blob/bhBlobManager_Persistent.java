package b33hive.server.data.blob;

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

class bhBlobManager_Persistent extends bhA_BlobManager
{
	private final bhBlobTemplateManager m_templateMngr;
	
	bhBlobManager_Persistent(bhBlobTemplateManager templateMngr)
	{
		m_templateMngr = templateMngr;
	}
	
	private Entity createEntityForPut(bhI_BlobKeySource keySource, bhI_Blob blob) throws bhBlobException
	{
		Key keyObject = KeyFactory.createKey(blob.getKind(), keySource.createBlobKey(blob));
		Entity entity = new Entity(keyObject);
		byte[] blobBytes = bhU_Blob.convertToBytes(blob);
		Blob blobData = new Blob(blobBytes);
		entity.setUnindexedProperty(bhS_Blob.DATA_FIELD_NAME, blobData);
		
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
	public void putBlob(bhI_BlobKeySource keySource, bhI_Blob blob) throws bhBlobException, ConcurrentModificationException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Entity entity = createEntityForPut(keySource, blob);
		
		try
		{
			Transaction currentTransaction_canBeNull = bhBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
			
			datastore.put(currentTransaction_canBeNull, entity);
		}
		catch(ConcurrentModificationException concurrencyException)
		{
			throw concurrencyException;
		}
		catch(Exception e)
		{
			throw new bhBlobException("Some error occured while putting blob.", e);
		}
	}

	@Override
	public void putBlobAsync(bhI_BlobKeySource keySource, bhI_Blob blob) throws bhBlobException
	{
		AsyncDatastoreService datastore = DatastoreServiceFactory.getAsyncDatastoreService();
		
		Transaction currentTransaction = bhBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
		
		if( currentTransaction != null )
		{
			throw new bhBlobException("Blob manager does not support transactions for async puts.");
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
			throw new bhBlobException("Some error occured while putting blob.", e);
		}
	}
	
	@Override
	public <T extends bhI_Blob> T getBlob(bhI_BlobKeySource keySource, Class<? extends T> T) throws bhBlobException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		bhI_Blob blobTemplate = m_templateMngr.getTemplate(T);
		
		Key keyObject = KeyFactory.createKey(blobTemplate.getKind(), keySource.createBlobKey(blobTemplate));
	
		try
		{
			Transaction currentTransaction_canBeNull = bhBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
			
			Entity entity = datastore.get(currentTransaction_canBeNull, keyObject);
			
			//--- DRK > As I understand it, entity should never be null if exception isn't thrown, but just making sure.
			if( entity != null )
			{
				bhI_Blob toReturn = bhU_Blob.createBlobInstance(T);
				
				bhU_Blob.readBytes(toReturn, entity);
				
				return (T) toReturn;
			}
			else
			{
				throw new bhBlobException("When getting blob, entity was null, but no exception was thrown.");
			}
		}
		catch (EntityNotFoundException e)
		{
			return null;
		}
		catch(Exception e)
		{
			throw new bhBlobException("Unknown error occurred while getting blob.", e);
		}
	}
	
	@Override
	public Map<bhI_BlobKeySource, bhI_Blob> getBlobs(Map<bhI_BlobKeySource, Class<? extends bhI_Blob>> values) throws bhBlobException
	{
		ArrayList<Key> keyObjects = new ArrayList<Key>();
		Map<String, bhBlobTuple> generatedKeysToTuples = new HashMap<String, bhBlobTuple>();
		
		Iterator<? extends bhI_BlobKeySource> keySourceIterator = values.keySet().iterator();
		while( keySourceIterator.hasNext() )
		{
			bhI_BlobKeySource keySource = keySourceIterator.next();
			Class<? extends bhI_Blob> nextBlobType = values.get(keySource);
			bhI_Blob blobTemplate = m_templateMngr.getTemplate(nextBlobType);
			
			String generatedKey = keySource.createBlobKey(blobTemplate);
			generatedKeysToTuples.put(generatedKey, new bhBlobTuple(keySource, nextBlobType));
			Key keyObject = KeyFactory.createKey(blobTemplate.getKind(), generatedKey);
			keyObjects.add(keyObject);
		}
	
		try
		{
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Transaction currentTransaction_canBeNull = bhBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
			Map<Key, Entity> entities = datastore.get(currentTransaction_canBeNull, keyObjects);
			
			if( entities != null && entities.size() > 0 )
			{
				HashMap<bhI_BlobKeySource, bhI_Blob> toReturn = new HashMap<bhI_BlobKeySource, bhI_Blob>();
				
				for( Key keyObject : entities.keySet() )
				{
					Entity entity = entities.get(keyObject);
					
					String generatedKey = keyObject.getName();
					bhBlobTuple tuple = generatedKeysToTuples.get(generatedKey);
			
					bhI_Blob blob = bhU_Blob.createBlobInstance(tuple.m_blobType);
					bhU_Blob.readBytes(blob, entity);
					
					toReturn.put(tuple.m_keySource, blob);
				}
				
				return toReturn;
			}
		}
		catch(Exception e)
		{
			throw new bhBlobException("Error occurred while getting blob batch.", e);
		}
		
		return null;
	}
	
	private List<Entity> createEntitiesForPut(Map<bhI_BlobKeySource, bhI_Blob> values) throws bhBlobException
	{
		ArrayList<Entity> entities = new ArrayList<Entity>();
		
		Iterator<? extends bhI_BlobKeySource> keySourceIterator = values.keySet().iterator();
		while( keySourceIterator.hasNext() )
		{
			bhI_BlobKeySource keySource = keySourceIterator.next();
			bhI_Blob nextBlob = values.get(keySource);
			
			Entity entity = createEntityForPut(keySource, nextBlob);
			entities.add(entity);
		}
		
		return entities;
	}
	
	@Override
	public void putBlobs(Map<bhI_BlobKeySource, bhI_Blob> values) throws bhBlobException
	{
		List<Entity> entities = createEntitiesForPut(values);
		
		try
		{
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Transaction currentTransaction_canBeNull = bhBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
			datastore.put(currentTransaction_canBeNull, entities);
		}
		catch(Exception e)
		{
			throw new bhBlobException("Error occurred while putting blob batch.", e);
		}
	}
	
	@Override
	public void putBlobsAsync(Map<bhI_BlobKeySource, bhI_Blob> values) throws bhBlobException
	{
		List<Entity> entities = createEntitiesForPut(values);
		
		AsyncDatastoreService datastore = DatastoreServiceFactory.getAsyncDatastoreService();
		Transaction currentTransaction = bhBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
		
		if( currentTransaction != null )
		{
			throw new bhBlobException("Can't do async multi-put within a blob transaction.");
		}
		
		try
		{
			datastore.put(entities);
		}
		catch(Exception e)
		{
			throw new bhBlobException("Error occurred while putting blob batch.", e);
		}
	}

	@Override
	public void deleteBlob(bhI_BlobKeySource keySource, Class<? extends bhI_Blob> blobType) throws bhBlobException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		bhI_Blob blobTemplate = m_templateMngr.getTemplate(blobType);
		
		Key keyObject = KeyFactory.createKey(blobTemplate.getKind(), keySource.createBlobKey(blobTemplate));
	
		try
		{
			Transaction currentTransaction_canBeNull = bhBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
			
			datastore.delete(currentTransaction_canBeNull, keyObject);
		}
		catch(Exception e)
		{
			throw new bhBlobException("Unknown error occurred while deleting blob (sync).", e);
		}
	}
	
	@Override
	public void deleteBlobAsync(bhI_BlobKeySource keySource, Class<? extends bhI_Blob> blobType) throws bhBlobException
	{
		AsyncDatastoreService datastore = DatastoreServiceFactory.getAsyncDatastoreService();
		
		Transaction currentTransaction = bhBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
		
		if( currentTransaction != null )
		{
			throw new bhBlobException("Can't delete within transaction.");
		}
		
		bhI_Blob blobTemplate = m_templateMngr.getTemplate(blobType);
		
		Key keyObject = KeyFactory.createKey(blobTemplate.getKind(), keySource.createBlobKey(blobTemplate));
	
		try
		{
			datastore.delete(keyObject);
		}
		catch(Exception e)
		{
			throw new bhBlobException("Unknown error occurred while deleting blob (async).", e);
		}
	}
	
	private List<Key> createKeysForBatchDelete(Map<bhI_BlobKeySource, Class<? extends bhI_Blob>> values) throws bhBlobException
	{
		ArrayList<Key> keys = new ArrayList<Key>();
		
		Iterator<bhI_BlobKeySource> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			bhI_BlobKeySource keySource = iterator.next();
			bhI_Blob blob = m_templateMngr.getTemplate(values.get(keySource));
			
			Key keyObject = KeyFactory.createKey(blob.getKind(), keySource.createBlobKey(blob));
			
			keys.add(keyObject);
		}
		
		return keys;
	}
	
	@Override
	public void deleteBlobs(Map<bhI_BlobKeySource, Class<? extends bhI_Blob>> values) throws bhBlobException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		List<Key> keys = createKeysForBatchDelete(values);
		
		try
		{
			Transaction currentTransaction_canBeNull = bhBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
			
			datastore.delete(currentTransaction_canBeNull, keys);
		}
		catch(Exception e)
		{
			throw new bhBlobException("Unknown error occurred while deleting blobs (sync).", e);
		}
	}
	
	@Override
	public void deleteBlobsAsync(Map<bhI_BlobKeySource, Class<? extends bhI_Blob>> values) throws bhBlobException
	{
		AsyncDatastoreService datastore = DatastoreServiceFactory.getAsyncDatastoreService();
		
		Transaction currentTransaction = bhBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
		
		if( currentTransaction != null )
		{
			throw new bhBlobException("Can't delete within transaction.");
		}
		
		List<Key> keys = createKeysForBatchDelete(values);
		
		try
		{
			datastore.delete(keys);
		}
		catch(Exception e)
		{
			throw new bhBlobException("Unknown error occurred while deleting blobs (async).", e);
		}
	}

	@Override
	public Map<bhI_BlobKeySource, bhI_Blob> performQuery(bhBlobQuery query) throws bhBlobException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		bhI_Blob blobTemplate = m_templateMngr.getTemplate(query.getBlobType());
		
		/*Query query = new Query(blobTemplate.getKind());
		FilterOperator filter = new FilterOperator();
		query.addFilter(propertyName, operator, value)*/
		
		return null;
	}
}
