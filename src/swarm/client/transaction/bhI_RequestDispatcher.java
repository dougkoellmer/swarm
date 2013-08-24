package swarm.client.transaction;

import swarm.shared.json.bhJsonQuery;
import swarm.shared.transaction.bhE_RequestPath;
import swarm.shared.transaction.bhTransactionRequest;

public interface bhI_RequestDispatcher
{
	void initialize(bhI_ResponseCallbacks callbacks, int maxGetUrlLength);
	
	boolean dispatch(bhTransactionRequest request);
}
