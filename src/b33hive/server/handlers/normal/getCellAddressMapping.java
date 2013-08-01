package com.b33hive.server.handlers;

import com.b33hive.server.data.blob.bhBlobException;
import com.b33hive.server.data.blob.bhBlobManagerFactory;
import com.b33hive.server.data.blob.bhE_BlobCacheLevel;
import com.b33hive.server.data.blob.bhI_BlobManager;
import com.b33hive.server.structs.bhServerCellAddress;
import com.b33hive.server.structs.bhServerCellAddressMapping;
import com.b33hive.server.transaction.bhI_RequestHandler;
import com.b33hive.server.transaction.bhTransactionContext;
import com.b33hive.shared.structs.bhE_CellAddressParseError;
import com.b33hive.shared.structs.bhE_GetCellAddressMappingError;
import com.b33hive.shared.structs.bhGetCellAddressMappingResult;
import com.b33hive.shared.transaction.bhE_ResponseError;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

public class getCellAddressMapping implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhServerCellAddress address = new bhServerCellAddress();
		address.readJson(request.getJson());
		bhE_CellAddressParseError parseError = address.getParseError();
		bhGetCellAddressMappingResult result = new bhGetCellAddressMappingResult();
		
		if( parseError == bhE_CellAddressParseError.NO_ERROR )
		{
			bhI_BlobManager blobManager = bhBlobManagerFactory.getInstance().create(bhE_BlobCacheLevel.values());
			
			bhServerCellAddressMapping addressMapping = null;
			
			try
			{
				addressMapping = blobManager.getBlob(address, bhServerCellAddressMapping.class);
						
				if( addressMapping != null )
				{
					result.setMapping(addressMapping);
				}
				else
				{
					result.setError(bhE_GetCellAddressMappingError.NOT_FOUND);
				}
			}
			catch(bhBlobException e)
			{
				response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
				
				return;
			}
		}
		else
		{
			result.setError(bhE_GetCellAddressMappingError.ADDRESS_PARSE_ERROR);
		}
		
		result.writeJson(response.getJson());
	}
}
