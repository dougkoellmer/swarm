package swarm.client.transaction;

import swarm.shared.json.smJsonQuery;
import swarm.shared.transaction.smI_RequestPath;
import swarm.shared.transaction.smTransactionRequest;

public interface smI_AsyncRequestDispatcher extends smI_RequestDispatcher
{
	smTransactionRequest getDispatchedRequest(smI_RequestPath path, smJsonQuery jsonQuery, smTransactionRequest exclusion_nullable);
	
	void cancelRequestsByPath(smI_RequestPath path, smTransactionRequest exclusion_nullable);
}
