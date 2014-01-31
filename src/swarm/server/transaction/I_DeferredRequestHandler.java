package swarm.server.transaction;


public interface I_DeferredRequestHandler
{
	void handleDeferredRequests(TransactionContext context, TransactionBatch batch);
}
