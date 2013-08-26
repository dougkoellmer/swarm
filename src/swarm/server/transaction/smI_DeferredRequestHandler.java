package swarm.server.transaction;


public interface smI_DeferredRequestHandler
{
	void handleDeferredRequests(smTransactionContext context, smTransactionBatch batch);
}
