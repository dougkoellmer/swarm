package swarm.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.bhE_Role;
import swarm.server.account.bhUserSession;
import swarm.server.blobxn.bhBlobTransaction_DeactivateUserCells;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhE_BlobTransactionType;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.entities.bhServerGrid;
import swarm.server.entities.bhServerUser;
import swarm.server.session.bhSessionManager;
import swarm.server.structs.bhServerCellAddress;
import swarm.server.structs.bhServerCodePrivileges;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhServerTransactionManager;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.app.sm;
import swarm.shared.entities.bhA_User;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.structs.bhCode;
import swarm.shared.structs.bhE_NetworkPrivilege;
import swarm.shared.structs.bhGridCoordinate;
import swarm.shared.transaction.bhE_RequestPath;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class deactivateUserCells implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(deactivateUserCells.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		Integer accountId = sm.jsonFactory.getHelper().getInt(request.getJson(), bhE_JsonKey.accountId);
		
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
