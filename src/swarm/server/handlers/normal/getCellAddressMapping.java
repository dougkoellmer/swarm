package swarm.server.handlers.normal;


import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.structs.smServerCellAddress;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.transaction.smA_DefaultRequestHandler;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.structs.smE_CellAddressParseError;
import swarm.shared.structs.smE_GetCellAddressMappingError;
import swarm.shared.structs.smGetCellAddressMappingResult;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class getCellAddressMapping extends smA_DefaultRequestHandler
{
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		smServerCellAddress address = new smServerCellAddress();
		address.readJson(null, request.getJsonArgs());
		smE_CellAddressParseError parseError = address.getParseError();
		smGetCellAddressMappingResult result = new smGetCellAddressMappingResult();
		
		if( parseError == smE_CellAddressParseError.NO_ERROR )
		{
			smI_BlobManager blobManager = m_context.blobMngrFactory.create(smE_BlobCacheLevel.values());
			
			smServerCellAddressMapping addressMapping = null;
			
			try
			{
				addressMapping = blobManager.getBlob(address, smServerCellAddressMapping.class);
						
				if( addressMapping != null )
				{
					result.setMapping(addressMapping);
				}
				else
				{
					result.setError(smE_GetCellAddressMappingError.NOT_FOUND);
				}
			}
			catch(smBlobException e)
			{
				response.setError(smE_ResponseError.SERVICE_EXCEPTION);
				
				return;
			}
		}
		else
		{
			result.setError(smE_GetCellAddressMappingError.ADDRESS_PARSE_ERROR);
		}
		
		result.writeJson(null, response.getJsonArgs());
	}
}
