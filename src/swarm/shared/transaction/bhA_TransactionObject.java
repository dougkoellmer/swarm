package swarm.shared.transaction;

import swarm.shared.app.sm;
import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhA_JsonFactory;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;


public abstract class bhA_TransactionObject extends bhA_JsonEncodable
{
	private bhI_JsonObject m_jsonArgs = null;

	protected Object m_nativeObject;
	
	protected bhA_TransactionObject()
	{
		m_nativeObject = null;
		m_jsonArgs = null;
	}
	
	protected bhA_TransactionObject(Object nativeObject)
	{
		m_nativeObject = nativeObject;
	}
	
	protected bhA_TransactionObject(bhI_JsonObject jsonArgs)
	{
		m_jsonArgs = jsonArgs;
	}
	
	public void resetJson()
	{
		m_jsonArgs = null;
	}
	
	public bhI_JsonObject getJson()
	{		
		if ( m_jsonArgs == null )
		{
			bhA_JsonFactory jsonFactory = sm.jsonFactory;
			m_jsonArgs = jsonFactory.createJsonObject();
		}
		
		return m_jsonArgs;
	}
	
	public boolean isEqualTo(bhA_TransactionObject otherObject)
	{		
		bhI_JsonObject thisJson = m_jsonArgs;
		bhI_JsonObject otherJson = otherObject.m_jsonArgs;
		
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
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		if( m_jsonArgs != null )
		{
			sm.jsonFactory.getHelper().putJsonObject(json, bhE_JsonKey.requestArgs, m_jsonArgs);
		}
	}
	
	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_jsonArgs = sm.jsonFactory.getHelper().getJsonObject(json, bhE_JsonKey.requestArgs);
	}
}
