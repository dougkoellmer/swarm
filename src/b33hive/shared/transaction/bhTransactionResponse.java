package b33hive.shared.transaction;

import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;

/**
 * ...
 * @author 
 */
public class bhTransactionResponse extends bhA_TransactionObject
{
	private bhE_ResponseError m_error = bhE_ResponseError.NO_ERROR;
	protected Throwable m_clientException = null;
	
	private bhCachePolicy m_cachePolicy = null;
	
	public bhTransactionResponse() 
	{
		super(null);
	}
	
	public Object getNativeResponse()
	{
		return m_nativeObject;
	}
	
	public void reset()
	{
		m_error = bhE_ResponseError.NO_ERROR;
		m_cachePolicy = null;
		resetJson();
	}
	
	public bhTransactionResponse(Object nativeResponse) 
	{
		super(nativeResponse);
	}
	
	public void setCachePolicy(bhCachePolicy policy)
	{
		m_cachePolicy = policy;
	}
	
	public bhCachePolicy getCachePolicy()
	{
		return m_cachePolicy;
	}
	
	public void setError(bhE_ResponseError error)
	{
		m_error = error;
	}
	
	public bhE_ResponseError getError()
	{
		return m_error;
	}
	
	public Throwable getClientException()
	{
		return m_clientException;
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		if( m_error == bhE_ResponseError.NO_ERROR )
		{
			super.writeJson(json);
		}
		
		bhJsonHelper.getInstance().putEnum(json, bhE_JsonKey.responseError, m_error);
	}
	
	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_error = bhJsonHelper.getInstance().getEnum(json, bhE_JsonKey.responseError, bhE_ResponseError.values());
		
		if( m_error == bhE_ResponseError.NO_ERROR )
		{
			super.readJson(json);
		}
	}
}