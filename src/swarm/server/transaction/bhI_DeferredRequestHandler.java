package swarm.server.transaction;


public interface bhI_DeferredRequestHandler
{
	void handleDeferredRequests(bhTransactionContext context, bhTransactionBatch batch);
}
