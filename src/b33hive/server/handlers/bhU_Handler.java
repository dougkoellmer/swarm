package b33hive.server.handlers;

import java.util.logging.Logger;

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
}
