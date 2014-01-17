package swarm.server.handlers.normal;

import java.util.logging.Level;
import java.util.logging.Logger;






import swarm.server.account.smE_Role;
import swarm.server.account.smS_ServerAccount;
import swarm.server.account.smU_Hashing;
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
import swarm.shared.account.smE_SignInCredentialType;
import swarm.shared.account.smSignInCredentials;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.entities.smA_Grid;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class getHashedPassword extends smA_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(getHashedPassword.class.getName());
	
	public getHashedPassword()
	{
	}
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		String plainTextPassword = request.getJsonArgs().getString("password");
		byte[] passwordSaltBytes = smU_Hashing.calcRandomSaltBytes(smS_ServerAccount.PASSWORD_SALT_BYTE_LENGTH);
		byte[] passwordHashBytes = smU_Hashing.hashWithSalt(plainTextPassword, passwordSaltBytes);
		
		String passwordHashString = smU_Hashing.convertBytesToHashString(passwordHashBytes);
		String passwordSaltString = smU_Hashing.convertBytesToHashString(passwordSaltBytes);
		
		response.getJsonArgs().putString("hash", passwordHashString);
		response.getJsonArgs().putString("salt", passwordSaltString);
	}
}
