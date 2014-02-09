package swarm.server.handlers.normal;


import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.structs.E_CellAddressParseError;
import swarm.shared.structs.E_GetCellAddressMappingError;
import swarm.shared.structs.GetCellAddressMappingResult;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class getCellAddressMapping extends A_DefaultRequestHandler
{
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		ServerCellAddress address = new ServerCellAddress(m_serverContext.jsonFactory, request.getJsonArgs());
		E_CellAddressParseError parseError = address.getParseError();
		GetCellAddressMappingResult result = new GetCellAddressMappingResult();
		
		if( parseError == E_CellAddressParseError.NO_ERROR )
		{
			I_BlobManager blobManager = m_serverContext.blobMngrFactory.create(E_BlobCacheLevel.values());
			
			ServerCellAddressMapping addressMapping = null;
			
			try
			{
				addressMapping = blobManager.getBlob(address, ServerCellAddressMapping.class);
						
				if( addressMapping != null )
				{
					result.setMapping(addressMapping);
				}
				else
				{
					result.setError(E_GetCellAddressMappingError.NOT_FOUND);
				}
			}
			catch(BlobException e)
			{
				response.setError(E_ResponseError.SERVICE_EXCEPTION);
				
				return;
			}
		}
		else
		{
			result.setError(E_GetCellAddressMappingError.ADDRESS_PARSE_ERROR);
		}
		
		result.writeJson(response.getJsonArgs(), m_serverContext.jsonFactory);
	}
}
