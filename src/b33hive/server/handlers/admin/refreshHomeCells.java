package com.b33hive.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.b33hive.server.account.bhE_Role;
import com.b33hive.server.account.bhUserSession;
import com.b33hive.server.data.blob.bhBlobException;
import com.b33hive.server.data.blob.bhBlobManagerFactory;
import com.b33hive.server.data.blob.bhE_BlobCacheLevel;
import com.b33hive.server.data.blob.bhI_BlobManager;
import com.b33hive.server.entities.bhServerCell;
import com.b33hive.server.entities.bhServerUser;
import com.b33hive.server.homecells.bhHomeCellCreator;
import com.b33hive.server.session.bhSessionManager;
import com.b33hive.server.structs.bhServerCellAddressMapping;
import com.b33hive.server.transaction.bhI_RequestHandler;
import com.b33hive.server.transaction.bhTransactionContext;
import com.b33hive.shared.structs.bhE_GetCellAddressError;
import com.b33hive.shared.structs.bhGetCellAddressResult;
import com.b33hive.shared.transaction.bhE_ResponseError;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

public class refreshHomeCells implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(refreshHomeCells.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		if( !bhSessionManager.getInstance().isAuthorized(request, response, bhE_Role.ADMIN) )
		{
			return;
		}
		
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
		
		(new bhHomeCellCreator((ServletContext)context.getNativeContext())).run(request, response, context, session, user);
	}
}
