package swarm.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.E_Role;
import swarm.server.account.UserSession;
import swarm.server.blobxn.BlobTransaction_DeactivateUserCells;
import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.E_BlobTransactionType;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.BaseServerGrid;
import swarm.server.entities.ServerUser;
import swarm.server.session.SessionManager;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCodePrivileges;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.ServerTransactionManager;
import swarm.server.transaction.TransactionContext;
import swarm.shared.app.BaseAppContext;
import swarm.shared.entities.A_User;
import swarm.shared.entities.E_CodeType;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.JsonHelper;
import swarm.shared.structs.Code;
import swarm.shared.structs.E_NetworkPrivilege;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class deactivateUserCells extends A_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(deactivateUserCells.class.getName());
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		Integer accountId = m_serverContext.jsonFactory.getHelper().getInt(request.getJsonArgs(), E_JsonKey.accountId);
		
		if( accountId == null )
		{
			response.setError(E_ResponseError.BAD_INPUT);
			
			return;
		}
		
		UserSession dummySession = new UserSession(accountId, "", E_Role.USER);
		
		BlobTransaction_DeactivateUserCells deactivateCells = new BlobTransaction_DeactivateUserCells(dummySession);
		
		try
		{
			deactivateCells.perform(m_serverContext.blobMngrFactory, E_BlobTransactionType.MULTI_BLOB_TYPE, 1);
		}
		catch(BlobException e)
		{
			s_logger.log(Level.SEVERE, "Could not deactivate user cells.", e);
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			
			return;
		}
	}
}
