package b33hive.server.transaction;

import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public interface bhI_RequestHandler
{
	void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response);
}