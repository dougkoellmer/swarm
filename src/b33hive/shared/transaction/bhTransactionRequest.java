package b33hive.shared.transaction;

import b33hive.shared.app.bhA_App;
import b33hive.shared.app.bhS_App;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;
import com.google.gwt.http.client.RequestBuilder;


/**
 * ...
 * @author 
 */
public class bhTransactionRequest extends bhA_TransactionObject
{
	private long m_dispatchTime = 0;
	private bhI_RequestPath m_path = null;
	private int m_retryCount = 0;
	protected bhE_HttpMethod m_method;
	private boolean m_isCancelled = false;

	protected Integer m_serverVersion = null;
	
	public bhTransactionRequest(bhI_RequestPath path, Object nativeRequest)
	{
		super(nativeRequest);
		
		initPath(path);
	}
	
	public bhTransactionRequest(Object nativeRequest)
	{
		super(nativeRequest);
	}
	
	public bhTransactionRequest()
	{
		super(null);
	}
	
	public long getDispatchTime()
	{
		return m_dispatchTime;
	}
	
	public bhTransactionRequest(bhI_RequestPath path) 
	{
		super(null);

		initPath(path);
	}
	
	public bhTransactionRequest(bhI_RequestPath path, bhI_JsonObject jsonArgs) 
	{
		super(jsonArgs);

		initPath(path);
	}
	
	private void initPath(bhI_RequestPath path)
	{
		m_path = path;
		m_method = path.getDefaultMethod();
	}
	
	public bhTransactionRequest(Object nativeRequest, bhI_RequestPath path) 
	{
		super(nativeRequest);
		
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
	
	public bhE_HttpMethod getMethod()
	{
		return m_method;
	}
	
	public void setPath(bhI_RequestPath path)
	{
		m_path = path;
	}
	
	public bhI_RequestPath getPath()
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
	public boolean isEqualTo(bhA_TransactionObject otherObject)
	{
		if( otherObject instanceof bhTransactionRequest )
		{
			if( this.getPath() != ((bhTransactionRequest)otherObject).getPath() )
			{
				return false;
			}
		}

		return super.isEqualTo(otherObject);
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		super.writeJson(json);
		
		bhRequestPathManager.getInstance().putToJson(json, m_path);
		
		if( m_serverVersion != null )
		{
			bhJsonHelper.getInstance().putInt(json, bhE_JsonKey.serverVersion, m_serverVersion);
		}
	}
	
	@Override
	public void readJson(bhI_JsonObject json)
	{
		super.readJson(json);
		
		m_path = bhRequestPathManager.getInstance().getFromJson(json);
		
		Integer serverVersion = bhJsonHelper.getInstance().getInt(json, bhE_JsonKey.serverVersion);
		
		m_serverVersion = serverVersion;
	}
}