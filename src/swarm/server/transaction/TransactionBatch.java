package swarm.server.transaction;

import java.util.ArrayList;

import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class TransactionBatch
{
	private static class Transaction
	{
		private final TransactionRequest m_request;
		private final TransactionResponse m_response;
		
		Transaction(TransactionRequest request, TransactionResponse response)
		{
			m_request = request;
			m_response = response;
		}
	}
	
	private final ArrayList<Transaction> m_queue = new ArrayList<Transaction>();
	
	TransactionBatch()
	{
		
	}
	
	void add(TransactionRequest request, TransactionResponse response)
	{
		m_queue.add(new Transaction(request, response));
	}
	
	public int getCount()
	{
		return m_queue.size();
	}
	
	public TransactionRequest getRequest(int index)
	{
		return m_queue.get(index).m_request;
	}
	
	public TransactionResponse getResponse(int index)
	{
		return m_queue.get(index).m_response;
	}
	
	void removeHandledTransactions()
	{
		for( int i = m_queue.size()-1; i >= 0; i-- )
		{
			if( m_queue.get(i).m_response.getError() != E_ResponseError.DEFERRED )
			{
				m_queue.remove(i);
			}
		}
	}
	
	void markUnhandledTransactions(E_ResponseError error)
	{
		for( int i = m_queue.size()-1; i >= 0; i-- )
		{
			if( m_queue.get(i).m_response.getError() == E_ResponseError.DEFERRED )
			{
				m_queue.get(i).m_response.setError(error);
			}
		}
	}
}
