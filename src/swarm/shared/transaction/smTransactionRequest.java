package swarm.shared.transaction;

import swarm.shared.app.smSharedAppContext;
import swarm.shared.app.smA_App;
import swarm.shared.app.smS_App;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import com.google.gwt.http.client.RequestBuilder;


/**
 * ...
 * @author 
 */
public class smTransactionRequest extends smA_TransactionObject
{
	private long m_dispatchTime = 0;
	private smI_RequestPath m_path = null;
	private int m_retryCount = 0;
	protected smE_HttpMethod m_method;
	private boolean m_isCancelled = false;

	protected Integer m_serverVersion = null;
	
	public smTransactionRequest(smI_RequestPath path) 
	{
		super(null);

		initPath(path);
	}
	
	public smTransactionRequest(smI_RequestPath path, smI_JsonObject jsonArgs) 
	{
		super(jsonArgs);

		initPath(path);
	}
	
	public smTransactionRequest(smI_RequestPath path, Object nativeRequest)
	{
		super(nativeRequest);
		
		initPath(path);
	}
	
	public smTransactionRequest(Object nativeRequest)
	{
		super(nativeRequest);
	}
	
	public smTransactionRequest()
	{
		super(null);
	}
	
	public long getDispatchTime()
	{
		return m_dispatchTime;
	}
	
	private void initPath(smI_RequestPath path)
	{
		m_path = path;
		m_method = path.getDefaultMethod();
	}
	
	public Object getNativeRequest()
	{
		return m_nativeObject;
	}
	
	public void setNativeRequest(Object nativeRequest)
	{
		m_nativeObject = nativeRequest;
	}
	
	public boolean isCancelled()
	{
		return m_isCancelled;
	}
	
	public void cancel()
	{
		m_isCancelled = true;
	}
	
	public Integer getServerVersion()
	{
		return m_serverVersion;
	}
	
	public smE_HttpMethod getMethod()
	{
		return m_method;
	}
	
	public void setPath(smI_RequestPath path)
	{
		m_path = path;
	}
	
	public smI_RequestPath getPath()
	{
		return m_path;
	}
	
	public void onDispatch(long timeInMilliseconds)
	{
		m_dispatchTime = timeInMilliseconds;
	}
	
	public void setServerVersion(int serverVersion)
	{
		m_serverVersion = serverVersion;
	}
	
	@Override
	public boolean isEqualTo(smA_TransactionObject otherObject)
	{
		if( otherObject instanceof smTransactionRequest )
		{
			if( this.getPath() != ((smTransactionRequest)otherObject).getPath() )
			{
				return false;
			}
		}

		return super.isEqualTo(otherObject);
	}
	
	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		super.writeJson(factory, json_out);
		
		smSharedAppContext.requestPathMngr.putToJson(json_out, m_path);
		
		if( m_serverVersion != null )
		{
			factory.getHelper().putInt(json_out, smE_JsonKey.serverVersion, m_serverVersion);
		}
	}
	
	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		super.readJson(factory, json);
		
		m_path = smSharedAppContext.requestPathMngr.getFromJson(json);
		
		Integer serverVersion = factory.getHelper().getInt(json, smE_JsonKey.serverVersion);
		
		m_serverVersion = serverVersion;
	}
}