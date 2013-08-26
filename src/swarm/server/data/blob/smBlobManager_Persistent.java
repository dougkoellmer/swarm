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

class smBlobManager_Persistent extends smA_BlobManager
{
	private final smBlobTemplateManager m_templateMngr;
	
	smBlobManager_Persistent(smBlobTemplateManager templateMngr)
	{
		m_templateMngr = templateMngr;
	}
	
	private Entity createEntityForPut(smI_BlobKey keySource, smI_Blob blob) throws smBlobException
	{
		Key keyObject = KeyFactory.createKey(blob.getKind(), keySource.createBlobKey(blob));
		Entity entity = new Entity(keyObject);
		byte[] blobBytes = smU_Blob.convertToBytes(blob);
		Blob blobData = new Blob(blobBytes);
		entity.setUnindexedProperty(smS_Blob.DATA_FIELD_NAME, blobData);
		
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
	public void putBlob(smI_BlobKey keySource, smI_Blob blob) throws smBlobException, ConcurrentModificationException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Entity entity = createEntityForPut(keySource, blob);
		
		try
		{
			Transaction currentTransaction_canBeNull = smBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
			
			datastore.put(currentTransaction_canBeNull, entity);
		}
		catch(ConcurrentModificationException concurrencyException)
		{
			throw concurrencyException;
		}
		catch(Exception e)
		{
			throw new smBlobException("Some error occured while putting blob.", e);
		}
	}

	@Override
	public void putBlobAsync(smI_BlobKey keySource, smI_Blob blob) throws smBlobException
	{
		AsyncDatastoreService datastore = DatastoreServiceFactory.getAsyncDatastoreService();
		
		Transaction currentTransaction = smBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
		
		if( currentTransaction != null )
		{
			throw new smBlobException("Blob manager does not support transactions for async puts.");
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
			throw new smBlobException("Some error occured while putting blob.", e);
		}
	}
	
	@Override
	public <T extends smI_Blob> T getBlob(smI_BlobKey keySource, Class<? extends T> T) throws smBlobException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		smI_Blob blobTemplate = m_templateMngr.getTemplate(T);
		
		Key keyObject = KeyFactory.createKey(blobTemplate.getKind(), keySource.createBlobKey(blobTemplate));
	
