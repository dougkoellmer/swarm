package swarm.server.handlers.normal;

import swarm.server.account.sm_s;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smE_GridType;
import swarm.server.entities.smServerCell;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.structs.smE_GetCellAddressError;
import swarm.shared.structs.smGetCellAddressResult;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class getCellAddress implements smI_RequestHandler
{
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		smServerCellAddressMapping mapping = new smServerCellAddressMapping(smE_GridType.ACTIVE);
		mapping.readJson(request.getJson());
		smGetCellAddressResult result = new smGetCellAddressResult();
		
		smI_BlobManager blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.values());
		
		try
		{
			smServerCell persistedCell = null;
			
			persistedCell = blobManager.getBlob(mapping, smServerCell.class);
			if( persistedCell != null )
			{
				result.setAddress(persistedCell.getAddress());
			}
			else
			{
				result.setError(smE_GetCellAddressError.NOT_FOUND);
			}
		}
		catch(smBlobException e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			
			return;
		}
		
		result.writeJson(response.getJson());
	}
}
