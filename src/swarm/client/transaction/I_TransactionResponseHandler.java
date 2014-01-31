package swarm.client.transaction;

import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

/**
 * ...
 * @author 
 */
public interface I_TransactionResponseHandler 
{
	E_ResponseSuccessControl onResponseSuccess(TransactionRequest request, TransactionResponse response);
	
	E_ResponseErrorControl onResponseError(TransactionRequest request, TransactionResponse response);
}