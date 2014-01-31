package swarm.server.handlers;

import java.util.logging.Logger;

import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.I_BlobManager;
import swarm.shared.statemachine.A_State;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionResponse;

public class U_Handler
{
	private static final Logger s_logger = Logger.getLogger(U_Handler.class.getName());
	
	public static <T extends Object> T newObjectInstance(Class<? extends Object> T, TransactionResponse response)
	{
		Object instance = null;
		try
		{
			instance = T.newInstance();
		}
		catch (Exception e)
		{
			response.setError(E_ResponseError.DEPENDENCY_PROBLEM);
			s_logger.severe("Couldn't create instance: " + e);
		}
		
		return instance != null ? (T) instance : null;
	}
	
	public static I_Blob getBlob(I_BlobManager blobMngr, I_BlobKey key, Class<? extends I_Blob> T, TransactionResponse response)
	{
		I_Blob blob = null;
		
		try
		{
			blob = blobMngr.getBlob(key, T);
		}
		catch(BlobException e)
		{
			s_logger.severe("Exception getting blob for key: " + key + " " + e);
			
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
		}
		
		return blob;
	}
}
