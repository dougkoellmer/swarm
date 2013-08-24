package swarm.server.transaction;

import java.util.ArrayList;

import swarm.shared.app.sm;
import swarm.shared.json.bhA_JsonFactory;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonArray;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;


class bhTransactionResponseBatch extends bhTransactionResponse
{
	private final ArrayList<bhTransactionResponse> m_responses = new ArrayList<bhTransactionResponse>();
	
	bhTransactionResponseBatch()
	{
		super();
	}
	
	public void addResponse(bhTransactionResponse response)
	{
		m_responses.add(response);
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		super.writeJson(json);
		
		final bhI_JsonArray responsesJson = sm.jsonFactory.createJsonArray();
		
		for ( int i = 0; i < m_responses.size(); i++ )
		{
			bhTransactionResponse ithResponse = m_responses.get(i);
			
			responsesJson.addObject(ithResponse.writeJson());
		}
		
		sm.jsonFactory.getHelper().putJsonArray(json, bhE_JsonKey.responseList, responsesJson);
	}
}
