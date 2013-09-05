package swarm.server.transaction;

import java.util.ArrayList;

import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class smTransactionBatch
{
	private static class Transaction
	{
		private final smTransactionRequest m_request;
		private final smTransactionResponse m_response;
		
		Transaction(smTransactionRequest request, smTransactionResponse response)
		{
			m_request = request;
			m_response = response;
		}
	}
	
	private final ArrayList<Transaction> m_queue = new ArrayList<Transaction>();
	
	smTransactionBatch()
	{
		
	}
	
	void add(smTransactionRequest request, smTransactionResponse response)
	{
		m_queue.add(new Transaction(request, response));
	}
	
	public int getCount()
	{
		return m_queue.size();
	}
	
	public smTransactionRequest getRequest(int index)
	{
		return m_queue.get(index).m_request;
	}
	
	public smTransactionResponse getResponse(int index)
	{
		return m_queue.get(index).m_response;
	}
	
	void removeHandledTransactions()
	{
		for( int i = m_queue.size()-1; i >= 0; i-- )
		{
			if( m_queue.get(i).m_response.getError() != smE_ResponseError.DEFERRED )
			{
				m_queue.remove(i);
			}
		}
	}
	
	void markUnhandledTransactions(smE_ResponseError error)
	{
		for( int i = m_queue.size()-1; i >= 0; i-- )
		{
			if( m_queue.get(i).m_response.getError() == smE_ResponseError.DEFERRED )
			{
				m_queue.get(i).m_response.setError(error);
			}
		}
	}
}
