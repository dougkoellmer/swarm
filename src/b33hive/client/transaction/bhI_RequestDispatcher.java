package b33hive.client.transaction;

import b33hive.shared.json.bhJsonQuery;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhTransactionRequest;

public interface bhI_RequestDispatcher
{
	void initialize(bhI_ResponseCallbacks callbacks, int maxGetUrlLength);
	
	boolean dispatch(bhTransactionRequest request);
}
