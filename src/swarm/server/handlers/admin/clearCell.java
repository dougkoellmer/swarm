package swarm.server.handlers.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import swarm.server.account.E_Role;
import swarm.server.account.UserSession;
import swarm.server.blobxn.BlobTransaction_ClearCell;
import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.E_BlobTransactionType;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.ServerCell;
import swarm.server.entities.ServerUser;
import swarm.server.session.SessionManager;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.json.JsonHelper;
import swarm.shared.structs.E_GetCellAddressError;
import swarm.shared.structs.GetCellAddressResult;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class clearCell extends A_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(clearCell.class.getName());
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{		
		ServerCellAddress address = new ServerCellAddress(m_serverContext.jsonFactory, request.getJsonArgs());
		
		BlobTransaction_ClearCell transaction = new BlobTransaction_ClearCell(address);
		
		try
		{
			transaction.perform(m_serverContext.blobMngrFactory, E_BlobTransactionType.MULTI_BLOB_TYPE, 1);
		}
		catch (BlobException e)
		{
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.log(Level.SEVERE, "Could not clear cell because of exception.", e);
		}
	}
}
