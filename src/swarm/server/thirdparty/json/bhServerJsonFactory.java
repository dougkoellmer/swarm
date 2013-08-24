package swarm.server.thirdparty.json;

import java.util.logging.Logger;

import org.json.JSONException;

import swarm.client.thirdparty.json.bhGwtJsonArray;
import swarm.client.thirdparty.json.bhGwtJsonObject;
import swarm.server.app.bhA_ServerJsonFactory;
import swarm.server.transaction.bhI_TransactionScopeListener;
import swarm.shared.json.bhA_JsonFactory;
import swarm.shared.json.bhI_JsonArray;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.reflection.bhI_Class;

public class bhServerJsonFactory extends bhA_ServerJsonFactory
{
	private static final Logger s_logger = Logger.getLogger(bhServerJsonFactory.class.getName());
	
	private final ThreadLocal<bhJsonHelper> m_threadLocal = new ThreadLocal<bhJsonHelper>();
	
	public bhServerJsonFactory()
	{
	}
	
	public void startScope(boolean verboseKeys)
	{
		m_threadLocal.set(new bhJsonHelper(verboseKeys));
	}
	
	public void endScope()
	{
		m_threadLocal.remove();
	}
	
	private final bhI_Class<bhI_JsonObject> m_objectClass = new bhI_Class<bhI_JsonObject>()
	{
		@Override
		public bhI_JsonObject newInstance()
		{
			return new bhServerJsonObject();
		}
	};
	
	private final bhI_Class<bhI_JsonArray> m_arrayClass = new bhI_Class<bhI_JsonArray>()
	{
		@Override
		public bhI_JsonArray newInstance()
		{
			return new bhServerJsonArray();
		}
	};
	
	@Override
	public bhI_Class<? extends bhI_JsonObject> getJsonObjectClass()
	{
		return m_objectClass;
	}
	
	@Override
	public bhI_Class<? extends bhI_JsonArray> getJsonArrayClass()
	{
		return m_arrayClass;
	}
	
	@Override
	public bhI_JsonObject createJsonObject(String data)
	{
		try
		{
			return new bhServerJsonObject(data);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public bhI_JsonArray createJsonArray(String data)
	{
		try
		{
			return new bhServerJsonArray(data);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public bhJsonHelper getHelper()
	{
		bhJsonHelper helper = m_threadLocal.get();
		
		if( helper == null )
		{
			s_logger.severe("Didn't expect json helper to be null.");
			
			helper = new bhJsonHelper(false);
		}
		
		return helper;
	}
}
