package b33hive.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import b33hive.server.account.bhE_Role;
import b33hive.server.account.bhUserSession;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.entities.bhServerCell;
import b33hive.server.entities.bhServerUser;
import b33hive.server.handlers.bhU_Handler;
import b33hive.server.session.bhSessionManager;
import b33hive.server.structs.bhServerCellAddressMapping;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.structs.bhE_GetCellAddressError;
import b33hive.shared.structs.bhGetCellAddressResult;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

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
		bhI_BlobManager blobManager = bhBlobManagerFactory.getInstance().create(bhE_BlobCacheLevel.values());
		bhUserSession session = bhSessionManager.getInstance().getSession(request, response);
		
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
