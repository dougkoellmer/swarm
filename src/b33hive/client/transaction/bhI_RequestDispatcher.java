package com.b33hive.client.transaction;

import com.b33hive.shared.json.bhJsonQuery;
import com.b33hive.shared.transaction.bhE_RequestPath;
import com.b33hive.shared.transaction.bhTransactionRequest;

public interface bhI_RequestDispatcher
{
	void initialize(bhClientTransactionManager manager);
	
	boolean dispatch(bhTransactionRequest request);
}
