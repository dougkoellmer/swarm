package b33hive.client.transaction;

import b33hive.shared.json.bhJsonQuery;
import b33hive.shared.transaction.bhI_RequestPath;
import b33hive.shared.transaction.bhTransactionRequest;

public interface bhI_AsynchronousRequestDispatcher extends bhI_RequestDispatcher
{
	bhTransactionRequest getDispatchedRequest(bhI_RequestPath path, bhJsonQuery jsonQuery, bhTransactionRequest exclusion_nullable);
	
	void cancelRequestsByPath(bhI_RequestPath path, bhTransactionRequest exclusion_nullable);
}
