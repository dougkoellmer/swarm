package swarm.server.handlers.normal;

import java.util.logging.Level;
import java.util.logging.Logger;






import swarm.server.account.E_Role;
import swarm.server.account.S_ServerAccount;
import swarm.server.account.U_Hashing;
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
import swarm.shared.account.E_SignInCredentialType;
import swarm.shared.account.SignInCredentials;
import swarm.shared.app.BaseAppContext;
import swarm.shared.entities.A_Grid;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class getHashedPassword extends A_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(getHashedPassword.class.getName());
	
	public getHashedPassword()
	{
	}
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		String plainTextPassword = request.getJsonArgs().getString("password");
		byte[] passwordSaltBytes = U_Hashing.calcRandomSaltBytes(S_ServerAccount.PASSWORD_SALT_BYTE_LENGTH);
		byte[] passwordHashBytes = U_Hashing.hashWithSalt(plainTextPassword, passwordSaltBytes);
		
		String passwordHashString = U_Hashing.convertBytesToHashString(passwordHashBytes);
		String passwordSaltString = U_Hashing.convertBytesToHashString(passwordSaltBytes);
		
		response.getJsonArgs().putString("hash", passwordHashString);
		response.getJsonArgs().putString("salt", passwordSaltString);
	}
}
