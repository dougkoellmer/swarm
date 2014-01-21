package swarm.server.handlers.admin;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.smE_Role;
import swarm.server.account.smUserSession;
import swarm.server.blobxn.smBlobTransaction_DeactivateCell;
import swarm.server.blobxn.smBlobTransaction_DeactivateUserCells;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smE_BlobTransactionType;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smE_GridType;
import swarm.server.entities.smServerGrid;
import swarm.server.entities.smServerUser;
import swarm.server.session.smSessionManager;
import swarm.server.structs.smServerCellAddress;
import swarm.server.structs.smServerCellAddressMapping;
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

public class recreateHomeCells extends smA_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(recreateHomeCells.class.getName());
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		smI_BlobManager blobManager = m_serverContext.blobMngrFactory.create(smE_BlobCacheLevel.values());
		smUserSession session = m_serverContext.sessionMngr.getSession(request, response);
		
		smServerUser user = null;
		try
		{
			user = blobManager.getBlob(session, smServerUser.class);
		}
		catch(smBlobException e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			s_logger.log(Level.SEVERE, "Could not get user for refresh home cells.", e);
			
			return;
		}
		
		if( user == null )
		{
			s_logger.log(Level.WARNING, "User is null.");
			return;
		}
		
		Iterator<smServerCellAddressMapping> iterator = user.getOwnedCells();
		while( iterator.hasNext() )
		{
			smServerCellAddressMapping mapping = iterator.next();
			
			if( mapping.getGridType() != smE_GridType.ACTIVE )
			{
				return;
			}
			
			smBlobTransaction_DeactivateCell deactivateCellTransaction = new smBlobTransaction_DeactivateCell(mapping);
			try
			{
				deactivateCellTransaction.perform(m_serverContext.blobMngrFactory, smE_BlobTransactionType.MULTI_BLOB_TYPE, 1);
			}
			catch (smBlobException e)
			{
				s_logger.log(Level.WARNING, e.getMessage());
			}
			
			iterator.remove();
		}
		
		try
		{
			blobManager.putBlob(session, user);
		}
		catch (Exception e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			s_logger.log(Level.SEVERE, "", e);
		}
	}
}