		try
		{
			Transaction currentTransaction_canBeNull = smBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
			
			Entity entity = datastore.get(currentTransaction_canBeNull, keyObject);
			
			//--- DRK > As I understand it, entity should never be null if exception isn't thrown, but just making sure.
			if( entity != null )
			{
				smI_Blob toReturn = smU_Blob.createBlobInstance(T);
				
				smU_Blob.readBytes(toReturn, entity);
				
				return (T) toReturn;
			}
			else
			{
				throw new smBlobException("When getting blob, entity was null, but no exception was thrown.");
			}
		}
		catch (EntityNotFoundException e)
		{
			return null;
		}
		catch(Exception e)
		{
			throw new smBlobException("Unknown error occurred while getting blob.", e);
		}
	}
	
	@Override
	public Map<smI_BlobKey, smI_Blob> getBlobs(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws smBlobException
	{
		ArrayList<Key> keyObjects = new ArrayList<Key>();
		Map<String, smBlobTuple> generatedKeysToTuples = new HashMap<String, smBlobTuple>();
		
		Iterator<? extends smI_BlobKey> keySourceIterator = values.keySet().iterator();
		while( keySourceIterator.hasNext() )
		{
			smI_BlobKey keySource = keySourceIterator.next();
			Class<? extends smI_Blob> nextBlobType = values.get(keySource);
			smI_Blob blobTemplate = m_templateMngr.getTemplate(nextBlobType);
			
			String generatedKey = keySource.createBlobKey(blobTemplate);
			generatedKeysToTuples.put(generatedKey, new smBlobTuple(keySource, nextBlobType));
			Key keyObject = KeyFactory.createKey(blobTemplate.getKind(), generatedKey);
			keyObjects.add(keyObject);
		}
	
		try
		{
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Transaction currentTransaction_canBeNull = smBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
			Map<Key, Entity> entities = datastore.get(currentTransaction_canBeNull, keyObjects);
			
			if( entities != null && entities.size() > 0 )
			{
				HashMap<smI_BlobKey, smI_Blob> toReturn = new HashMap<smI_BlobKey, smI_Blob>();
				
				for( Key keyObject : entities.keySet() )
				{
					Entity entity = entities.get(keyObject);
					
					String generatedKey = keyObject.getName();
					smBlobTuple tuple = generatedKeysToTuples.get(generatedKey);
			
					smI_Blob blob = smU_Blob.createBlobInstance(tuple.m_blobType);
					smU_Blob.readBytes(blob, entity);
					
					toReturn.put(tuple.m_keySource, blob);
				}
				
				return toReturn;
			}
		}
		catch(Exception e)
		{
			throw new smBlobException("Error occurred while getting blob batch.", e);
		}
		
		return null;
	}
	
	private List<Entity> createEntitiesForPut(Map<smI_BlobKey, smI_Blob> values) throws smBlobException
	{
		ArrayList<Entity> entities = new ArrayList<Entity>();
		
		Iterator<? extends smI_BlobKey> keySourceIterator = values.keySet().iterator();
		while( keySourceIterator.hasNext() )
		{
			smI_BlobKey keySource = keySourceIterator.next();
			smI_Blob nextBlob = values.get(keySource);
			
			Entity entity = createEntityForPut(keySource, nextBlob);
			entities.add(entity);
		}
		
		return entities;
	}
	
	@Override
	public void putBlobs(Map<smI_BlobKey, smI_Blob> values) throws smBlobException
	{
		List<Entity> entities = createEntitiesForPut(values);
		
		try
		{
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Transaction currentTransaction_canBeNull = smBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
			datastore.put(currentTransaction_canBeNull, entities);
		}
		catch(Exception e)
		{
			throw new smBlobException("Error occurred while putting blob batch.", e);
		}
	}
	
	@Override
	public void putBlobsAsync(Map<smI_BlobKey, smI_Blob> values) throws smBlobException
	{
		List<Entity> entities = createEntitiesForPut(values);
		
		AsyncDatastoreService datastore = DatastoreServiceFactory.getAsyncDatastoreService();
		Transaction currentTransaction = smBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
		
		if( currentTransaction != null )
		{
			throw new smBlobException("Can't do async multi-put within a blob transaction.");
		}
		
		try
		{
			datastore.put(entities);
		}
		catch(Exception e)
		{
			throw new smBlobException("Error occurred while putting blob batch.", e);
		}
	}

	@Override
	public void deleteBlob(smI_BlobKey keySource, Class<? extends smI_Blob> blobType) throws smBlobException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		smI_Blob blobTemplate = m_templateMngr.getTemplate(blobType);
		
		Key keyObject = KeyFactory.createKey(blobTemplate.getKind(), keySource.createBlobKey(blobTemplate));
	
		try
		{
			Transaction currentTransaction_canBeNull = smBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
			
			datastore.delete(currentTransaction_canBeNull, keyObject);
		}
		catch(Exception e)
		{
			throw new smBlobException("Unknown error occurred while deleting blob (sync).", e);
		}
	}
	
	@Override
	public void deleteBlobAsync(smI_BlobKey keySource, Class<? extends smI_Blob> blobType) throws smBlobException
	{
		AsyncDatastoreService datastore = DatastoreServiceFactory.getAsyncDatastoreService();
		
		Transaction currentTransaction = smBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
		
		if( currentTransaction != null )
		{
			throw new smBlobException("Can't delete within transaction.");
		}
		
		smI_Blob blobTemplate = m_templateMngr.getTemplate(blobType);
		
		Key keyObject = KeyFactory.createKey(blobTemplate.getKind(), keySource.createBlobKey(blobTemplate));
	
		try
		{
			datastore.delete(keyObject);
		}
		catch(Exception e)
		{
			throw new smBlobException("Unknown error occurred while deleting blob (async).", e);
		}
	}
	
	private List<Key> createKeysForBatchDelete(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws smBlobException
	{
		ArrayList<Key> keys = new ArrayList<Key>();
		
		Iterator<smI_BlobKey> iterator = values.keySet().iterator();
		while( iterator.hasNext() )
		{
			smI_BlobKey keySource = iterator.next();
			smI_Blob blob = m_templateMngr.getTemplate(values.get(keySource));
			
			Key keyObject = KeyFactory.createKey(blob.getKind(), keySource.createBlobKey(blob));
			
			keys.add(keyObject);
		}
		
		return keys;
	}
	
	@Override
	public void deleteBlobs(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws smBlobException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		List<Key> keys = createKeysForBatchDelete(values);
		
		try
		{
			Transaction currentTransaction_canBeNull = smBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
			
			datastore.delete(currentTransaction_canBeNull, keys);
		}
		catch(Exception e)
		{
			throw new smBlobException("Unknown error occurred while deleting blobs (sync).", e);
		}
	}
	
	@Override
	public void deleteBlobsAsync(Map<smI_BlobKey, Class<? extends smI_Blob>> values) throws smBlobException
	{
		AsyncDatastoreService datastore = DatastoreServiceFactory.getAsyncDatastoreService();
		
		Transaction currentTransaction = smBlobTransactionManager.getInstance().getCurrentTransaction(datastore);
		
		if( currentTransaction != null )
		{
			throw new smBlobException("Can't delete within transaction.");
		}
		
		List<Key> keys = createKeysForBatchDelete(values);
		
		try
		{
			datastore.delete(keys);
		}
		catch(Exception e)
		{
			throw new smBlobException("Unknown error occurred while deleting blobs (async).", e);
		}
	}

	@Override
	public Map<smI_BlobKey, smI_Blob> performQuery(smBlobQuery query) throws smBlobException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		smI_Blob blobTemplate = m_templateMngr.getTemplate(query.getBlobType());
		
		/*Query query = new Query(blobTemplate.getKind());
		FilterOperator filter = new FilterOperator();
		query.addFilter(propertyName, operator, value)*/
		
		return null;
	}
}
