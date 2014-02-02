package swarm.client.transaction;

import java.util.ArrayList;

import swarm.client.js.U_Native;
import swarm.shared.app.BaseAppContext;
import swarm.shared.debugging.U_Debug;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.transaction.RequestPathManager;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

public class InlineRequestDispatcher implements I_SyncRequestDispatcher
{
	private static class QueuedTransaction
	{
		QueuedTransaction(TransactionRequest request, Object responseObject)
		{
			m_request = request;
			m_responseObject = responseObject;
		}
		
		final TransactionRequest m_request;
		final Object m_responseObject;
	}
	
	private static class InlineTransaction
	{
		InlineTransaction(TransactionRequest request, TransactionResponse response)
		{
			m_request = request;
			m_response = response;
		}
		
		final TransactionRequest m_request;
		final TransactionResponse m_response;
	}
	
	private I_ResponseCallbacks m_callbacks;
	
	private final ArrayList<InlineTransaction> m_inlineTransactions = new ArrayList<InlineTransaction>();
	private final ArrayList<QueuedTransaction> m_queuedTransactions = new ArrayList<QueuedTransaction>();
	
	private final String m_appId;
	
	private final RequestPathManager m_requestPathMngr;
	private final A_JsonFactory m_jsonFactory;
	
	public InlineRequestDispatcher(A_JsonFactory jsonFactory, RequestPathManager requestPathMngr, String appId)
	{
		m_appId = appId;
		m_jsonFactory = jsonFactory;
		m_requestPathMngr = requestPathMngr;
		
		JsArray batch = U_Native.getGlobalArray(m_appId+"_rl");
		
		for( int i = 0; i < batch.length(); i++ )
		{
			JsArrayString entry = (JsArrayString) batch.get(i);
			
			TransactionRequest request = new TransactionRequest(m_jsonFactory);
			TransactionResponse response = new TransactionResponse(m_jsonFactory);
			
			String requestJsonString = entry.get(0);
			String responseJsonString = entry.get(1);
			
			try
			{
				I_JsonObject requestJson = m_jsonFactory.createJsonObject(requestJsonString);
				I_JsonObject responseJson = m_jsonFactory.createJsonObject(responseJsonString);
				
				request.readJson(m_jsonFactory, m_requestPathMngr, requestJson);
				response.readJson(m_jsonFactory, responseJson);
				
				m_inlineTransactions.add(new InlineTransaction(request, response));
			}
			catch(Throwable e)
			{
				U_Debug.ASSERT(false, "Couldn't read inline request (" + requestJsonString + ") or response(" + responseJsonString + ")");
			}
		}
		
		//--- DRK > Release memory.
		U_Native.setGlobalObject(m_appId+"_rl", null);
	}
	
	@Override
	public void initialize(I_ResponseCallbacks callbacks, int maxGetUrlLength)
	{
		m_callbacks = callbacks;
	}
	
	public void flushResponses()
	{
		for(int i = 0; i < m_queuedTransactions.size(); i++ )
		{
			QueuedTransaction transaction = m_queuedTransactions.get(i);
			
			this.getBackToManager(transaction.m_request, transaction.m_responseObject);
		}
		
		m_queuedTransactions.clear();
	}
	
	private void queueTransaction(TransactionRequest request, Object responseObject)
	{
		m_queuedTransactions.add(new QueuedTransaction(request, responseObject));
	}
	
	private void getBackToManager(TransactionRequest request, Object responseObject)
	{
		if( request instanceof TransactionRequestBatch )
		{
			m_callbacks.onResponseReceived((TransactionRequestBatch)request, (I_JsonArray)responseObject);
		}
		else
		{
			m_callbacks.onResponseReceived(request, (TransactionResponse)responseObject);
		}
	}

	@Override
	public boolean dispatch(TransactionRequest request)
	{
		if( m_inlineTransactions.size() == 0 )  return false;
		
		if( request instanceof TransactionRequestBatch )
		{
			TransactionRequestBatch requestBatch = (TransactionRequestBatch) request;
			
			I_JsonArray responseBatch = null;
			TransactionRequestBatch inlineRequestBatch = null;
			
			int handledCount = 0;
			for( int i = 0; i < requestBatch.getSize(); i++ )
			{
				TransactionRequest ithRequest = requestBatch.getRequest(i);
				InlineTransaction transaction = getTransaction(ithRequest);
				
				if( transaction != null )
				{
					responseBatch = responseBatch == null ? m_jsonFactory.createJsonArray() : responseBatch;
					inlineRequestBatch = inlineRequestBatch == null ? new TransactionRequestBatch(m_jsonFactory) : inlineRequestBatch;
					
					inlineRequestBatch.addRequest(transaction.m_request);
					I_JsonObject responseJson = m_jsonFactory.createJsonObject();
					transaction.m_response.writeJson(m_jsonFactory, responseJson);
					responseBatch.addObject(responseJson);
					
					ithRequest.cancel();
					
					handledCount++;
				}
			}
			
			if( responseBatch != null )
			{
				this.queueTransaction(inlineRequestBatch, responseBatch);
			}
			
			if( handledCount == requestBatch.getSize() )
			{
				return true;
			}
		}
		else
		{
			InlineTransaction transaction = getTransaction(request);
			
			if( transaction != null )
			{
				this.queueTransaction(request, transaction.m_response);
				
				return true;
			}
		}
		
		return false;
	}
	
	private InlineTransaction getTransaction(TransactionRequest request)
	{
		for( int i = 0; i < m_inlineTransactions.size(); i++ )
		{
			InlineTransaction transaction = m_inlineTransactions.get(i);
			TransactionRequest inlineRequest = transaction.m_request;
			
			if( inlineRequest.isEqualTo(request) )
			{
				m_inlineTransactions.remove(i);
				
				return transaction;
			}
		}
		
		return null;
	}
}
