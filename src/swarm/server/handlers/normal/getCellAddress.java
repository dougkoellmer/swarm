package swarm.server.handlers.normal;

import swarm.server.account.sm_s;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.entities.bhE_GridType;
import swarm.server.entities.bhServerCell;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.structs.bhE_GetCellAddressError;
import swarm.shared.structs.bhGetCellAddressResult;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class getCellAddress implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhServerCellAddressMapping mapping = new bhServerCellAddressMapping(bhE_GridType.ACTIVE);
		mapping.readJson(request.getJson());
		bhGetCellAddressResult result = new bhGetCellAddressResult();
		
		bhI_BlobManager blobManager = sm_s.blobMngrFactory.create(bhE_BlobCacheLevel.values());
		
		try
		{
			bhServerCell persistedCell = null;
			
			persistedCell = blobManager.getBlob(mapping, bhServerCell.class);
			if( persistedCell != null )
			{
				result.setAddress(persistedCell.getAddress());
			}
			else
			{
				result.setError(bhE_GetCellAddressError.NOT_FOUND);
			}
		}
		catch(bhBlobException e)
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			return;
		}
		
		result.writeJson(response.getJson());
	}
}
