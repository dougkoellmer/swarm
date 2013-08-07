package b33hive.client.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import b33hive.client.time.bhU_Time;
import b33hive.shared.utils.bhListenerManager;
import b33hive.shared.app.bhA_App;
import b33hive.shared.app.bhS_App;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.json.bhI_JsonEncodable;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonQuery;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.transaction.bhA_TransactionObject;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhE_ReservedRequestPath;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhI_RequestPath;
import b33hive.shared.transaction.bhRequestPathManager;
import b33hive.shared.transaction.bhS_Transaction;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;

/**
 * TODO: Need to have stricter runtime handling of when you can cancel requests.
 *		For example, it's now possible to cancel a request inside success and error handlers.
 *		Should this be allowed in any way?  If so, how should it work?.
 */
public class bhClientTransactionManager
{	
	private static final Logger s_logger = Logger.getLogger(bhClientTransactionManager.class.getName());
		
	private final bhListenerManager<bhI_TransactionResponseHandler> m_handlers = new bhListenerManager<bhI_TransactionResponseHandler>();
	private final bhListenerManager<bhI_ResponseBatchListener> m_batchListeners = new bhListenerManager<bhI_ResponseBatchListener>();
	
	private bhTransactionRequestBatch m_transactionRequestBatch = null;
	
	private bhI_RequestDispatcher m_syncDispatcher = null;
	private bhI_AsynchronousRequestDispatcher m_asyncDispatcher = null;
	
	private final bhTransactionResponse m_reusedResponse = new bhTransactionResponse();
	
	private boolean m_isInsideBatch = false;
	
	private bhTransactionRequestBatch m_currentRequestBatch = null;
	private bhI_JsonArray m_currentResponseList = null;
	private bhTransactionResponse m_currentErrorResponse = null;
	private int m_currentBatchIndex = 0;
	
	private bhTransactionRequest m_currentlyHandledRequest;
	
	private final bhI_ResponseCallbacks m_callbacks = new bhI_ResponseCallbacks()
	{
		@Override
		public void onResponseReceived(bhTransactionRequest request, bhTransactionResponse response)
		{
			bhClientTransactionManager.this.onResponseReceived(request, response);
		}
		
		@Override
		public void onResponseReceived(bhTransactionRequestBatch requestBatch, bhI_JsonArray jsonResponseBatch)
		{
			bhClientTransactionManager.this.onResponseReceived(requestBatch, jsonResponseBatch);
		}
		
		@Override
		public void onError(bhTransactionRequest request, bhTransactionResponse response)
		{
			bhClientTransactionManager.this.onError(request, response);
		}
	};
	
	public bhClientTransactionManager(bhRequestPathManager requestPathMngr) 
	{
		requestPathMngr.register(bhE_ReservedRequestPath.values());
	}
	
	public void setSynchronousRequestRouter(bhI_RequestDispatcher router)
	{
		m_syncDispatcher = router;
		m_syncDispatcher.initialize(m_callbacks, bhS_Transaction.MAX_GET_URL_LENGTH);
	}
	
	public void setAsynchronousRequestRouter(bhI_AsynchronousRequestDispatcher router)
	{
		m_asyncDispatcher = router;
		m_asyncDispatcher.initialize(m_callbacks, bhS_Transaction.MAX_GET_URL_LENGTH);
	}
	
	private void callSuccessHandlers(bhTransactionRequest request, bhTransactionResponse response)
	{
		bhU_Debug.ASSERT(request.isCancelled() == false, "callSuccessHandlers1");
		
		m_currentlyHandledRequest = request;
		
		ArrayList<bhI_TransactionResponseHandler> list = m_handlers.getListeners();
		
		for ( int i = list.size()-1; i >= 0; i-- )
		{
			bhE_ResponseSuccessControl controlStatement = list.get(i).onResponseSuccess(request, response);
			
			switch(controlStatement)
			{
				case BREAK:  return;
			}
		}
		
		m_currentlyHandledRequest = null;
	}
	
	private void callErrorHandlers(bhTransactionRequest request, bhTransactionResponse response)
	{
		bhU_Debug.ASSERT(request.isCancelled() == false, "callErrorHandlers");
		
		m_currentlyHandledRequest = request;
		
		ArrayList<bhI_TransactionResponseHandler> list = m_handlers.getListeners();
		
		for ( int i = list.size()-1; i >= 0; i-- )
		{
			bhE_ResponseErrorControl controlStatement = list.get(i).onResponseError(request, response);
			
			switch(controlStatement)
			{
				case BREAK:  return;
				case CONTINUE:
					break;
				default:
					break;
			}
		}
		
		m_currentlyHandledRequest = null;
	}
	
	public boolean isInBatch()
	{
		return m_isInsideBatch;
	}
	
	public void queueRequest(bhE_RequestPath path, bhI_JsonEncodable jsonEncodable)
	{
		bhTransactionRequest request = new bhTransactionRequest(path);
		jsonEncodable.writeJson(request.getJson());
		queueRequest(request);
	}
	
