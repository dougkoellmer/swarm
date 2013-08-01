package com.b33hive.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.b33hive.server.account.bhE_Role;
import com.b33hive.server.account.bhUserSession;
import com.b33hive.server.data.blob.bhBlobException;
import com.b33hive.server.data.blob.bhBlobManagerFactory;
import com.b33hive.server.data.blob.bhE_BlobCacheLevel;
import com.b33hive.server.data.blob.bhE_BlobTransactionType;
import com.b33hive.server.data.blob.bhI_BlobManager;
import com.b33hive.server.entities.bhServerCell;
import com.b33hive.server.entities.bhServerUser;
import com.b33hive.server.handlers.bhBlobTransaction_ClearCell;
import com.b33hive.server.homecells.bhHomeCellCreator;
import com.b33hive.server.session.bhSessionManager;
import com.b33hive.server.structs.bhServerCellAddress;
import com.b33hive.server.structs.bhServerCellAddressMapping;
import com.b33hive.server.transaction.bhI_RequestHandler;
import com.b33hive.server.transaction.bhTransactionContext;
import com.b33hive.shared.json.bhJsonHelper;
import com.b33hive.shared.structs.bhE_GetCellAddressError;
import com.b33hive.shared.structs.bhGetCellAddressResult;
import com.b33hive.shared.transaction.bhE_ResponseError;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

public class clearCell implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(clearCell.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		if( !bhSessionManager.getInstance().isAuthorized(request, response, bhE_Role.ADMIN) )
		{
			return;
		}
		
		bhServerCellAddress address = new bhServerCellAddress(request.getJson());
		
		bhBlobTransaction_ClearCell transaction = new bhBlobTransaction_ClearCell(address);
		
		try {
			transaction.perform(bhE_BlobTransactionType.MULTI_BLOB_TYPE, 1);
		} catch (bhBlobException e)
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.log(Level.SEVERE, "Could not clear cell because of exception.", e);
		}
	}
}
