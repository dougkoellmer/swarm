package b33hive.client.transaction;

import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

/**
 * ...
 * @author 
 */
public interface bhI_TransactionResponseHandler 
{
	bhE_ResponseSuccessControl onResponseSuccess(bhTransactionRequest request, bhTransactionResponse response);
	
	bhE_ResponseErrorControl onResponseError(bhTransactionRequest request, bhTransactionResponse response);
}