package swarm.client.transaction;

import swarm.shared.json.bhJsonQuery;
import swarm.shared.transaction.bhI_RequestPath;
import swarm.shared.transaction.bhTransactionRequest;

public interface bhI_AsyncRequestDispatcher extends bhI_RequestDispatcher
{
	bhTransactionRequest getDispatchedRequest(bhI_RequestPath path, bhJsonQuery jsonQuery, bhTransactionRequest exclusion_nullable);
	
	void cancelRequestsByPath(bhI_RequestPath path, bhTransactionRequest exclusion_nullable);
}
