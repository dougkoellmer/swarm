package b33hive.server.handlers.normal;

import java.util.logging.Logger;


import b33hive.server.account.bh_s;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhE_BlobTransactionType;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.entities.bhE_GridType;
import b33hive.server.entities.bhServerGrid;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class getGridData implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(getGridData.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhI_BlobManager blobManager = bh_s.blobMngrFactory.create(bhE_BlobCacheLevel.values());
		
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
