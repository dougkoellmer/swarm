package b33hive.client.transaction;

import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public interface bhI_ResponseCallbacks
{
	void onResponseReceived(bhTransactionRequestBatch requestBatch, bhI_JsonArray jsonResponseBatch);
	
	void onResponseReceived(bhTransactionRequest request, bhTransactionResponse response);
	
	void onError(bhTransactionRequest request, bhTransactionResponse response);
}
