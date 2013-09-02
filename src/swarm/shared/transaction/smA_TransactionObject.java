package swarm.shared.transaction;

import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;


public abstract class smA_TransactionObject extends smA_JsonEncodable
{
	private smI_JsonObject m_jsonArgs = null;

	protected Object m_nativeObject;
	
	protected smA_TransactionObject()
	{
		m_nativeObject = null;
		m_jsonArgs = null;
	}
	
	protected smA_TransactionObject(Object nativeObject)
	{
		m_nativeObject = nativeObject;
	}
	
	protected smA_TransactionObject(smI_JsonObject jsonArgs)
	{
		m_jsonArgs = jsonArgs;
	}
	
	public void resetJson()
	{
		m_jsonArgs = null;
	}
	
	public smI_JsonObject getJsonArgs()
	{
		if ( m_jsonArgs == null )
		{
			smA_JsonFactory jsonFactory = smSharedAppContext.jsonFactory;
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
	
	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		if( m_jsonArgs != null )
		{
			factory.getHelper().putJsonObject(json_out, smE_JsonKey.requestArgs, m_jsonArgs);
		}
	}
	
	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		m_jsonArgs = factory.getHelper().getJsonObject(json, smE_JsonKey.requestArgs);
	}
}
