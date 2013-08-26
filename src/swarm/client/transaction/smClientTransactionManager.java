package swarm.client.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import swarm.client.time.smU_Time;
import swarm.shared.utils.smListenerManager;
import swarm.shared.app.smA_App;
import swarm.shared.app.smS_App;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonEncodable;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonQuery;
import swarm.shared.json.smJsonHelper;
import swarm.shared.transaction.smA_TransactionObject;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_ReservedRequestPath;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smI_RequestPath;
import swarm.shared.transaction.smRequestPathManager;
import swarm.shared.transaction.smS_Transaction;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;
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
public class smClientTransactionManager
{	
	private static final Logger s_logger = Logger.getLogger(smClientTransactionManager.class.getName());
		
	private final smListenerManager<smI_TransactionResponseHandler> m_handlers = new smListenerManager<smI_TransactionResponseHandler>();
	private final smListenerManager<smI_ResponseBatchListener> m_batchListeners = new smListenerManager<smI_ResponseBatchListener>();
	
	private bhTransactionRequestBatch m_transactionRequestBatch = null;
	
	private smI_SyncRequestDispatcher m_syncDispatcher = null;
	private smI_AsyncRequestDispatcher m_asyncDispatcher = null;
	
	private final smTransactionResponse m_reusedResponse = new smTransactionResponse();
	
	private boolean m_isInsideBatch = false;
	
	private bhTransactionRequestBatch m_currentRequestBatch = null;
	private smI_JsonArray m_currentResponseList = null;
	private bhTransactionResponse m_currentErrorResponse = null;
	private int m_currentBatchIndex = 0;
	
	private bhTransactionRequest m_currentlyHandledRequest;
	
	private final smI_ResponseCallbacks m_callbacks = new smI_ResponseCallbacks()
	{
		@Override
		public void onResponseReceived(smTransactionRequest request, smTransactionResponse response)
		{
			bhClientTransactionManager.this.onResponseReceived(request, response);
		}
		
		@Override
		public void onResponseReceived(smTransactionRequestBatch requestBatch, smI_JsonArray jsonResponseBatch)
		{
			bhClientTransactionManager.this.onResponseReceived(requestBatch, jsonResponseBatch);
		}
		
		@Override
		public void onError(smTransactionRequest request, smTransactionResponse response)
		{
			bhClientTransactionManager.this.onError(request, response);
		}
	};
	
	public smClientTransactionManager(smRequestPathManager requestPathMngr) 
	{
		requestPathMngr.register(smE_ReservedRequestPath.values());
	}
	
	public void setSyncRequestDispatcher(smI_SyncRequestDispatcher dispatcher)
	{
		m_syncDispatcher = dispatcher;
		m_syncDispatcher.initialize(m_callbacks, smS_Transaction.MAX_GET_URL_LENGTH);
	}
	
	public void setAsyncRequestDispatcher(smI_AsyncRequestDispatcher dispatcher)
	{
		m_asyncDispatcher = dispatcher;
		m_asyncDispatcher.initialize(m_callbacks, smS_Transaction.MAX_GET_URL_LENGTH);
	}
	
	private void callSuccessHandlers(smTransactionRequest request, smTransactionResponse response)
	{
		bhU_Debug.ASSERT(request.isCancelled() == false, "callSuccessHandlers1");
		
		m_currentlyHandledRequest = request;
		
		ArrayList<smI_TransactionResponseHandler> list = m_handlers.getListeners();
		
		for ( int i = list.size()-1; i >= 0; i-- )
		{
			smE_ResponseSuccessControl controlStatement = list.get(i).onResponseSuccess(request, response);
			
			switch(controlStatement)
			{
				case BREAK:  return;
			}
		}
		
		m_currentlyHandledRequest = null;
	}
	
