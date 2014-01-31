package swarm.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.E_Role;
import swarm.server.account.UserSession;
import swarm.server.blobxn.BlobTransaction_SetCellAddress;
import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.E_BlobTransactionType;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.ServerCell;
import swarm.server.entities.ServerUser;
import swarm.server.session.SessionManager;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.app.BaseAppContext;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.JsonHelper;
import swarm.shared.structs.E_GetCellAddressError;
import swarm.shared.structs.GetCellAddressResult;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class renameCell implements I_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(renameCell.class.getName());
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		/*String oldRawAddress = sm.jsonFactory.getHelper().getString(request.getJson(), smE_JsonKey.oldCellAddress);
		String newRawAddress = sm.jsonFactory.getHelper().getString(request.getJson(), smE_JsonKey.newCellAddress);
		
		smServerCellAddress oldAddress = new smServerCellAddress(oldRawAddress);
		smServerCellAddress newAddress = new smServerCellAddress(newRawAddress);
		
		smBlobTransaction_SetCellAddress transaction = new smBlobTransaction_SetCellAddress(oldAddress, newAddress);
		
		try {
			transaction.perform(smE_BlobTransactionType.MULTI_BLOB_TYPE, 1);
		} catch (smBlobException e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.log(Level.SEVERE, "Could not rename cell because of exception.", e);
		}*/
	}
}
