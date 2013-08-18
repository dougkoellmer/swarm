package b33hive.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import b33hive.server.account.bhE_Role;
import b33hive.server.account.bhUserSession;
import b33hive.server.blobxn.bhBlobTransaction_SetCellAddress;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhE_BlobTransactionType;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.entities.bhServerCell;
import b33hive.server.entities.bhServerUser;
import b33hive.server.session.bhSessionManager;
import b33hive.server.structs.bhServerCellAddress;
import b33hive.server.structs.bhServerCellAddressMapping;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.app.bh;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.structs.bhE_GetCellAddressError;
import b33hive.shared.structs.bhGetCellAddressResult;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class renameCell implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(renameCell.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		/*String oldRawAddress = bh.jsonFactory.getHelper().getString(request.getJson(), bhE_JsonKey.oldCellAddress);
		String newRawAddress = bh.jsonFactory.getHelper().getString(request.getJson(), bhE_JsonKey.newCellAddress);
		
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
