package swarm.server.transaction;

public interface I_TransactionScopeListener
{
	void onEnterScope();
	
	void onBatchStart();
	
	void onBatchEnd();
	
	void onExitScope();
}
