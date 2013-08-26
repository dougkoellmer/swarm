package swarm.server.transaction;

public interface smI_TransactionScopeListener
{
	void onEnterScope();
	
	void onBatchStart();
	
	void onBatchEnd();
	
	void onExitScope();
}