	public void queueRequest(bhE_RequestPath requestPath)
	{
		bhTransactionRequest request = new bhTransactionRequest(requestPath);
		queueRequest(request);
	}
	
	public void queueRequest(bhTransactionRequest request)
	{
		if( m_transactionRequestBatch == null )
		{
			m_transactionRequestBatch = new bhTransactionRequestBatch();
		}
		
		m_transactionRequestBatch.addRequest(request);
	}
	
	public void flushRequestQueue()
	{
		if ( m_transactionRequestBatch == null )  return;		
		
		//trace("flushing " + m_requestQueue.length + " transactions");
		
		this.makeRequest(m_transactionRequestBatch);
		
		m_transactionRequestBatch = null;
	}
	
	public void makeRequest(bhI_RequestPath path, bhI_JsonObject jsonArgs)
	{
		bhTransactionRequest request = new bhTransactionRequest(path, jsonArgs);
		makeRequest(request);
	}
	
	public void makeRequest(bhI_RequestPath path, bhI_JsonEncodable jsonEncodable)
	{
		bhTransactionRequest request = new bhTransactionRequest(path);
		jsonEncodable.writeJson(request.getJson());
		makeRequest(request);
	}
	
	public void makeRequest(bhI_RequestPath path)
	{
		bhTransactionRequest request = new bhTransactionRequest(path);
		makeRequest(request);
	}
	
	public void performAction(bhE_TransactionAction action, bhE_RequestPath requestPath)
	{
		bhTransactionRequest request = new bhTransactionRequest(requestPath);
		performAction(action, request);
	}
	
	public void performAction(bhE_TransactionAction action, bhE_RequestPath requestPath, bhI_JsonEncodable jsonEncodable)
	{
		bhTransactionRequest request = new bhTransactionRequest(requestPath);
		jsonEncodable.writeJson(request.getJson());
		performAction(action, request);
	}
	
	/**
	 * This allows other classes (mainly various managers) to delegate how
	 * they send their transactions to the caller of the manager's methods.
	 */
	public void performAction(bhE_TransactionAction action, bhTransactionRequest request)
	{
		switch(action)
		{
			case MAKE_REQUEST:
			{
				this.makeRequest(request);
				
				break;
			}
			
			case QUEUE_REQUEST:
			{
				this.queueRequest(request);
				
				break;
			}
			
			case QUEUE_REQUEST_AND_FLUSH:
			{
				this.queueRequest(request);
				this.flushRequestQueue();
				
				break;
			}
		}
	}
	
	public void makeRequest(bhTransactionRequest request)
	{
		request.setServerVersion(bhS_App.SERVER_VERSION);
		request.onDispatch(bhU_Time.getMilliseconds());
		
		if( m_syncDispatcher.dispatch(request) ){}
		else if( m_asyncDispatcher.dispatch(request) ){}
		else
		{
			//TODO: Create a third "error" dispatcher that simply responds to requests with CLIENT_EXCEPTION (or a new error type)
			//		The error here Is guess is that something was so fucked we couldn't even get the request out the door with the other two dispatchers.
		}
	}
	
	public bhTransactionResponse getPreviousBatchResponse(bhE_RequestPath path)
	{
		Object responseObject = getPreviousBatchResponseObject(path);
		
		if( responseObject != null )
		{
			if( m_currentErrorResponse != null )
			{
				return (bhTransactionResponse)responseObject;
			}
			else
			{
				bhI_JsonObject responseJson = (bhI_JsonObject) responseObject;
				bhTransactionResponse response = new bhTransactionResponse();
				response.readJson(responseJson);
				
				return response;
			}
		}
		
		return null;
	}
	
	public boolean hasPreviousBatchResponse(bhE_RequestPath path)
	{
		return getPreviousBatchResponseObject(path) != null;
	}
	
	private Object getPreviousBatchResponseObject(bhE_RequestPath path)
	{
		if( !m_isInsideBatch )  return null;

		for( int i = m_currentBatchIndex-1; i >= 0; i-- )
		{
			bhTransactionRequest previousRequest = m_currentRequestBatch.getRequest(i);
			
			if( previousRequest.isCancelled() )  continue;
			
			if( previousRequest.getPath() == path )
			{
				if( m_currentErrorResponse != null )
				{
					return m_currentErrorResponse;
				}
				else
				{
					return m_currentResponseList.getObject(i);
				}
			}
		}
		
		return null;
	}
	
	private void onBatchResponseStart(bhTransactionRequestBatch batch, bhI_JsonArray responseList)
	{
		m_currentResponseList = responseList;
		
		onBatchStart_shared(batch);
	}
	
	private void onBatchResponseStartWithError(bhTransactionRequestBatch batch, bhTransactionResponse errorResponse)
	{
		m_currentErrorResponse = errorResponse;
		
		onBatchStart_shared(batch);
	}
	
