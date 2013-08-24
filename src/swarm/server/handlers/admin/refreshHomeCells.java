package swarm.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.bhE_Role;
import swarm.server.account.bhUserSession;
import swarm.server.account.sm_s;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.entities.bhServerCell;
import swarm.server.entities.bhServerUser;
import swarm.server.handlers.bhU_Handler;
import swarm.server.session.bhSessionManager;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.structs.bhE_GetCellAddressError;
import swarm.shared.structs.bhGetCellAddressResult;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class refreshHomeCells implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(refreshHomeCells.class.getName());
	
	private final Class<? extends bhI_HomeCellCreator> m_T_homeCellCreator;
	
	public refreshHomeCells(Class<? extends bhI_HomeCellCreator> T_homeCellCreator)
	{
		m_T_homeCellCreator = T_homeCellCreator;
	}
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhI_BlobManager blobManager = sm_s.blobMngrFactory.create(bhE_BlobCacheLevel.values());
		bhUserSession session = sm_s.sessionMngr.getSession(request, response);
		
		bhServerUser user = null;
		try
		{
			user = blobManager.getBlob(session, bhServerUser.class);
		}
		catch(bhBlobException e)
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.log(Level.SEVERE, "Could not get user for refresh home cells.", e);
			
			return;
		}
		
		bhI_HomeCellCreator homeCellCreator = bhU_Handler.newObjectInstance(m_T_homeCellCreator, response);
		
		if( homeCellCreator == null )  return;
		
		homeCellCreator.initialize((ServletContext)context.getNativeContext());
		homeCellCreator.run(request, response, context, session, user);
	}
}
