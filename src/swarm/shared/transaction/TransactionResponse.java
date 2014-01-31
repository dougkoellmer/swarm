package swarm.shared.transaction;

import swarm.shared.json.E_JsonKey;
import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

/**
 * ...
 * @author 
 */
public class TransactionResponse extends A_TransactionObject
{
	private E_ResponseError m_error = E_ResponseError.NO_ERROR;
	protected Throwable m_clientException = null;
	
	private CachePolicy m_cachePolicy = null;
	
	public TransactionResponse(A_JsonFactory jsonFactory) 
	{
		super(jsonFactory);
	}
	
	public TransactionResponse(A_JsonFactory jsonFactory, Object nativeResponse) 
	{
		super(jsonFactory, nativeResponse);
	}
	
	public Object getNativeResponse()
	{
		return m_nativeObject;
	}
	
	public void clear()
	{
		m_error = E_ResponseError.NO_ERROR;
		m_cachePolicy = null;
		clearJsonArgs();
	}
	
	public void setCachePolicy(CachePolicy policy)
	{
		m_cachePolicy = policy;
	}
	
	public CachePolicy getCachePolicy()
	{
		return m_cachePolicy;
	}
	
	public void setError(E_ResponseError error)
	{
		m_error = error;
	}
	
	public E_ResponseError getError()
	{
		return m_error;
	}
	
	public Throwable getClientException()
	{
		return m_clientException;
	}
	
	@Override
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		if( m_error == E_ResponseError.NO_ERROR )
		{
			super.writeJson(factory, json_out);
		}
		
		factory.getHelper().putEnum(json_out, E_JsonKey.responseError, m_error);
	}
	
	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		m_error = factory.getHelper().getEnum(json, E_JsonKey.responseError, E_ResponseError.values());
		
		if( m_error == E_ResponseError.NO_ERROR )
		{
			super.readJson(factory, json);
		}
	}
}