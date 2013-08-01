package com.b33hive.server.handlers;

import com.b33hive.server.data.blob.bhBlobException;
import com.b33hive.server.data.blob.bhBlobManagerFactory;
import com.b33hive.server.data.blob.bhE_BlobCacheLevel;
import com.b33hive.server.data.blob.bhI_BlobManager;
import com.b33hive.server.entities.bhE_GridType;
import com.b33hive.server.entities.bhServerCell;
import com.b33hive.server.structs.bhServerCellAddressMapping;
import com.b33hive.server.transaction.bhI_RequestHandler;
import com.b33hive.server.transaction.bhTransactionContext;
import com.b33hive.shared.structs.bhE_GetCellAddressError;
import com.b33hive.shared.structs.bhGetCellAddressResult;
import com.b33hive.shared.transaction.bhE_ResponseError;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

public class getCellAddress implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhServerCellAddressMapping mapping = new bhServerCellAddressMapping(bhE_GridType.ACTIVE);
		mapping.readJson(request.getJson());
		bhGetCellAddressResult result = new bhGetCellAddressResult();
		
		bhI_BlobManager blobManager = bhBlobManagerFactory.getInstance().create(bhE_BlobCacheLevel.values());
		
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
