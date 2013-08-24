package swarm.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.bhE_Role;
import swarm.server.account.bhUserSession;
import swarm.server.blobxn.bhBlobTransaction_SetCellAddress;
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
import swarm.shared.app.sm;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.structs.bhE_GetCellAddressError;
import swarm.shared.structs.bhGetCellAddressResult;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class renameCell implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(renameCell.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		/*String oldRawAddress = sm.jsonFactory.getHelper().getString(request.getJson(), bhE_JsonKey.oldCellAddress);
		String newRawAddress = sm.jsonFactory.getHelper().getString(request.getJson(), bhE_JsonKey.newCellAddress);
		
		bhServerCellAddress oldAddress = new bhServerCellAddress(oldRawAddress);
		bhServerCellAddress newAddress = new bhServerCellAddress(newRawAddress);
		
		bhBlobTransaction_SetCellAddress transaction = new bhBlobTransaction_SetCellAddress(oldAddress, newAddress);
		
		try {
			transaction.perform(bhE_BlobTransactionType.MULTI_BLOB_TYPE, 1);
		} catch (bhBlobException e)
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.log(Level.SEVERE, "Could not rename cell because of exception.", e);
		}*/
	}
}