	private void onBatchStart_shared(bhTransactionRequestBatch batch)
	{
		m_currentRequestBatch = batch;
		m_isInsideBatch = true;
		
		ArrayList<bhI_ResponseBatchListener> list = m_batchListeners.getListeners();
		
		for ( int i = list.size()-1; i >= 0; i-- )
		{
			list.get(i).onResponseBatchStart();
		}
	}
	
	private void onBatchResponseEnd()
	{
		m_isInsideBatch = false;
		m_currentRequestBatch = null;
		m_currentResponseList = null;
		m_currentErrorResponse = null;
		
		ArrayList<bhI_ResponseBatchListener> list = m_batchListeners.getListeners();
		
		for ( int i = list.size()-1; i >= 0; i-- )
		{
			list.get(i).onResponseBatchEnd();
		}
	}
	
	public void addBatchListener(bhI_ResponseBatchListener listener)
	{
		m_batchListeners.addListenerToBack(listener);
	}
	
	public void removeBatchListener(bhI_ResponseBatchListener listener)
	{
		m_batchListeners.removeListener(listener);
	}
	
	public void addHandler(bhI_TransactionResponseHandler handler)
	{
		m_handlers.addListenerToBack(handler);
	}
	
	public void removeHandler(bhI_TransactionResponseHandler handler)
	{
		m_handlers.removeListener(handler);
	}
	
	public bhTransactionRequest getDispatchedRequest(bhE_RequestPath path)
	{
		return this.getDispatchedRequest(path, null);
	}
	
	public bhTransactionRequest getDispatchedRequest(bhI_RequestPath path, bhJsonQuery jsonQuery)
	{
		bhTransactionRequest dispatchedRequest = m_asyncDispatcher.getDispatchedRequest(path, jsonQuery, m_currentlyHandledRequest);
		
		return dispatchedRequest;
	}
	
	public boolean containsDispatchedRequest(bhI_RequestPath path)
	{
		return this.containsDispatchedRequest(path, null);
	}
	
	public boolean containsDispatchedRequest(bhI_RequestPath path, bhJsonQuery query)
	{
		return this.getDispatchedRequest(path, query) != null;
	}
	
	public void cancelRequestsByPaths(bhI_RequestPath ... paths)
	{
		for( int i = 0; i < paths.length; i++ )
		{
			cancelRequestsByPath(paths[i]);
		}
	}
	
	public void cancelRequestsByPath(bhI_RequestPath path)
	{
		m_asyncDispatcher.cancelRequestsByPath(path, m_currentlyHandledRequest);
	}
	
	void onResponseReceived(bhTransactionRequestBatch requestBatch, bhI_JsonArray jsonResponseBatch)
	{
		if( jsonResponseBatch.getSize() > 1 )
		{
			this.onBatchResponseStart(requestBatch, jsonResponseBatch);
		}
		
		for( int i = 0; i < jsonResponseBatch.getSize(); i++ )
		{
			this.m_currentBatchIndex = i;
			
			bhTransactionRequest ithRequest = requestBatch.getRequest(i);
			
			if( ithRequest.isCancelled() )
			{
				continue;
			}
			
			bhI_JsonObject jsonObject = jsonResponseBatch.getObject(i);
			
			m_reusedResponse.reset();
			
			m_reusedResponse.readJson(jsonObject);
			
			if( m_reusedResponse.getError() == bhE_ResponseError.NO_ERROR )
			{
				this.callSuccessHandlers(ithRequest, m_reusedResponse);
			}
			else
			{
				this.callErrorHandlers(ithRequest, m_reusedResponse);
			}
		}

		if( jsonResponseBatch.getSize() > 1 )
		{
			this.onBatchResponseEnd();
		}
	}
	
	void onResponseReceived(bhTransactionRequest request, bhTransactionResponse response)
	{
		if( response.getError() == bhE_ResponseError.NO_ERROR )
		{
			this.callSuccessHandlers(request, response);
		}
		else
		{
			this.callErrorHandlers(request, response);
		}
	}
	
	void onError(bhTransactionRequest request, bhTransactionResponse response)
	{			
		if( !(request instanceof bhTransactionRequestBatch) )
		{
			this.callErrorHandlers(request, response);
		}
		else
		{
			bhTransactionRequestBatch batch = (bhTransactionRequestBatch) request;
			
			if( batch.getSize() > 1 )
			{
				this.onBatchResponseStartWithError(batch, response);
			}
			
			for( int i = 0; i < batch.getSize(); i++ )
			{
				this.m_currentBatchIndex = i;
				
				if( batch.getRequest(i).isCancelled() )
				{
					continue;
				}
				
				this.callErrorHandlers(batch.getRequest(i), response);
			}
			
			if( batch.getSize() > 1 )
			{
				this.onBatchResponseEnd();
			}
		}
	}
}
