package b33hive.client.transaction;

import b33hive.shared.json.bhJsonQuery;
import b33hive.shared.transaction.bhI_RequestPath;
import b33hive.shared.transaction.bhTransactionRequest;

public interface bhI_SyncRequestDispatcher extends bhI_RequestDispatcher
{
	void flushResponses();
}
