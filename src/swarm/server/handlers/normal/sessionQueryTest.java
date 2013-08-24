package swarm.server.handlers.normal;

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
import swarm.server.session.bhS_Session;
import swarm.server.session.bhSessionManager;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.structs.bhE_GetCellAddressError;
import swarm.shared.structs.bhGetCellAddressResult;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;
import com.google.appengine.api.datastore.*;

public class sessionQueryTest implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(sessionQueryTest.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		//--- DRK > debug handler, so just doing basic security check.
		if( !sm_s.sessionMngr.isAuthorized(request, response, bhE_Role.ADMIN) )
		{
			return;
		}
		
		bhUserSession session = sm_s.sessionMngr.getSession(request, response);
		
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
