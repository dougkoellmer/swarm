package b33hive.server.handlers.normal;

import b33hive.server.account.bh_s;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.entities.bhE_GridType;
import b33hive.server.entities.bhServerCell;
import b33hive.server.structs.bhServerCellAddressMapping;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.structs.bhE_GetCellAddressError;
import b33hive.shared.structs.bhGetCellAddressResult;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class getCellAddress implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhServerCellAddressMapping mapping = new bhServerCellAddressMapping(bhE_GridType.ACTIVE);
		mapping.readJson(request.getJson());
		bhGetCellAddressResult result = new bhGetCellAddressResult();
		
		bhI_BlobManager blobManager = bh_s.blobMngrFactory.create(bhE_BlobCacheLevel.values());
		
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
