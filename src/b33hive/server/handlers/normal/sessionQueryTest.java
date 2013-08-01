package com.b33hive.server.handlers;

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
import com.b33hive.server.session.bhS_Session;
import com.b33hive.server.session.bhSessionManager;
import com.b33hive.server.structs.bhServerCellAddressMapping;
import com.b33hive.server.transaction.bhI_RequestHandler;
import com.b33hive.server.transaction.bhTransactionContext;
import com.b33hive.shared.structs.bhE_GetCellAddressError;
import com.b33hive.shared.structs.bhGetCellAddressResult;
import com.b33hive.shared.transaction.bhE_ResponseError;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.*;

public class sessionQueryTest implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(sessionQueryTest.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		//--- DRK > debug handler, so just doing basic security check.
		if( !bhSessionManager.getInstance().isAuthorized(request, response, bhE_Role.ADMIN) )
		{
			return;
		}
		
		bhUserSession session = bhSessionManager.getInstance().getSession(request, response);
		
		int accountId = session.getAccountId();
		
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query.Filter filter = new Query.FilterPredicate(bhS_Session.ACCOUNT_ID_PROPERTY, Query.FilterOperator.EQUAL, accountId);
		Query query = new Query(session.getKind()).setFilter(filter);
		
		PreparedQuery pq = datastore.prepare(query);

		for (Entity result : pq.asIterable())
		{
		  String firstName = (String) result.getProperty("firstName");
		  String lastName = (String) result.getProperty("lastName");
		  Long height = (Long) result.getProperty("height");

		  System.out.println(firstName + " " + lastName + ", " + height + " inches tall");
		}
	}
}
