package com.b33hive.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.b33hive.server.account.bhE_Role;
import com.b33hive.server.account.bhUserSession;
import com.b33hive.server.app.bhS_ServerApp;
import com.b33hive.server.data.blob.bhBlobException;
import com.b33hive.server.data.blob.bhBlobManagerFactory;
import com.b33hive.server.data.blob.bhE_BlobCacheLevel;
import com.b33hive.server.data.blob.bhE_BlobTransactionType;
import com.b33hive.server.data.blob.bhI_BlobManager;
import com.b33hive.server.entities.bhServerGrid;
import com.b33hive.server.entities.bhServerUser;
import com.b33hive.server.handlers.bhBlobTransaction_DeactivateUserCells;
import com.b33hive.server.homecells.bhHomeCellCreator;
import com.b33hive.server.session.bhSessionManager;
import com.b33hive.server.structs.bhServerCellAddress;
import com.b33hive.server.structs.bhServerCodePrivileges;
import com.b33hive.server.transaction.bhI_RequestHandler;
import com.b33hive.server.transaction.bhServerTransactionManager;
import com.b33hive.server.transaction.bhTransactionContext;
import com.b33hive.shared.entities.bhA_User;
import com.b33hive.shared.entities.bhE_CodeType;
import com.b33hive.shared.json.bhE_JsonKey;
import com.b33hive.shared.json.bhJsonHelper;
import com.b33hive.shared.structs.bhCode;
import com.b33hive.shared.structs.bhE_NetworkPrivilege;
import com.b33hive.shared.structs.bhGridCoordinate;
import com.b33hive.shared.transaction.bhE_RequestPath;
import com.b33hive.shared.transaction.bhE_ResponseError;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

public class deactivateUserCells implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(deactivateUserCells.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		if( !bhSessionManager.getInstance().isAuthorized(request, response, bhE_Role.ADMIN) )
		{
			return;
		}
		
		Integer accountId = bhJsonHelper.getInstance().getInt(request.getJson(), bhE_JsonKey.accountId);
		
		if( accountId == null )
		{
			response.setError(bhE_ResponseError.BAD_INPUT);
			
			return;
		}
		
		bhUserSession dummySession = new bhUserSession(accountId, "", bhE_Role.USER);
		
		bhBlobTransaction_DeactivateUserCells deactivateCells = new bhBlobTransaction_DeactivateUserCells(dummySession);
		
		try
		{
			deactivateCells.perform(bhE_BlobTransactionType.MULTI_BLOB_TYPE, 1);
		}
		catch(bhBlobException e)
		{
			s_logger.log(Level.SEVERE, "Could not deactivate user cells.", e);
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			return;
		}
	}
}
