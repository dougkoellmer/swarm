package swarm.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.smE_Role;
import swarm.server.account.smUserSession;
import swarm.server.blobxn.smBlobTransaction_ClearCell;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smE_BlobTransactionType;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smServerCell;
import swarm.server.entities.smServerUser;
import swarm.server.session.smSessionManager;
import swarm.server.structs.smServerCellAddress;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.transaction.smA_DefaultRequestHandler;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.json.smJsonHelper;
import swarm.shared.structs.smE_GetCellAddressError;
import swarm.shared.structs.smGetCellAddressResult;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class clearCell extends smA_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(clearCell.class.getName());
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{		
		smServerCellAddress address = new smServerCellAddress(request.getJsonArgs());
		
		smBlobTransaction_ClearCell transaction = new smBlobTransaction_ClearCell(address);
		
		try
		{
			transaction.perform(m_context.blobMngrFactory, smE_BlobTransactionType.MULTI_BLOB_TYPE, 1);
		}
		catch (smBlobException e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.log(Level.SEVERE, "Could not clear cell because of exception.", e);
		}
	}
}
