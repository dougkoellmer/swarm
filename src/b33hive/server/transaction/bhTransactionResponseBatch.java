package com.b33hive.server.transaction;

import java.util.ArrayList;

import com.b33hive.shared.json.bhA_JsonFactory;
import com.b33hive.shared.json.bhE_JsonKey;
import com.b33hive.shared.json.bhI_JsonArray;
import com.b33hive.shared.json.bhI_JsonObject;
import com.b33hive.shared.json.bhJsonHelper;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

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
		
		final bhI_JsonArray responsesJson = bhA_JsonFactory.getInstance().createJsonArray();
		
		for ( int i = 0; i < m_responses.size(); i++ )
		{
			bhTransactionResponse ithResponse = m_responses.get(i);
			
			responsesJson.addObject(ithResponse.writeJson());
		}
		
		bhJsonHelper.getInstance().putJsonArray(json, bhE_JsonKey.responseList, responsesJson);
	}
}
