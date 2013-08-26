package swarm.server.transaction;

import java.util.ArrayList;

import swarm.shared.app.sm;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;


class smTransactionResponseBatch extends bhTransactionResponse
{
	private final ArrayList<smTransactionResponse> m_responses = new ArrayList<smTransactionResponse>();
	
	bhTransactionResponseBatch()
	{
		super();
	}
	
	public void addResponse(smTransactionResponse response)
	{
		m_responses.add(response);
	}
	
	@Override
	public void writeJson(smI_JsonObject json)
	{
		super.writeJson(json);
		
		final smI_JsonArray responsesJson = sm.jsonFactory.createJsonArray();
		
		for ( int i = 0; i < m_responses.size(); i++ )
		{
			bhTransactionResponse ithResponse = m_responses.get(i);
			
			responsesJson.addObject(ithResponse.writeJson());
		}
		
		sm.jsonFactory.getHelper().putJsonArray(json, smE_JsonKey.responseList, responsesJson);
	}
}
