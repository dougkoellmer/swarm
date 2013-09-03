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
	
	private smRequestPathManager m_requestPathMngr = null;
	
	public smTransactionRequest(smA_JsonFactory jsonFactory, smI_RequestPath path) 
	{
		super(jsonFactory);

		initPath(path);
	}
	
	public smTransactionRequest(smA_JsonFactory jsonFactory, smI_RequestPath path, Object nativeRequest)
	{
		super(jsonFactory, nativeRequest);
		
		initPath(path);
	}
	
	public smTransactionRequest(smA_JsonFactory jsonFactory, Object nativeRequest)
	{
		super(jsonFactory, nativeRequest);
	}
	
	public smTransactionRequest(smA_JsonFactory jsonFactory, smRequestPathManager requestPathMngr, Object nativeRequest)
	{
		super(jsonFactory, nativeRequest);
		
		m_requestPathMngr = requestPathMngr;
	}
	
	public smTransactionRequest(smA_JsonFactory jsonFactory)
	{
		super(jsonFactory);
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
	
	public smI_RequestPath getPath()
	{
		return m_path;
	}
	
	public void onDispatch(long timeInMilliseconds)
	{
		m_dispatchTime = timeInMilliseconds;
		m_serverVersion = null;
	}
	
	public void onDispatch(long timeInMilliseconds, int serverVersion)
	{
		m_dispatchTime = timeInMilliseconds;
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
		
		m_requestPathMngr.putToJson(json_out, m_path);
		
		if( m_serverVersion != null )
		{
			factory.getHelper().putInt(json_out, smE_JsonKey.serverVersion, m_serverVersion);
		}
	}
	
	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		super.readJson(factory, json);
		
		m_path = m_requestPathMngr.getFromJson(json);
		
		Integer serverVersion = factory.getHelper().getInt(json, smE_JsonKey.serverVersion);
		
		m_serverVersion = serverVersion;
	}
}