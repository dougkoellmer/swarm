package swarm.client.transaction;

import swarm.shared.json.JsonQuery;
import swarm.shared.transaction.I_RequestPath;
import swarm.shared.transaction.TransactionRequest;

public interface I_AsyncRequestDispatcher extends I_RequestDispatcher
{
	TransactionRequest getDispatchedRequest(I_RequestPath path, JsonQuery jsonQuery, TransactionRequest exclusion_nullable);
	
	void cancelRequestsByPath(I_RequestPath path, TransactionRequest exclusion_nullable);
}
