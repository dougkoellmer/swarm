package swarm.client.transaction;

import swarm.shared.json.bhJsonQuery;
import swarm.shared.transaction.bhI_RequestPath;
import swarm.shared.transaction.bhTransactionRequest;

public interface bhI_SyncRequestDispatcher extends bhI_RequestDispatcher
{
	void flushResponses();
}
