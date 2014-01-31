package swarm.server.handlers.admin;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.E_Role;
import swarm.server.account.UserSession;

import swarm.server.blobxn.BlobTransaction_CreateUser;
import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.E_BlobTransactionType;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.E_GridType;
import swarm.server.entities.BaseServerGrid;
import swarm.server.entities.ServerUser;
import swarm.server.handlers.U_Handler;
import swarm.server.session.SessionManager;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCodePrivileges;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.ServerTransactionManager;
import swarm.server.transaction.TransactionContext;
import swarm.shared.entities.A_User;
import swarm.shared.entities.E_CodeType;
import swarm.shared.structs.Code;
import swarm.shared.structs.E_NetworkPrivilege;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class createGrid extends A_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(createGrid.class.getName());
	
	// TEMPORARY
	private final Class<? extends BaseServerGrid> m_T_grid;

	public createGrid(Class<? extends BaseServerGrid> T_grid)
	{
		m_T_grid = T_grid;
	}
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{		
		UserSession session = m_serverContext.sessionMngr.getSession(request, response);
		
		I_BlobManager blobManager = m_serverContext.blobMngrFactory.create(E_BlobCacheLevel.values());
		
		BaseServerGrid activeGrid = null;
		
		try
		{
			activeGrid = blobManager.getBlob(E_GridType.ACTIVE, BaseServerGrid.class);
		}
		catch( BlobException e)
		{
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.severe("Could not see if grid was already created because of exception: " + e);
			
			return;
		}
		
		if( activeGrid != null )
		{
			response.setError(E_ResponseError.BAD_STATE);
			
			s_logger.severe("Grid is already made!");
			
			return;
		}
		
		try
		{
			HashMap<I_BlobKey, I_Blob> grids = new HashMap<I_BlobKey, I_Blob>();
			activeGrid = U_Handler.newObjectInstance(m_T_grid, response);
			
			blobManager.putBlob(E_GridType.ACTIVE, activeGrid);
		}
		catch(BlobException e)
		{
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.severe("Could not create grid due to exception: " + e);
			
			return;
		}
		
		/*smServerUser user = null;
		
		try
		{
			user = blobManager.getBlob(session, smServerUser.class);
		}
		catch(smBlobException e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.log(Level.SEVERE, "Could not create home cells because of blob exception when checking if user exists: ", e);
			
			return;
		}
		
		if( user != null )
		{
			response.setError(smE_ResponseError.BAD_STATE);
			
			s_logger.severe("User must be null for home cell creation.");
			
			return;
		}
		
		//--- DRK > Create the user.
		smBlobTransaction_CreateUser createUserTransaction = new smBlobTransaction_CreateUser(session, false);
		try
		{
			createUserTransaction.perform(smE_BlobTransactionType.SINGLE_BLOB_TYPE, 1);
		}
		catch(smBlobException e)
		{
			s_logger.log(Level.SEVERE, "Could not create user. ", e);
		}
		
		user = createUserTransaction.getUser();
		
		/*if( user.getCellCount() > 1 || !user.isCellOwner(new smGridCoordinate(0, 0)) )
		{
			response.setError(smE_ResponseError.NOT_AUTHORIZED);
			
			s_logger.severe("User has too many cells already, or doesn't own origin cell!");
			
			return;
		}

		smI_HomeCellCreator homeCellCreator = smU_Handler.newObjectInstance(m_T_homeCellCreator, response);
		
		if( homeCellCreator == null )  return;
		
		homeCellCreator.initialize((ServletContext)context.getNativeContext());
		homeCellCreator.run(request, response, context, session, user);*/
	}
}
