package swarm.server.handlers.normal;

import java.util.logging.Level;
import java.util.logging.Logger;


import swarm.server.account.smE_Role;
import swarm.server.account.smUserSession;

import swarm.server.blobxn.smBlobTransaction_CreateUser;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smE_BlobTransactionType;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smServerUser;
import swarm.server.session.smSessionManager;
import swarm.server.transaction.smA_DefaultRequestHandler;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.entities.smA_Grid;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class getUserData extends smA_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(getUserData.class.getName());
	
	public boolean m_autoCreateHomeCell;
	
	public getUserData(boolean autoCreateHomeCell)
	{
		m_autoCreateHomeCell = autoCreateHomeCell;
	}
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		if( !m_context.sessionMngr.isAuthorized(request, response, smE_Role.USER) )
		{
			return;
		}
		
		smUserSession userSession = m_context.sessionMngr.getSession(request, response);

		smServerUser user = null;
		
		smI_BlobManager blobManager = m_context.blobMngrFactory.create(smE_BlobCacheLevel.PERSISTENT);

		//--- DRK > First try to just get the user with a straight read...should be successful most of the time.
		try
		{
			user = blobManager.getBlob(userSession, smServerUser.class);
		}
		catch(smBlobException e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			
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
				smBlobTransaction_CreateUser createUserTransaction = new smBlobTransaction_CreateUser(userSession, m_autoCreateHomeCell);
				createUserTransaction.perform(smE_BlobTransactionType.MULTI_BLOB_TYPE, 5);
				
				//--- DRK > Not a huge fan of this method of letting client know that grid size changed.
				//---		Better would be if I could let client know through batch system, but I can't figure
				//---		out how to make that efficient without completely spaghettifying server code.
				if( createUserTransaction.didGridGrow() )
				{
					smA_Grid dummyGrid = new smA_Grid(createUserTransaction.getGridWidth(), createUserTransaction.getGridHeight())
					{
						@Override
						public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
						{
							//--- DRK > Only sending down width and height so we don't overwrite other properties.
							factory.getHelper().putInt(json_out, smE_JsonKey.gridWidth, this.getWidth());
							factory.getHelper().putInt(json_out, smE_JsonKey.gridHeight, this.getHeight());
						}
					};
					
					dummyGrid.writeJson(null, response.getJsonArgs());
				}
				
				user = createUserTransaction.getUser();
				createdUser = true;
			}
			catch(smBlobException e)
			{
				response.setError(smE_ResponseError.SERVICE_EXCEPTION);
				
				s_logger.log(Level.SEVERE, "Could not create user because of exception.", e);
				
				return;
			}
		}
		
		//--- DRK > Just being really anal here...should never be null by this point.
		if( user == null )
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);

			s_logger.severe("User object came up null when it should have been initialized.");
			
			return;
		}

		m_context.jsonFactory.getHelper().putBoolean(response.getJsonArgs(), smE_JsonKey.createdUser, createdUser);
		user.writeJson(null, response.getJsonArgs());
	}
}
