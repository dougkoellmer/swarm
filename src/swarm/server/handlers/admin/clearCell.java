package swarm.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.bhE_Role;
import swarm.server.account.bhUserSession;
import swarm.server.blobxn.bhBlobTransaction_ClearCell;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhE_BlobTransactionType;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.entities.bhServerCell;
import swarm.server.entities.bhServerUser;
import swarm.server.session.bhSessionManager;
import swarm.server.structs.bhServerCellAddress;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.structs.bhE_GetCellAddressError;
import swarm.shared.structs.bhGetCellAddressResult;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class clearCell implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(clearCell.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{		
		bhServerCellAddress address = new bhServerCellAddress(request.getJson());
		
		bhBlobTransaction_ClearCell transaction = new bhBlobTransaction_ClearCell(address);
		
		try {
			transaction.perform(bhE_BlobTransactionType.MULTI_BLOB_TYPE, 1);
		} catch (bhBlobException e)
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.log(Level.SEVERE, "Could not clear cell because of exception.", e);
		}
	}
}
