package swarm.server.transaction;

import java.util.ArrayList;

import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;


class smTransactionResponseBatch extends smTransactionResponse
{
	private final ArrayList<smTransactionResponse> m_responses = new ArrayList<smTransactionResponse>();
	
	smTransactionResponseBatch(smA_JsonFactory jsonFactory)
	{
		super(jsonFactory);
	}
	
	public void addResponse(smTransactionResponse response)
	{
		m_responses.add(response);
	}
	
	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		super.writeJson(factory, json_out);
		
		final smI_JsonArray responsesJson = factory.createJsonArray();
		
		for ( int i = 0; i < m_responses.size(); i++ )
		{
			smTransactionResponse ithResponse = m_responses.get(i);
			
			smI_JsonObject jsonObject = factory.createJsonObject();
			ithResponse.writeJson(factory, jsonObject);
			responsesJson.addObject(jsonObject);
		}
		
		factory.getHelper().putJsonArray(json_out, smE_JsonKey.responseList, responsesJson);
	}
}
