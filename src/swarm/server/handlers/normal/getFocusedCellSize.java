package swarm.server.handlers.normal;

import java.util.logging.Logger;






import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.E_BlobTransactionType;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.E_GridType;
import swarm.server.entities.BaseServerGrid;
import swarm.server.entities.ServerCell;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class getFocusedCellSize extends A_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(getFocusedCellSize.class.getName());
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		ServerCellAddressMapping mapping = new ServerCellAddressMapping(E_GridType.ACTIVE, request.getJsonArgs(), m_serverContext.jsonFactory);
		
		I_BlobManager blobManager = m_serverContext.blobMngrFactory.create(E_BlobCacheLevel.values());
		
		ServerCell persistedCell = null;
		
		try
		{
			persistedCell = blobManager.getBlob(mapping, ServerCell.class);
		}
		catch(BlobException e)
		{
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			
			return;
		}
		
		if( persistedCell != null )
		{
			persistedCell.getFocusedCellSize().writeJson(m_serverContext.jsonFactory, response.getJsonArgs());
		}
	}
}
