package swarm.server.transaction;

import java.util.ArrayList;

import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;


class TransactionResponseBatch extends TransactionResponse
{
	private final ArrayList<TransactionResponse> m_responses = new ArrayList<TransactionResponse>();
	
	TransactionResponseBatch(A_JsonFactory jsonFactory)
	{
		super(jsonFactory);
	}
	
	public void addResponse(TransactionResponse response)
	{
		m_responses.add(response);
	}
	
	@Override public boolean hasError()
	{
		for( int i = 0; i < m_responses.size(); i++ )
		{
			if( m_responses.get(i).hasError() )
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		super.writeJson(factory, json_out);
		
		final I_JsonArray responsesJson = factory.createJsonArray();
		
		for ( int i = 0; i < m_responses.size(); i++ )
		{
			TransactionResponse ithResponse = m_responses.get(i);
			
			I_JsonObject jsonObject = factory.createJsonObject();
			ithResponse.writeJson(factory, jsonObject);
			responsesJson.addObject(jsonObject);
		}
		
		factory.getHelper().putJsonArray(json_out, E_JsonKey.responseList, responsesJson);
	}
}
