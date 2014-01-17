package swarm.shared.transaction;

import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;


public abstract class smA_TransactionObject
{
	private smI_JsonObject m_jsonArgs = null;

	protected Object m_nativeObject;
	private final smA_JsonFactory m_jsonFactory;
	
	protected smA_TransactionObject(smA_JsonFactory jsonFactory)
	{
		m_jsonFactory = jsonFactory;
		m_nativeObject = null;
		m_jsonArgs = null;
	}
	
	protected smA_TransactionObject(smA_JsonFactory jsonFactory, Object nativeObject)
	{
		m_jsonFactory = jsonFactory;
		m_nativeObject = nativeObject;
	}
	
	public void clearJsonArgs()
	{
		m_jsonArgs = null;
	}
	
	public smI_JsonObject getJsonArgs()
	{
		if ( m_jsonArgs == null )
		{
			smA_JsonFactory jsonFactory = m_jsonFactory;
			m_jsonArgs = jsonFactory.createJsonObject();
		}
		
		return m_jsonArgs;
	}
	
	public boolean isEqualTo(smA_TransactionObject otherObject)
	{		
		smI_JsonObject thisJson = m_jsonArgs;
		smI_JsonObject otherJson = otherObject.m_jsonArgs;
		
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
	
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		if( m_jsonArgs != null )
		{
			factory.getHelper().putJsonObject(json_out, smE_JsonKey.txnArgs, m_jsonArgs);
		}
	}
	
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		m_jsonArgs = factory.getHelper().getJsonObject(json, smE_JsonKey.txnArgs);
	}
}
