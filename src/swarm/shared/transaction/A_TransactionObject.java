package swarm.shared.transaction;

import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;


public abstract class A_TransactionObject
{
	private I_JsonObject m_jsonArgs = null;

	protected Object m_nativeObject;
	private final A_JsonFactory m_jsonFactory;
	
	protected A_TransactionObject(A_JsonFactory jsonFactory)
	{
		m_jsonFactory = jsonFactory;
		m_nativeObject = null;
		m_jsonArgs = null;
	}
	
	protected A_TransactionObject(A_JsonFactory jsonFactory, Object nativeObject)
	{
		m_jsonFactory = jsonFactory;
		m_nativeObject = nativeObject;
	}
	
	public void clearJsonArgs()
	{
		m_jsonArgs = null;
	}
	
	public I_JsonObject getJsonArgs()
	{
		if ( m_jsonArgs == null )
		{
			A_JsonFactory jsonFactory = m_jsonFactory;
			m_jsonArgs = jsonFactory.createJsonObject();
		}
		
		return m_jsonArgs;
	}
	
	public boolean isEqualTo(A_TransactionObject otherObject)
	{		
		I_JsonObject thisJson = m_jsonArgs;
		I_JsonObject otherJson = otherObject.m_jsonArgs;
		
		if( thisJson == null && otherJson == null )
		{
			return true;
		}
		
		if( thisJson != null && otherJson != null )
		{
			return thisJson.isEqualTo(otherJson);
		}
		
		return false;
	}
	
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		if( m_jsonArgs != null )
		{
			factory.getHelper().putJsonObject(json_out, E_JsonKey.txnArgs, m_jsonArgs);
		}
	}
	
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		m_jsonArgs = factory.getHelper().getJsonObject(json, E_JsonKey.txnArgs);
	}
	
	@Override
	public String toString()
	{
		return m_jsonArgs != null ? m_jsonArgs.toString() : "";
	}
}
