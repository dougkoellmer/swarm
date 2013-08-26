package swarm.server.handlers.normal;

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
import swarm.server.session.smS_Session;
import swarm.server.session.smSessionManager;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.structs.smE_GetCellAddressError;
import swarm.shared.structs.smGetCellAddressResult;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;
import com.google.appengine.api.datastore.*;

public class sessionQueryTest implements smI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(sessionQueryTest.class.getName());
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		//--- DRK > debug handler, so just doing basic security check.
		if( !sm_s.sessionMngr.isAuthorized(request, response, smE_Role.ADMIN) )
		{
			return;
		}
		
		bhUserSession session = sm_s.sessionMngr.getSession(request, response);
		
		int accountId = session.getAccountId();
		
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query.Filter filter = new Query.FilterPredicate(smS_Session.ACCOUNT_ID_PROPERTY, Query.FilterOperator.EQUAL, accountId);
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
