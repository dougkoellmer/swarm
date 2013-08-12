package b33hive.server.handlers;

import java.util.logging.Logger;

import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhI_Blob;
import b33hive.server.data.blob.bhI_BlobKey;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionResponse;

public class bhU_Handler
{
	private static final Logger s_logger = Logger.getLogger(bhU_Handler.class.getName());
	
	public static <T extends Object> T newObjectInstance(Class<? extends Object> T, bhTransactionResponse response)
	{
		Object instance = null;
		try
		{
			instance = T.newInstance();
		}
		catch (Exception e)
		{
			response.setError(bhE_ResponseError.DEPENDENCY_PROBLEM);
			s_logger.severe("Couldn't create instance: " + e);
		}
		
		return instance != null ? (T) instance : null;
	}
	
	public static bhI_Blob getBlob(bhI_BlobManager blobMngr, bhI_BlobKey key, Class<? extends bhI_Blob> T, bhTransactionResponse response)
	{
		bhI_Blob blob = null;
		
		try
		{
			blob = blobMngr.getBlob(key, T);
		}
		catch(bhBlobException e)
		{
			s_logger.severe("Exception getting blob for key: " + key + " " + e);
			
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
		}
		
		return blob;
	}
}
