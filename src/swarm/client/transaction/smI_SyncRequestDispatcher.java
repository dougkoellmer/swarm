package swarm.client.transaction;

import swarm.shared.json.smJsonQuery;
import swarm.shared.transaction.smI_RequestPath;
import swarm.shared.transaction.smTransactionRequest;

public interface smI_SyncRequestDispatcher extends smI_RequestDispatcher
{
	void flushResponses();
}
