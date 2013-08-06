package b33hive.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.b33hive.server.app.bhS_ServerApp;

import b33hive.server.account.bhE_Role;
import b33hive.server.account.bhUserSession;
import b33hive.server.blobxn.bhBlobTransaction_DeactivateUserCells;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhE_BlobTransactionType;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.entities.bhServerGrid;
import b33hive.server.entities.bhServerUser;
import b33hive.server.session.bhSessionManager;
import b33hive.server.structs.bhServerCellAddress;
import b33hive.server.structs.bhServerCodePrivileges;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhServerTransactionManager;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.app.bh;
import b33hive.shared.entities.bhA_User;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhE_NetworkPrivilege;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class deactivateUserCells implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(deactivateUserCells.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		Integer accountId = bh.jsonFactory.getHelper().getInt(request.getJson(), bhE_JsonKey.accountId);
		
		if( accountId == null )
		{
			response.setError(bhE_ResponseError.BAD_INPUT);
			
			return;
		}
		
		bhUserSession dummySession = new bhUserSession(accountId, "", bhE_Role.USER);
		
		bhBlobTransaction_DeactivateUserCells deactivateCells = new bhBlobTransaction_DeactivateUserCells(dummySession);
		
		try
		{
			deactivateCells.perform(bhE_BlobTransactionType.MULTI_BLOB_TYPE, 1);
		}
		catch(bhBlobException e)
		{
			s_logger.log(Level.SEVERE, "Could not deactivate user cells.", e);
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			return;
		}
	}
}
