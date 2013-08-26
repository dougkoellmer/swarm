package swarm.server.transaction;

import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public interface smI_RequestHandler
{
	void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response);
}