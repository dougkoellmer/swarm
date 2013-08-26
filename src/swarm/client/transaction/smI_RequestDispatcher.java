package swarm.client.transaction;

import swarm.shared.json.smJsonQuery;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smTransactionRequest;

public interface smI_RequestDispatcher
{
	void initialize(smI_ResponseCallbacks callbacks, int maxGetUrlLength);
	
	boolean dispatch(smTransactionRequest request);
}
