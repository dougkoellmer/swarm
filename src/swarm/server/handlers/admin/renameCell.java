package swarm.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.smE_Role;
import swarm.server.account.smUserSession;
import swarm.server.blobxn.smBlobTransaction_SetCellAddress;
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
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.app.sm;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smJsonHelper;
import swarm.shared.structs.smE_GetCellAddressError;
import swarm.shared.structs.smGetCellAddressResult;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class renameCell implements smI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(renameCell.class.getName());
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		/*String oldRawAddress = sm.jsonFactory.getHelper().getString(request.getJson(), smE_JsonKey.oldCellAddress);
		String newRawAddress = sm.jsonFactory.getHelper().getString(request.getJson(), smE_JsonKey.newCellAddress);
		
		smServerCellAddress oldAddress = new smServerCellAddress(oldRawAddress);
		smServerCellAddress newAddress = new smServerCellAddress(newRawAddress);
		
		bhBlobTransaction_SetCellAddress transaction = new smBlobTransaction_SetCellAddress(oldAddress, newAddress);
		
		try {
			transaction.perform(smE_BlobTransactionType.MULTI_BLOB_TYPE, 1);
		} catch (smBlobException e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.log(Level.SEVERE, "Could not rename cell because of exception.", e);
		}*/
	}
}
