package swarm.server.handlers.admin;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.smE_Role;
import swarm.server.account.smUserSession;

import swarm.server.blobxn.smBlobTransaction_CreateUser;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smE_BlobTransactionType;
import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smI_BlobKey;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smE_GridType;
import swarm.server.entities.smServerGrid;
import swarm.server.entities.smServerUser;
import swarm.server.handlers.smU_Handler;
import swarm.server.session.smSessionManager;
import swarm.server.structs.smServerCellAddress;
import swarm.server.structs.smServerCodePrivileges;
import swarm.server.transaction.smA_DefaultRequestHandler;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smServerTransactionManager;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.entities.smA_User;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smE_NetworkPrivilege;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class createGrid extends smA_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(createGrid.class.getName());
	
	// TEMPORARY
	private final Class<? extends smServerGrid> m_T_grid;

	public createGrid(Class<? extends smServerGrid> T_grid)
	{
		m_T_grid = T_grid;
	}
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{		
		smUserSession session = m_context.sessionMngr.getSession(request, response);
		
		smI_BlobManager blobManager = m_context.blobMngrFactory.create(smE_BlobCacheLevel.values());
		
		smServerGrid activeGrid = null;
		
		try
		{
			activeGrid = blobManager.getBlob(smE_GridType.ACTIVE, smServerGrid.class);
		}
		catch( smBlobException e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.severe("Could not see if grid was already created because of exception: " + e);
			
			return;
		}
		
		if( activeGrid != null )
		{
			response.setError(smE_ResponseError.BAD_STATE);
			
			s_logger.severe("Grid is already made!");
			
			return;
		}
		
		try
		{
			HashMap<smI_BlobKey, smI_Blob> grids = new HashMap<smI_BlobKey, smI_Blob>();
			activeGrid = smU_Handler.newObjectInstance(m_T_grid, response);
			
			blobManager.putBlob(smE_GridType.ACTIVE, activeGrid);
		}
		catch(smBlobException e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			
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
