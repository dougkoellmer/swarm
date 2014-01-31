package swarm.client.transaction;

import swarm.shared.json.JsonQuery;
import swarm.shared.transaction.I_RequestPath;
import swarm.shared.transaction.TransactionRequest;

public interface I_SyncRequestDispatcher extends I_RequestDispatcher
{
	void flushResponses();
}
