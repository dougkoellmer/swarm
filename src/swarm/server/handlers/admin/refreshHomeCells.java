package swarm.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.E_Role;
import swarm.server.account.UserSession;

import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.ServerCell;
import swarm.server.entities.ServerUser;
import swarm.server.handlers.U_Handler;
import swarm.server.session.SessionManager;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.structs.E_GetCellAddressError;
import swarm.shared.structs.GetCellAddressResult;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class refreshHomeCells extends A_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(refreshHomeCells.class.getName());
	
	private final Class<? extends I_HomeCellCreator> m_T_homeCellCreator;
	
	public refreshHomeCells(Class<? extends I_HomeCellCreator> T_homeCellCreator)
	{
		m_T_homeCellCreator = T_homeCellCreator;
	}
	
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
		
		I_HomeCellCreator homeCellCreator = U_Handler.newObjectInstance(m_T_homeCellCreator, response);
		
		if( homeCellCreator == null )  return;
		
		homeCellCreator.initialize(m_serverContext, (ServletContext)context.getNativeContext());
		homeCellCreator.run(request, response, context, session, user);
	}
}
