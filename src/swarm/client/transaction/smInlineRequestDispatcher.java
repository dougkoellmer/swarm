package swarm.client.transaction;

import java.util.ArrayList;

import swarm.client.js.smU_Native;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

public class smInlineRequestDispatcher implements smI_SyncRequestDispatcher
{
	private static class QueuedTransaction
	{
		QueuedTransaction(smTransactionRequest request, Object responseObject)
		{
			m_request = request;
			m_responseObject = responseObject;
		}
		
		final smTransactionRequest m_request;
		final Object m_responseObject;
	}
	
	private static class InlineTransaction
	{
		InlineTransaction(smTransactionRequest request, smTransactionResponse response)
		{
			m_request = request;
			m_response = response;
		}
		
		final smTransactionRequest m_request;
		final smTransactionResponse m_response;
	}
	
	private smI_ResponseCallbacks m_callbacks;
	
	private final ArrayList<InlineTransaction> m_inlineTransactions = new ArrayList<InlineTransaction>();
	private final ArrayList<QueuedTransaction> m_queuedTransactions = new ArrayList<QueuedTransaction>();
	
	private final String m_appId;
	
	private final smA_JsonFactory m_jsonFactory;
	
	public smInlineRequestDispatcher(smA_JsonFactory jsonFactory, String appId)
	{
		m_appId = appId;
		m_jsonFactory = jsonFactory;
		
		JsArray batch = smU_Native.getGlobalArray(m_appId+"_rl");
		
		for( int i = 0; i < batch.length(); i++ )
		{
			JsArrayString entry = (JsArrayString) batch.get(i);
			
			smTransactionRequest request = new smTransactionRequest();
			smTransactionResponse response = new smTransactionResponse();
			
			String requestJsonString = entry.get(0);
			String responseJsonString = entry.get(1);
			
			try
			{
				smI_JsonObject requestJson = m_jsonFactory.createJsonObject(requestJsonString);
				smI_JsonObject responseJson = m_jsonFactory.createJsonObject(responseJsonString);
				
				request.readJson(m_jsonFactory, requestJson);
				response.readJson(m_jsonFactory, responseJson);
				
				m_inlineTransactions.add(new InlineTransaction(request, response));
			}
			catch(Throwable e)
			{
				smU_Debug.ASSERT(false, "Couldn't read inline request (" + requestJsonString + ") or response(" + responseJsonString + ")");
			}
		}
		
		//--- DRK > Release memory to GC.
		smU_Native.setGlobalObject(m_appId+"_rl", null);
	}
	
	@Override
	public void initialize(smI_ResponseCallbacks callbacks, int maxGetUrlLength)
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
	
	private void queueTransaction(smTransactionRequest request, Object responseObject)
	{
		m_queuedTransactions.add(new QueuedTransaction(request, responseObject));
	}
	
	private void getBackToManager(smTransactionRequest request, Object responseObject)
	{
		if( request instanceof smTransactionRequestBatch )
		{
			m_callbacks.onResponseReceived((smTransactionRequestBatch)request, (smI_JsonArray)responseObject);
		}
		else
		{
			m_callbacks.onResponseReceived(request, (smTransactionResponse)responseObject);
		}
	}

	@Override
	public boolean dispatch(smTransactionRequest request)
	{
		if( m_inlineTransactions.size() == 0 )  return false;
		
		if( request instanceof smTransactionRequestBatch )
		{
			smTransactionRequestBatch requestBatch = (smTransactionRequestBatch) request;
			
			smI_JsonArray responseBatch = null;//
			smTransactionRequestBatch inlineRequestBatch = null;
			
			int handledCount = 0;
			for( int i = 0; i < requestBatch.getSize(); i++ )
			{
				smTransactionRequest ithRequest = requestBatch.getRequest(i);
				InlineTransaction transaction = getTransaction(ithRequest);
				
				if( transaction != null )
				{
					responseBatch = responseBatch == null ? m_jsonFactory.createJsonArray() : responseBatch;
					inlineRequestBatch = inlineRequestBatch == null ? new smTransactionRequestBatch() : inlineRequestBatch;
					
					inlineRequestBatch.addRequest(transaction.m_request);
					smI_JsonObject responseJson = m_jsonFactory.createJsonObject();
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
	
	private InlineTransaction getTransaction(smTransactionRequest request)
	{
		for( int i = 0; i < m_inlineTransactions.size(); i++ )
		{
			InlineTransaction transaction = m_inlineTransactions.get(i);
			smTransactionRequest inlineRequest = transaction.m_request;
			
			if( inlineRequest.isEqualTo(request) )
			{
				m_inlineTransactions.remove(i);
				
				return transaction;
			}
		}
		
		return null;
	}
}
