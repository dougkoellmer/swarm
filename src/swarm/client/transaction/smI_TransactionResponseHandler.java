package swarm.client.transaction;

import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

/**
 * ...
 * @author 
 */
public interface smI_TransactionResponseHandler 
{
	smE_ResponseSuccessControl onResponseSuccess(smTransactionRequest request, smTransactionResponse response);
	
	smE_ResponseErrorControl onResponseError(smTransactionRequest request, smTransactionResponse response);
}