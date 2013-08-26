package swarm.server.thirdparty.json;

import java.util.logging.Logger;

import org.json.JSONException;

import swarm.client.thirdparty.json.smGwtJsonArray;
import swarm.client.thirdparty.json.smGwtJsonObject;
import swarm.server.app.smA_ServerJsonFactory;
import swarm.server.transaction.smI_TransactionScopeListener;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.reflection.smI_Class;

public class smServerJsonFactory extends smA_ServerJsonFactory
{
	private static final Logger s_logger = Logger.getLogger(smServerJsonFactory.class.getName());
	
	private final ThreadLocal<smJsonHelper> m_threadLocal = new ThreadLocal<smJsonHelper>();
	
	public smServerJsonFactory()
	{
	}
	
	public void startScope(boolean verboseKeys)
	{
		m_threadLocal.set(new smJsonHelper(verboseKeys));
	}
	
	public void endScope()
	{
		m_threadLocal.remove();
	}
	
	private final smI_Class<smI_JsonObject> m_objectClass = new smI_Class<smI_JsonObject>()
	{
		@Override
		public smI_JsonObject newInstance()
		{
			return new smServerJsonObject();
		}
	};
	
	private final smI_Class<smI_JsonArray> m_arrayClass = new smI_Class<smI_JsonArray>()
	{
		@Override
		public smI_JsonArray newInstance()
		{
			return new smServerJsonArray();
		}
	};
	
	@Override
	public smI_Class<? extends smI_JsonObject> getJsonObjectClass()
	{
		return m_objectClass;
	}
	
	@Override
	public smI_Class<? extends smI_JsonArray> getJsonArrayClass()
	{
		return m_arrayClass;
	}
	
	@Override
	public smI_JsonObject createJsonObject(String data)
	{
		try
		{
			return new smServerJsonObject(data);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public smI_JsonArray createJsonArray(String data)
	{
		try
		{
			return new smServerJsonArray(data);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public smJsonHelper getHelper()
	{
		smJsonHelper helper = m_threadLocal.get();
		
		if( helper == null )
		{
			s_logger.severe("Didn't expect json helper to be null.");
			
			helper = new smJsonHelper(false);
		}
		
		return helper;
	}
}
