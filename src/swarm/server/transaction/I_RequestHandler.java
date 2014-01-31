package swarm.server.transaction;

import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public interface I_RequestHandler
{
	void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response);
}