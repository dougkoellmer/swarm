package swarm.server.handlers.normal;

import java.util.logging.Level;
import java.util.logging.Logger;


import swarm.server.account.smE_Role;
import swarm.server.account.smUserSession;
import swarm.server.account.sm_s;
import swarm.server.blobxn.smBlobTransaction_CreateUser;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smE_BlobTransactionType;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smServerUser;
import swarm.server.session.smSessionManager;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.app.sm;
import swarm.shared.entities.smA_Grid;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class getUserData implements smI_RequestHandler
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
		if( !sm_s.sessionMngr.isAuthorized(request, response, smE_Role.USER) )
		{
			return;
		}
		
		bhUserSession userSession = sm_s.sessionMngr.getSession(request, response);

		smServerUser user = null;
		
		smI_BlobManager blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.PERSISTENT);

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
				bhBlobTransaction_CreateUser createUserTransaction = new smBlobTransaction_CreateUser(userSession, m_autoCreateHomeCell);
				createUserTransaction.perform(smE_BlobTransactionType.MULTI_BLOB_TYPE, 5);
				
				//--- DRK > Not a huge fan of this method of letting client know that grid size changed.
				//---		Better would be if I could let client know through batch system, but I can't figure
				//---		out how to make that efficient without completely spaghettifying server code.
				if( createUserTransaction.didGridGrow() )
				{
					smA_Grid dummyGrid = new smA_Grid(createUserTransaction.getGridWidth(), createUserTransaction.getGridHeight())
					{
						@Override
						public void writeJson(smI_JsonObject json_out)
						{
							//--- DRK > Only sending down width and height so we don't overwrite other properties.
							sm.jsonFactory.getHelper().putInt(json_out, smE_JsonKey.gridWidth, this.getWidth());
							sm.jsonFactory.getHelper().putInt(json_out, smE_JsonKey.gridHeight, this.getHeight());
						}
					};
					
					dummyGrid.writeJson(response.getJson());
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

		sm.jsonFactory.getHelper().putBoolean(response.getJson(), smE_JsonKey.createdUser, createdUser);
		user.writeJson(response.getJson());
	}
}
