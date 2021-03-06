package swarm.server.handlers.normal;

import java.util.logging.Level;
import java.util.logging.Logger;


import swarm.server.account.E_Role;
import swarm.server.account.UserSession;

import swarm.server.blobxn.BlobTransaction_CreateUser;
import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.E_BlobTransactionType;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.ServerUser;
import swarm.server.session.SessionManager;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.app.BaseAppContext;
import swarm.shared.entities.A_Grid;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class getUserData extends A_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(getUserData.class.getName());
	
	public final boolean m_autoCreateHomeCell;
	public final int m_gridExpansionDelta;
	
	public getUserData(boolean autoCreateHomeCell, int gridExpansionDelta)
	{
		m_autoCreateHomeCell = autoCreateHomeCell;
		m_gridExpansionDelta = gridExpansionDelta;
	}
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		if( !m_serverContext.sessionMngr.isAuthorized(request, response, E_Role.USER) )
		{
			return;
		}
		
		UserSession userSession = m_serverContext.sessionMngr.getSession(request, response);

		ServerUser user = null;
		
		I_BlobManager blobManager = m_serverContext.blobMngrFactory.create(E_BlobCacheLevel.PERSISTENT);

		//--- DRK > First try to just get the user with a straight read...should be successful most of the time.
		try
		{
			user = blobManager.getBlob(userSession, ServerUser.class);
		}
		catch(BlobException e)
		{
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.log(Level.SEVERE, "Could not retrieve user due to exception.", e);
			
			return;
		}
		
		//--- DRK > If the read turned up empty, it means that the user hasn't been created yet, so we have to do that.
		//---		Most of the time this should be because the user is going through the sign up process.
		//---		It is possible however that when the user first signed up, something went wrong with the transaction,
		//---		so subsequent requests that should normally be pure reads end up having to retry user creation implicitly.
		boolean createdUser = false;
		if( user == null )
		{
			try
			{
				BlobTransaction_CreateUser createUserTransaction = new BlobTransaction_CreateUser(userSession, m_autoCreateHomeCell, m_gridExpansionDelta);
				createUserTransaction.perform(m_serverContext.blobMngrFactory, E_BlobTransactionType.MULTI_BLOB_TYPE, 5);
				
				//--- DRK > Not a huge fan of this method of letting client know that grid size changed.
				//---		Better would be if I could let client know through batch system, but I can't figure
				//---		out how to make that efficient without completely spaghettifying server code.
				if( createUserTransaction.didGridGrow() )
				{
					A_Grid dummyGrid = new A_Grid(createUserTransaction.getGridWidth(), createUserTransaction.getGridHeight())
					{
						@Override
						public void writeJson(I_JsonObject json_out, A_JsonFactory factory)
						{
							//--- DRK > Only sending down width and height so we don't overwrite other properties.
							factory.getHelper().putInt(json_out, E_JsonKey.gridWidth, this.getWidth());
							factory.getHelper().putInt(json_out, E_JsonKey.gridHeight, this.getHeight());
						}
					};
					
					dummyGrid.writeJson(response.getJsonArgs(), m_serverContext.jsonFactory);
				}
				
				user = createUserTransaction.getUser();
				createdUser = true;
			}
			catch(BlobException e)
			{
				response.setError(E_ResponseError.SERVICE_EXCEPTION);
				
				s_logger.log(Level.SEVERE, "Could not create user because of exception.", e);
				
				return;
			}
		}
		
		//--- DRK > Just being really anal here...should never be null by this point.
		if( user == null )
		{
			response.setError(E_ResponseError.SERVICE_EXCEPTION);

			s_logger.severe("User object came up null when it should have been initialized.");
			
			return;
		}

		m_serverContext.jsonFactory.getHelper().putBoolean(response.getJsonArgs(), E_JsonKey.createdUser, createdUser);
		user.writeJson(response.getJsonArgs(), m_serverContext.jsonFactory);
	}
}
