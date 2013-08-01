package com.b33hive.server.transaction;

public interface bhI_TransactionScopeListener
{
	void onEnterScope();
	
	void onBatchStart();
	
	void onBatchEnd();
	
	void onExitScope();
}
