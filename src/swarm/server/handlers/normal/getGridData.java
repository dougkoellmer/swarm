package swarm.server.handlers.normal;

import java.util.logging.Logger;


import swarm.server.account.sm_s;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smE_BlobTransactionType;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smE_GridType;
import swarm.server.entities.smServerGrid;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class getGridData implements smI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(getGridData.class.getName());
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		smI_BlobManager blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.values());
		
		smServerGrid grid = null;
		
		try
		{
			grid = blobManager.getBlob(smE_GridType.ACTIVE, smServerGrid.class);
		}
		catch( smBlobException e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.severe("Could not retrieve grid data due to exception: " + e);
			
			return;
		}
		
		//--- DRK > Should really never be null by this point for practical cases.
		if( grid == null )
		{
			grid = new smServerGrid();
			
			s_logger.severe("Grid came up null when it probably should have been initialized.");
		}
		
		grid.writeJson(response.getJson());
	}
}
