package swarm.server.handlers.normal;


import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.E_GridType;
import swarm.server.entities.ServerCell;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.structs.E_GetCellAddressError;
import swarm.shared.structs.GetCellAddressResult;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class getCellAddress extends A_DefaultRequestHandler
{
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		ServerCellAddressMapping mapping = new ServerCellAddressMapping(E_GridType.ACTIVE);
		mapping.readJson(request.getJsonArgs(), m_serverContext.jsonFactory);
		GetCellAddressResult result = new GetCellAddressResult();
		
		I_BlobManager blobManager = m_serverContext.blobMngrFactory.create(E_BlobCacheLevel.values());
		
		try
		{
			ServerCell persistedCell = null;
			
			persistedCell = blobManager.getBlob(mapping, ServerCell.class);
			if( persistedCell != null )
			{
				result.setAddress(persistedCell.getPrimaryAddress());
			}
			else
			{
				result.setError(E_GetCellAddressError.NOT_FOUND);
			}
		}
		catch(BlobException e)
		{
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			
			return;
		}
		
		result.writeJson(response.getJsonArgs(), m_serverContext.jsonFactory);
	}
}
