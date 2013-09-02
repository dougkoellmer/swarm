package swarm.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.smE_Role;
import swarm.server.account.smUserSession;
import swarm.server.blobxn.smBlobTransaction_DeactivateUserCells;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smE_BlobTransactionType;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smServerGrid;
import swarm.server.entities.smServerUser;
import swarm.server.session.smSessionManager;
import swarm.server.structs.smServerCellAddress;
import swarm.server.structs.smServerCodePrivileges;
import swarm.server.transaction.smA_DefaultRequestHandler;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smServerTransactionManager;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.entities.smA_User;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smJsonHelper;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smE_NetworkPrivilege;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class deactivateUserCells extends smA_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(deactivateUserCells.class.getName());
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		Integer accountId = m_context.jsonFactory.getHelper().getInt(request.getJsonArgs(), smE_JsonKey.accountId);
		
		if( accountId == null )
		{
			response.setError(smE_ResponseError.BAD_INPUT);
			
			return;
		}
		
		smUserSession dummySession = new smUserSession(accountId, "", smE_Role.USER);
		
		smBlobTransaction_DeactivateUserCells deactivateCells = new smBlobTransaction_DeactivateUserCells(dummySession);
		
		try
		{
			deactivateCells.perform(m_context.blobMngrFactory, smE_BlobTransactionType.MULTI_BLOB_TYPE, 1);
		}
		catch(smBlobException e)
		{
			s_logger.log(Level.SEVERE, "Could not deactivate user cells.", e);
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			
			return;
		}
	}
}
