package swarm.server.handlers;

import java.util.logging.Logger;

import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smI_BlobKey;
import swarm.server.data.blob.smI_BlobManager;
import swarm.shared.statemachine.smA_State;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionResponse;

public class smU_Handler
{
	private static final Logger s_logger = Logger.getLogger(smU_Handler.class.getName());
	
	public static <T extends Object> T newObjectInstance(Class<? extends Object> T, smTransactionResponse response)
	{
		Object instance = null;
		try
		{
			instance = T.newInstance();
		}
		catch (Exception e)
		{
			response.setError(smE_ResponseError.DEPENDENCY_PROBLEM);
			s_logger.severe("Couldn't create instance: " + e);
		}
		
		return instance != null ? (T) instance : null;
	}
	
	public static smI_Blob getBlob(smI_BlobManager blobMngr, smI_BlobKey key, Class<? extends smI_Blob> T, smTransactionResponse response)
	{
		smI_Blob blob = null;
		
		try
		{
			blob = blobMngr.getBlob(key, T);
		}
		catch(smBlobException e)
		{
			s_logger.severe("Exception getting blob for key: " + key + " " + e);
			
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
		}
		
		return blob;
	}
}
