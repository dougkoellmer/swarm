package swarm.server.thirdparty.json;

import java.util.logging.Logger;

import org.json.JSONException;

import swarm.client.thirdparty.json.GwtJsonArray;
import swarm.client.thirdparty.json.GwtJsonObject;
import swarm.server.app.A_ServerJsonFactory;
import swarm.server.transaction.I_TransactionScopeListener;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.reflection.I_Class;

public class ServerJsonFactory extends A_ServerJsonFactory
{
	private static final Logger s_logger = Logger.getLogger(ServerJsonFactory.class.getName());
	
	private final ThreadLocal<JsonHelper> m_threadLocal = new ThreadLocal<JsonHelper>();
	
	public ServerJsonFactory()
	{
	}
	
	public void startScope(boolean verboseKeys)
	{
		m_threadLocal.set(new JsonHelper(verboseKeys));
	}
	
	public void endScope()
	{
		m_threadLocal.remove();
	}
	
	private final I_Class<I_JsonObject> m_objectClass = new I_Class<I_JsonObject>()
	{
		@Override
		public I_JsonObject newInstance()
		{
			return new ServerJsonObject(ServerJsonFactory.this);
		}
	};
	
	private final I_Class<I_JsonArray> m_arrayClass = new I_Class<I_JsonArray>()
	{
		@Override
		public I_JsonArray newInstance()
		{
			return new ServerJsonArray(ServerJsonFactory.this);
		}
	};
	
	@Override
	public I_Class<? extends I_JsonObject> getJsonObjectClass()
	{
		return m_objectClass;
	}
	
	@Override
	public I_Class<? extends I_JsonArray> getJsonArrayClass()
	{
		return m_arrayClass;
	}
	
	@Override
	public I_JsonObject createJsonObject(String data)
	{
		if( data == null || data.length() == 0 )  return null;
		
		try
		{
			return new ServerJsonObject(this, data);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public I_JsonArray createJsonArray(String data)
	{
		try
		{
			return new ServerJsonArray(this, data);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public JsonHelper getHelper()
	{
		JsonHelper helper = m_threadLocal.get();
		
		if( helper == null )
		{
			s_logger.severe("Didn't expect json helper to be null.");
			
			helper = new JsonHelper(false);
		}
		
		return helper;
	}
}
