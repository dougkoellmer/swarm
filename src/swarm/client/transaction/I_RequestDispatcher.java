package swarm.client.transaction;

import swarm.shared.json.JsonQuery;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.TransactionRequest;

public interface I_RequestDispatcher
{
	void initialize(I_ResponseCallbacks callbacks, int maxGetUrlLength);
	
	boolean dispatch(TransactionRequest request);
}
