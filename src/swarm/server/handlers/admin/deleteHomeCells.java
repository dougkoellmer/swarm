package swarm.server.handlers.admin;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.E_Role;
import swarm.server.account.UserSession;
import swarm.server.blobxn.BlobTransaction_DeactivateCell;
import swarm.server.blobxn.BlobTransaction_DeactivateUserCells;
import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.E_BlobTransactionType;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.E_GridType;
import swarm.server.entities.BaseServerGrid;
import swarm.server.entities.ServerUser;
import swarm.server.session.SessionManager;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCellAddressMapping;
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

public class deleteHomeCells extends A_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(deleteHomeCells.class.getName());
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		I_BlobManager blobManager = m_serverContext.blobMngrFactory.create(E_BlobCacheLevel.values());
		UserSession session = m_serverContext.sessionMngr.getSession(request, response);
		
		ServerUser user = null;
		try
		{
			user = blobManager.getBlob(session, ServerUser.class);
		}
		catch(BlobException e)
		{
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			s_logger.log(Level.SEVERE, "Could not get user for refresh home cells.", e);
			
			return;
		}
		
		if( user == null )
		{
			s_logger.log(Level.WARNING, "User is null.");
			return;
		}
		
		Iterator<ServerCellAddressMapping> iterator = user.getOwnedCells();
		while( iterator.hasNext() )
		{
			ServerCellAddressMapping mapping = iterator.next();
			
			if( mapping.getGridType() != E_GridType.ACTIVE )
			{
				return;
			}
			
			BlobTransaction_DeactivateCell deactivateCellTransaction = new BlobTransaction_DeactivateCell(mapping);
			try
			{
				deactivateCellTransaction.perform(m_serverContext.blobMngrFactory, E_BlobTransactionType.MULTI_BLOB_TYPE, 1);
			}
			catch (BlobException e)
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
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			s_logger.log(Level.SEVERE, "", e);
		}
	}
}
