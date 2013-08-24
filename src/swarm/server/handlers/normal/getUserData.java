package swarm.server.handlers.normal;

import java.util.logging.Level;
import java.util.logging.Logger;


import swarm.server.account.bhE_Role;
import swarm.server.account.bhUserSession;
import swarm.server.account.sm_s;
import swarm.server.blobxn.bhBlobTransaction_CreateUser;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhE_BlobTransactionType;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.entities.bhServerUser;
import swarm.server.session.bhSessionManager;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.app.sm;
import swarm.shared.entities.bhA_Grid;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class getUserData implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(getUserData.class.getName());
	
	public boolean m_autoCreateHomeCell;
	
	public getUserData(boolean autoCreateHomeCell)
	{
		m_autoCreateHomeCell = autoCreateHomeCell;
	}
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		if( !sm_s.sessionMngr.isAuthorized(request, response, bhE_Role.USER) )
		{
			return;
		}
		
		bhUserSession userSession = sm_s.sessionMngr.getSession(request, response);

		bhServerUser user = null;
		
		bhI_BlobManager blobManager = sm_s.blobMngrFactory.create(bhE_BlobCacheLevel.PERSISTENT);

		//--- DRK > First try to just get the user with a straight read...should be successful most of the time.
		try
		{
			user = blobManager.getBlob(userSession, bhServerUser.class);
		}
		catch(bhBlobException e)
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
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
				bhBlobTransaction_CreateUser createUserTransaction = new bhBlobTransaction_CreateUser(userSession, m_autoCreateHomeCell);
				createUserTransaction.perform(bhE_BlobTransactionType.MULTI_BLOB_TYPE, 5);
				
				//--- DRK > Not a huge fan of this method of letting client know that grid size changed.
				//---		Better would be if I could let client know through batch system, but I can't figure
				//---		out how to make that efficient without completely spaghettifying server code.
				if( createUserTransaction.didGridGrow() )
				{
					bhA_Grid dummyGrid = new bhA_Grid(createUserTransaction.getGridWidth(), createUserTransaction.getGridHeight())
					{
						@Override
						public void writeJson(bhI_JsonObject json_out)
						{
							//--- DRK > Only sending down width and height so we don't overwrite other properties.
							sm.jsonFactory.getHelper().putInt(json_out, bhE_JsonKey.gridWidth, this.getWidth());
							sm.jsonFactory.getHelper().putInt(json_out, bhE_JsonKey.gridHeight, this.getHeight());
						}
					};
					
					dummyGrid.writeJson(response.getJson());
				}
				
				user = createUserTransaction.getUser();
				createdUser = true;
			}
			catch(bhBlobException e)
			{
				response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
				
				s_logger.log(Level.SEVERE, "Could not create user because of exception.", e);
				
				return;
			}
		}
		
		//--- DRK > Just being really anal here...should never be null by this point.
		if( user == null )
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);

			s_logger.severe("User object came up null when it should have been initialized.");
			
			return;
		}

		sm.jsonFactory.getHelper().putBoolean(response.getJson(), bhE_JsonKey.createdUser, createdUser);
		user.writeJson(response.getJson());
	}
}
