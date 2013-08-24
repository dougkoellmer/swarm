package swarm.server.handlers.normal;

import java.util.logging.Logger;


import swarm.server.account.sm_s;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhE_BlobTransactionType;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.entities.bhE_GridType;
import swarm.server.entities.bhServerGrid;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class getGridData implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(getGridData.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhI_BlobManager blobManager = sm_s.blobMngrFactory.create(bhE_BlobCacheLevel.values());
		
		bhServerGrid grid = null;
		
		try
		{
			grid = blobManager.getBlob(bhE_GridType.ACTIVE, bhServerGrid.class);
		}
		catch( bhBlobException e)
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.severe("Could not retrieve grid data due to exception: " + e);
			
			return;
		}
		
		//--- DRK > Should really never be null by this point for practical cases.
		if( grid == null )
		{
			grid = new bhServerGrid();
			
			s_logger.severe("Grid came up null when it probably should have been initialized.");
		}
		
		grid.writeJson(response.getJson());
	}
}
