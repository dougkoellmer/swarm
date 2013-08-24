package swarm.server.transaction;

import java.util.ArrayList;

import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class bhTransactionBatch
{
	private static class Transaction
	{
		private final bhTransactionRequest m_request;
		private final bhTransactionResponse m_response;
		
		Transaction(bhTransactionRequest request, bhTransactionResponse response)
		{
			m_request = request;
			m_response = response;
		}
	}
	
	private final ArrayList<Transaction> m_queue = new ArrayList<Transaction>();
	
	bhTransactionBatch()
	{
		
	}
	
	public void add(bhTransactionRequest request, bhTransactionResponse response)
	{
		m_queue.add(new Transaction(request, response));
	}
	
	public int getCount()
	{
		return m_queue.size();
	}
	
	public bhTransactionRequest getRequest(int index)
	{
		return m_queue.get(index).m_request;
	}
	
	public bhTransactionResponse getResponse(int index)
	{
		return m_queue.get(index).m_response;
	}
	
	void removeHandledTransactions()
	{
		for( int i = m_queue.size()-1; i >= 0; i-- )
		{
			if( m_queue.get(i).m_response.getError() != bhE_ResponseError.DEFERRED )
			{
				m_queue.remove(i);
			}
		}
	}
	
	void markUnhandledTransactions(bhE_ResponseError error)
	{
		for( int i = m_queue.size()-1; i >= 0; i-- )
		{
			if( m_queue.get(i).m_response.getError() == bhE_ResponseError.DEFERRED )
			{
				m_queue.get(i).m_response.setError(error);
			}
		}
	}
}
