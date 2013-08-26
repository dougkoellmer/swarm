package swarm.shared.transaction;

import swarm.shared.json.smE_JsonKey;
import swarm.shared.app.sm;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

/**
 * ...
 * @author 
 */
public class smTransactionResponse extends smA_TransactionObject
{
	private smE_ResponseError m_error = smE_ResponseError.NO_ERROR;
	protected Throwable m_clientException = null;
	
	private bhCachePolicy m_cachePolicy = null;
	
	public smTransactionResponse() 
	{
		super(null);
	}
	
	public Object getNativeResponse()
	{
		return m_nativeObject;
	}
	
	public void reset()
	{
		m_error = smE_ResponseError.NO_ERROR;
		m_cachePolicy = null;
		resetJson();
	}
	
	public smTransactionResponse(Object nativeResponse) 
	{
		super(nativeResponse);
	}
	
	public void setCachePolicy(smCachePolicy policy)
	{
		m_cachePolicy = policy;
	}
	
	public smCachePolicy getCachePolicy()
	{
		return m_cachePolicy;
	}
	
	public void setError(smE_ResponseError error)
	{
		m_error = error;
	}
	
	public smE_ResponseError getError()
	{
		return m_error;
	}
	
	public Throwable getClientException()
	{
		return m_clientException;
	}
	
	@Override
	public void writeJson(smI_JsonObject json)
	{
		if( m_error == smE_ResponseError.NO_ERROR )
		{
			super.writeJson(json);
		}
		
		sm.jsonFactory.getHelper().putEnum(json, smE_JsonKey.responseError, m_error);
	}
	
	@Override
	public void readJson(smI_JsonObject json)
	{
		m_error = sm.jsonFactory.getHelper().getEnum(json, smE_JsonKey.responseError, smE_ResponseError.values());
		
		if( m_error == smE_ResponseError.NO_ERROR )
		{
			super.readJson(json);
		}
	}
}