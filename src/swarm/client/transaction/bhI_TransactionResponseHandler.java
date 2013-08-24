package swarm.client.transaction;

import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

/**
 * ...
 * @author 
 */
public interface bhI_TransactionResponseHandler 
{
	bhE_ResponseSuccessControl onResponseSuccess(bhTransactionRequest request, bhTransactionResponse response);
	
	bhE_ResponseErrorControl onResponseError(bhTransactionRequest request, bhTransactionResponse response);
}