	private void callErrorHandlers(smTransactionRequest request, smTransactionResponse response)
	{
		bhU_Debug.ASSERT(request.isCancelled() == false, "callErrorHandlers");
		
		m_currentlyHandledRequest = request;
		
		ArrayList<smI_TransactionResponseHandler> list = m_handlers.getListeners();
		
		for ( int i = list.size()-1; i >= 0; i-- )
		{
			smE_ResponseErrorControl controlStatement = list.get(i).onResponseError(request, response);
			
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
	
	public void queueRequest(smE_RequestPath path, smI_JsonEncodable jsonEncodable)
	{
		bhTransactionRequest request = new smTransactionRequest(path);
		jsonEncodable.writeJson(request.getJson());
		queueRequest(request);
	}
	
	public void queueRequest(smE_RequestPath requestPath)
	{
		bhTransactionRequest request = new smTransactionRequest(requestPath);
		queueRequest(request);
	}
	
	public void queueRequest(smTransactionRequest request)
	{
		if( m_transactionRequestBatch == null )
		{
			m_transactionRequestBatch = new smTransactionRequestBatch();
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
	
	public void flushSyncResponses()
	{
		if( m_syncDispatcher != null )
		{
			m_syncDispatcher.flushResponses();
		}
	}
	
	public void makeRequest(smI_RequestPath path, smI_JsonObject jsonArgs)
	{
		bhTransactionRequest request = new smTransactionRequest(path, jsonArgs);
		makeRequest(request);
	}
	
	public void makeRequest(smI_RequestPath path, smI_JsonEncodable jsonEncodable)
	{
		bhTransactionRequest request = new smTransactionRequest(path);
		jsonEncodable.writeJson(request.getJson());
		makeRequest(request);
	}
	
	public void makeRequest(smI_RequestPath path)
	{
		bhTransactionRequest request = new smTransactionRequest(path);
		makeRequest(request);
	}
	
	public void performAction(smE_TransactionAction action, smE_RequestPath requestPath)
	{
		bhTransactionRequest request = new smTransactionRequest(requestPath);
		performAction(action, request);
	}
	
	public void performAction(smE_TransactionAction action, smE_RequestPath requestPath, smI_JsonEncodable jsonEncodable)
	{
		bhTransactionRequest request = new smTransactionRequest(requestPath);
		jsonEncodable.writeJson(request.getJson());
		performAction(action, request);
	}
	
	/**
	 * This allows other classes (mainly various managers) to delegate how
	 * they send their transactions to the caller of the manager's methods.
	 */
	public void performAction(smE_TransactionAction action, smTransactionRequest request)
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
	
	public void makeRequest(smTransactionRequest request)
	{
		request.setServerVersion(smS_App.SERVER_VERSION);
		request.onDispatch(smU_Time.getMilliseconds());
		
		if( m_syncDispatcher.dispatch(request) ){}
		else if( m_asyncDispatcher.dispatch(request) ){}
		else
		{
			//TODO: Create a third "error" dispatcher that simply responds to requests with CLIENT_EXCEPTION (or a new error type)
			//		The error here Is guess is that something was so fucked we couldn't even get the request out the door with the other two dispatchers.
		}
	}
	
	public smTransactionResponse getPreviousBatchResponse(smE_RequestPath path)
	{
		Object responseObject = getPreviousBatchResponseObject(path);
		
		if( responseObject != null )
		{
			if( m_currentErrorResponse != null )
			{
				return (smTransactionResponse)responseObject;
			}
			else
			{
				smI_JsonObject responseJson = (smI_JsonObject) responseObject;
				bhTransactionResponse response = new smTransactionResponse();
				response.readJson(responseJson);
				
				return response;
			}
		}
		
		return null;
	}
	
	public boolean hasPreviousBatchResponse(smE_RequestPath path)
	{
		return getPreviousBatchResponseObject(path) != null;
	}
	
	private Object getPreviousBatchResponseObject(smE_RequestPath path)
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
	
	private void onBatchResponseStart(smTransactionRequestBatch batch, smI_JsonArray responseList)
	{
		m_currentResponseList = responseList;
		
		onBatchStart_shared(batch);
	}
	
	private void onBatchResponseStartWithError(smTransactionRequestBatch batch, smTransactionResponse errorResponse)
	{
		m_currentErrorResponse = errorResponse;
		
		onBatchStart_shared(batch);
	}
	
	private void onBatchStart_shared(smTransactionRequestBatch batch)
	{
		m_currentRequestBatch = batch;
		m_isInsideBatch = true;
		
		ArrayList<smI_ResponseBatchListener> list = m_batchListeners.getListeners();
		
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
		
		ArrayList<smI_ResponseBatchListener> list = m_batchListeners.getListeners();
		
		for ( int i = list.size()-1; i >= 0; i-- )
		{
			list.get(i).onResponseBatchEnd();
		}
	}
	
	public void addBatchListener(smI_ResponseBatchListener listener)
	{
		m_batchListeners.addListenerToBack(listener);
	}
	
	public void removeBatchListener(smI_ResponseBatchListener listener)
	{
		m_batchListeners.removeListener(listener);
	}
	
	public void addHandler(smI_TransactionResponseHandler handler)
	{
		m_handlers.addListenerToBack(handler);
	}
	
	public void removeHandler(smI_TransactionResponseHandler handler)
	{
		m_handlers.removeListener(handler);
	}
	
	public smTransactionRequest getDispatchedRequest(smE_RequestPath path)
	{
		return this.getDispatchedRequest(path, null);
	}
	
	public smTransactionRequest getDispatchedRequest(smI_RequestPath path, smJsonQuery jsonQuery)
	{
		bhTransactionRequest dispatchedRequest = m_asyncDispatcher.getDispatchedRequest(path, jsonQuery, m_currentlyHandledRequest);
		
		return dispatchedRequest;
	}
	
	public boolean containsDispatchedRequest(smI_RequestPath path)
	{
		return this.containsDispatchedRequest(path, null);
	}
	
	public boolean containsDispatchedRequest(smI_RequestPath path, smJsonQuery query)
	{
		return this.getDispatchedRequest(path, query) != null;
	}
	
	public void cancelRequestsByPaths(smI_RequestPath ... paths)
	{
		for( int i = 0; i < paths.length; i++ )
		{
			cancelRequestsByPath(paths[i]);
		}
	}
	
	public void cancelRequestsByPath(smI_RequestPath path)
	{
		m_asyncDispatcher.cancelRequestsByPath(path, m_currentlyHandledRequest);
	}
	
	void onResponseReceived(smTransactionRequestBatch requestBatch, smI_JsonArray jsonResponseBatch)
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
			
			smI_JsonObject jsonObject = jsonResponseBatch.getObject(i);
			
			m_reusedResponse.reset();
			
			m_reusedResponse.readJson(jsonObject);
			
			if( m_reusedResponse.getError() == smE_ResponseError.NO_ERROR )
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
	
	void onResponseReceived(smTransactionRequest request, smTransactionResponse response)
	{
		if( response.getError() == smE_ResponseError.NO_ERROR )
		{
			this.callSuccessHandlers(request, response);
		}
		else
		{
			this.callErrorHandlers(request, response);
		}
	}
	
	void onError(smTransactionRequest request, smTransactionResponse response)
	{			
		if( !(request instanceof bhTransactionRequestBatch) )
		{
			this.callErrorHandlers(request, response);
		}
		else
		{
			bhTransactionRequestBatch batch = (smTransactionRequestBatch) request;
			
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
