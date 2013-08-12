package b33hive.server.handlers.admin;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import b33hive.server.account.bhE_Role;
import b33hive.server.account.bhUserSession;
import b33hive.server.account.bh_s;
import b33hive.server.blobxn.bhBlobTransaction_CreateUser;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhE_BlobTransactionType;
import b33hive.server.data.blob.bhI_Blob;
import b33hive.server.data.blob.bhI_BlobKey;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.entities.bhE_GridType;
import b33hive.server.entities.bhServerGrid;
import b33hive.server.entities.bhServerUser;
import b33hive.server.handlers.bhU_Handler;
import b33hive.server.session.bhSessionManager;
import b33hive.server.structs.bhServerCellAddress;
import b33hive.server.structs.bhServerCodePrivileges;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhServerTransactionManager;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.entities.bhA_User;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhE_NetworkPrivilege;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class createGrid implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(createGrid.class.getName());
	
	private final Class<? extends bhI_HomeCellCreator> m_T_homeCellCreator;
	
	// TEMPORARY
	private final Class<? extends bhServerGrid> m_T_grid;

	public createGrid(Class<? extends bhI_HomeCellCreator> T_homeCellCreator, Class<? extends bhServerGrid> T_grid)
	{
		m_T_homeCellCreator = T_homeCellCreator;
		m_T_grid = T_grid;
	}
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{		
		bhUserSession session = bh_s.sessionMngr.getSession(request, response);
		
		bhI_BlobManager blobManager = bh_s.blobMngrFactory.create(bhE_BlobCacheLevel.values());
		
		bhServerGrid activeGrid = null;
		
		try
		{
			activeGrid = blobManager.getBlob(bhE_GridType.ACTIVE, bhServerGrid.class);
		}
		catch( bhBlobException e)
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.severe("Could not see if grid was already created because of exception: " + e);
			
			return;
		}
		
		if( activeGrid != null )
		{
			response.setError(bhE_ResponseError.BAD_STATE);
			
			s_logger.severe("Grid is already made!");
			
			return;
		}
		
		try
		{
			HashMap<bhI_BlobKey, bhI_Blob> grids = new HashMap<bhI_BlobKey, bhI_Blob>();
			activeGrid = bhU_Handler.newObjectInstance(m_T_grid, response);
			
			blobManager.putBlob(bhE_GridType.ACTIVE, activeGrid);
		}
		catch(bhBlobException e)
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.severe("Could not create grid due to exception: " + e);
			
			return;
		}
		
		/*bhServerUser user = null;
		
		try
		{
			user = blobManager.getBlob(session, bhServerUser.class);
		}
		catch(bhBlobException e)
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.log(Level.SEVERE, "Could not create home cells because of blob exception when checking if user exists: ", e);
			
			return;
		}
		
		if( user != null )
		{
			response.setError(bhE_ResponseError.BAD_STATE);
			
			s_logger.severe("User must be null for home cell creation.");
			
			return;
		}
		
		//--- DRK > Create the user.
		bhBlobTransaction_CreateUser createUserTransaction = new bhBlobTransaction_CreateUser(session, false);
		try
		{
			createUserTransaction.perform(bhE_BlobTransactionType.SINGLE_BLOB_TYPE, 1);
		}
		catch(bhBlobException e)
		{
			s_logger.log(Level.SEVERE, "Could not create user. ", e);
		}
		
		user = createUserTransaction.getUser();
		
		/*if( user.getCellCount() > 1 || !user.isCellOwner(new bhGridCoordinate(0, 0)) )
		{
			response.setError(bhE_ResponseError.NOT_AUTHORIZED);
			
			s_logger.severe("User has too many cells already, or doesn't own origin cell!");
			
			return;
		}

		bhI_HomeCellCreator homeCellCreator = bhU_Handler.newObjectInstance(m_T_homeCellCreator, response);
		
		if( homeCellCreator == null )  return;
		
		homeCellCreator.initialize((ServletContext)context.getNativeContext());
		homeCellCreator.run(request, response, context, session, user);*/
	}
}
