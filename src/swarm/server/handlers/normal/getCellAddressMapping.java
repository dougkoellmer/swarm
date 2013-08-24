package swarm.server.handlers.normal;

import swarm.server.account.sm_s;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.structs.bhServerCellAddress;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.structs.bhE_CellAddressParseError;
import swarm.shared.structs.bhE_GetCellAddressMappingError;
import swarm.shared.structs.bhGetCellAddressMappingResult;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

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
			bhI_BlobManager blobManager = sm_s.blobMngrFactory.create(bhE_BlobCacheLevel.values());
			
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
