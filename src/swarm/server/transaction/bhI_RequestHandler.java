package swarm.server.transaction;

import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public interface bhI_RequestHandler
{
	void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response);
}