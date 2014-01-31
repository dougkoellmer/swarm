package swarm.server.handlers.normal;

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
import swarm.server.session.S_Session;
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
import com.google.appengine.api.datastore.*;

public class sessionQueryTest extends A_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(sessionQueryTest.class.getName());
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		//--- DRK > debug handler, so just doing basic security check.
		if( !m_serverContext.sessionMngr.isAuthorized(request, response, E_Role.ADMIN) )
		{
			return;
		}
		
		UserSession session = m_serverContext.sessionMngr.getSession(request, response);
		
		int accountId = session.getAccountId();
		
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query.Filter filter = new Query.FilterPredicate(S_Session.ACCOUNT_ID_PROPERTY, Query.FilterOperator.EQUAL, accountId);
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
