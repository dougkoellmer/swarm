package swarm.client.transaction;

import swarm.shared.json.smI_JsonArray;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public interface smI_ResponseCallbacks
{
	void onResponseReceived(smTransactionRequestBatch requestBatch, smI_JsonArray jsonResponseBatch);
	
	void onResponseReceived(smTransactionRequest request, smTransactionResponse response);
	
	void onError(smTransactionRequest request, smTransactionResponse response);
}
