package com.b33hive.client.transaction;

import java.util.ArrayList;

import com.b33hive.client.js.bhU_Native;
import com.b33hive.shared.debugging.bhU_Debug;
import com.b33hive.shared.json.bhA_JsonFactory;
import com.b33hive.shared.json.bhI_JsonArray;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

public class bhInlineRequestDispatcher implements bhI_RequestDispatcher
{
	private static class QueuedTransaction
	{
		QueuedTransaction(bhTransactionRequest request, Object responseObject)
		{
			m_request = request;
			m_responseObject = responseObject;
		}
		
		final bhTransactionRequest m_request;
		final Object m_responseObject;
	}
	
	private static class InlineTransaction
	{
		InlineTransaction(bhTransactionRequest request, bhTransactionResponse response)
		{
			m_request = request;
			m_response = response;
		}
		
		final bhTransactionRequest m_request;
		final bhTransactionResponse m_response;
	}
	
	private bhClientTransactionManager m_manager;
	
	private final ArrayList<InlineTransaction> m_inlineTransactions = new ArrayList<InlineTransaction>();
	private final ArrayList<QueuedTransaction> m_queuedTransactions = new ArrayList<QueuedTransaction>();
	
	public bhInlineRequestDispatcher()
	{
		JsArray batch = bhU_Native.getGlobalArray("bh_rl");
		
		for( int i = 0; i < batch.length(); i++ )
		{
			JsArrayString entry = (JsArrayString) batch.get(i);
			
			String requestJson = entry.get(0);
			String responseJson = entry.get(1);
			
			bhTransactionRequest request = new bhTransactionRequest();
			bhTransactionResponse response = new bhTransactionResponse();
			
			try
			{
				request.readJson(requestJson);
				response.readJson(responseJson);
				
				m_inlineTransactions.add(new InlineTransaction(request, response));
			}
			catch(Throwable e)
			{
				bhU_Debug.ASSERT(false, "Couldn't read inline request (" + requestJson + ") or response(" + responseJson + ")");
			}
		}
		
		//--- DRK > Release memory to GC.
		bhU_Native.setGlobalObject("bh_rl", null);
	}
	
	@Override
	public void initialize(bhClientTransactionManager manager)
	{
		m_manager = manager;
	}
	
	public void flushQueuedSynchronousResponses()
	{
		for(int i = 0; i < m_queuedTransactions.size(); i++ )
		{
			QueuedTransaction transaction = m_queuedTransactions.get(i);
			
			this.getBackToManager(transaction.m_request, transaction.m_responseObject);
		}
		
		m_queuedTransactions.clear();
	}
	
	private void queueTransaction(bhTransactionRequest request, Object responseObject)
	{
		m_queuedTransactions.add(new QueuedTransaction(request, responseObject));
	}
	
	private void getBackToManager(bhTransactionRequest request, Object responseObject)
	{
		if( request instanceof bhTransactionRequestBatch )
		{
			m_manager.onResponseReceived((bhTransactionRequestBatch)request, (bhI_JsonArray)responseObject);
		}
		else
		{
			m_manager.onResponseReceived(request, (bhTransactionResponse)responseObject);
		}
	}

	@Override
	public boolean dispatch(bhTransactionRequest request)
	{
		if( m_inlineTransactions.size() == 0 )  return false;
		
		if( request instanceof bhTransactionRequestBatch )
		{
			bhTransactionRequestBatch requestBatch = (bhTransactionRequestBatch) request;
			
			bhI_JsonArray responseBatch = null;//
			bhTransactionRequestBatch inlineRequestBatch = null;
			
			int handledCount = 0;
			for( int i = 0; i < requestBatch.getSize(); i++ )
			{
				bhTransactionRequest ithRequest = requestBatch.getRequest(i);
				InlineTransaction transaction = getTransaction(ithRequest);
				
				if( transaction != null )
				{
					responseBatch = responseBatch == null ? bhA_JsonFactory.getInstance().createJsonArray() : responseBatch;
					inlineRequestBatch = inlineRequestBatch == null ? new bhTransactionRequestBatch() : inlineRequestBatch;
					
					inlineRequestBatch.addRequest(transaction.m_request);
					responseBatch.addObject(transaction.m_response.writeJson());
					
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
	
	private InlineTransaction getTransaction(bhTransactionRequest request)
	{
		for( int i = 0; i < m_inlineTransactions.size(); i++ )
		{
			InlineTransaction transaction = m_inlineTransactions.get(i);
			bhTransactionRequest inlineRequest = transaction.m_request;
			
			if( inlineRequest.isEqualTo(request) )
			{
				m_inlineTransactions.remove(i);
				
				return transaction;
			}
		}
		
		return null;
	}
}
