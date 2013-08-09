package b33hive.server.handlers.normal;

import java.util.logging.Level;
import java.util.logging.Logger;


import b33hive.server.account.bhE_Role;
import b33hive.server.account.bhUserSession;
import b33hive.server.account.bh_s;
import b33hive.server.blobxn.bhBlobTransaction_CreateUser;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhE_BlobTransactionType;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.entities.bhServerUser;
import b33hive.server.session.bhSessionManager;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.app.bh;
import b33hive.shared.entities.bhA_Grid;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class getUserData implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(getUserData.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		if( !bh_s.sessionMngr.isAuthorized(request, response, bhE_Role.USER) )
		{
			return;
		}
		
		bhUserSession userSession = bh_s.sessionMngr.getSession(request, response);

		bhServerUser user = null;
		
		bhI_BlobManager blobManager = bh_s.blobMngrFactory.create(bhE_BlobCacheLevel.PERSISTENT);

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
				bhBlobTransaction_CreateUser createUserTransaction = new bhBlobTransaction_CreateUser(userSession, true);
				createUserTransaction.perform(bhE_BlobTransactionType.MULTI_BLOB_TYPE, 5);
				
				//--- DRK > Not a huge fan of this method of letting client know that grid size changed.
				//---		Better would be if I could let client know through batch system, but I can't figure
				//---		out how to make that efficient without completely spaghettifying server code.
				if( createUserTransaction.didGridGrow() )
				{
					bhA_Grid dummyGrid = new bhA_Grid(createUserTransaction.getGridWidth(), createUserTransaction.getGridHeight()){};
					
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

		bh.jsonFactory.getHelper().putBoolean(response.getJson(), bhE_JsonKey.createdUser, createdUser);
		user.writeJson(response.getJson());
	}
}
