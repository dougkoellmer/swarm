package swarm.client.transaction;

import swarm.shared.json.I_JsonArray;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public interface I_ResponseCallbacks
{
	void onResponseReceived(TransactionRequestBatch requestBatch, I_JsonArray jsonResponseBatch);
	
	void onResponseReceived(TransactionRequest request, TransactionResponse response);
	
	void onError(TransactionRequest request, TransactionResponse response);
}
