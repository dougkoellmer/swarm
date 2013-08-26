package swarm.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.smE_Role;
import swarm.server.account.smUserSession;
import swarm.server.account.sm_s;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smServerCell;
import swarm.server.entities.smServerUser;
import swarm.server.handlers.smU_Handler;
import swarm.server.session.smSessionManager;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.structs.smE_GetCellAddressError;
import swarm.shared.structs.smGetCellAddressResult;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class refreshHomeCells implements smI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(refreshHomeCells.class.getName());
	
	private final Class<? extends smI_HomeCellCreator> m_T_homeCellCreator;
	
	public refreshHomeCells(Class<? extends smI_HomeCellCreator> T_homeCellCreator)
	{
		m_T_homeCellCreator = T_homeCellCreator;
	}
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		smI_BlobManager blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.values());
		bhUserSession session = sm_s.sessionMngr.getSession(request, response);
		
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
		
		smI_HomeCellCreator homeCellCreator = bhU_Handler.newObjectInstance(m_T_homeCellCreator, response);
		
		if( homeCellCreator == null )  return;
		
		homeCellCreator.initialize((ServletContext)context.getNativeContext());
		homeCellCreator.run(request, response, context, session, user);
	}
}
