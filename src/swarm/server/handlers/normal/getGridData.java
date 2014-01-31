package swarm.server.handlers.normal;

import java.util.logging.Logger;



import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.E_BlobTransactionType;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.E_GridType;
import swarm.server.entities.BaseServerGrid;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class getGridData extends A_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(getGridData.class.getName());
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		I_BlobManager blobManager = m_serverContext.blobMngrFactory.create(E_BlobCacheLevel.values());
		
		BaseServerGrid grid = null;
		
		try
		{
			grid = blobManager.getBlob(E_GridType.ACTIVE, BaseServerGrid.class);
		}
		catch( BlobException e)
		{
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.severe("Could not retrieve grid data due to exception: " + e);
			
			return;
		}
		
		//--- DRK > Should really never be null by this point for practical cases.
		if( grid == null )
		{
			grid = new BaseServerGrid();
			
			s_logger.severe("Grid came up null when it probably should have been initialized.");
		}
		
		grid.writeJson(m_serverContext.jsonFactory, response.getJsonArgs());
	}
}
