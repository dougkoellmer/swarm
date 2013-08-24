package swarm.client.transaction;

import swarm.shared.json.bhI_JsonArray;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public interface bhI_ResponseCallbacks
{
	void onResponseReceived(bhTransactionRequestBatch requestBatch, bhI_JsonArray jsonResponseBatch);
	
	void onResponseReceived(bhTransactionRequest request, bhTransactionResponse response);
	
	void onError(bhTransactionRequest request, bhTransactionResponse response);
}
