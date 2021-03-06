package swarm.shared.transaction;

import swarm.shared.app.BaseAppContext;
import swarm.shared.app.A_App;
import swarm.shared.app.S_CommonApp;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

import com.google.gwt.http.client.RequestBuilder;


/**
 * ...
 * @author 
 */
public class TransactionRequest extends A_TransactionObject
{
	private long m_dispatchTime = 0;
	private I_RequestPath m_path = null;
	private int m_retryCount = 0;
	protected E_HttpMethod m_method;
	private boolean m_isCancelled = false;
	protected Integer m_libServerVersion = null;
	protected Integer m_appServerVersion = null;
	
	public TransactionRequest(A_JsonFactory jsonFactory, I_RequestPath path)
	{
		super(jsonFactory);

		initPath(path);
	}
	
	public TransactionRequest(A_JsonFactory jsonFactory, I_RequestPath path, Object nativeRequest)
	{
		super(jsonFactory, nativeRequest);
		
		initPath(path);
	}
	
	public TransactionRequest(A_JsonFactory jsonFactory, Object nativeRequest)
	{
		super(jsonFactory, nativeRequest);
	}
	
	public TransactionRequest(A_JsonFactory jsonFactory)
	{
		super(jsonFactory);
	}
	
	public long getDispatchTime()
	{
		return m_dispatchTime;
	}
	
	private void initPath(I_RequestPath path)
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
	
	public Integer getLibServerVersion()
	{
		return m_libServerVersion;
	}
	
	public Integer getAppServerVersion()
	{
		return m_appServerVersion;
	}
	
	public E_HttpMethod getMethod()
	{
		return m_method;
	}
	
	public I_RequestPath getPath()
	{
		return m_path;
	}
	
	public void onDispatch(long timeInMilliseconds)
	{
		m_dispatchTime = timeInMilliseconds;
		m_libServerVersion = null;
		m_appServerVersion = null;
	}
	
	public void onDispatch(long timeInMilliseconds, int libServerVersion, int appServerVersion)
	{
		m_dispatchTime = timeInMilliseconds;
		m_libServerVersion = libServerVersion;
		m_appServerVersion = appServerVersion;
	}
	
	@Override
	public boolean isEqualTo(A_TransactionObject otherObject)
	{
		if( otherObject instanceof TransactionRequest )
		{
			if( this.getPath() != ((TransactionRequest)otherObject).getPath() )
			{
				return false;
			}
		}

		return super.isEqualTo(otherObject);
	}
	
	public void writeJson(A_JsonFactory factory, RequestPathManager requestPathMngr, I_JsonObject json_out)
	{
		super.writeJson(factory, json_out);
		
		requestPathMngr.putToJson(m_path, json_out);
		
		if( m_libServerVersion != null )
		{
			factory.getHelper().putInt(json_out, E_JsonKey.libVersion, m_libServerVersion);
		}
		
		if( m_appServerVersion != null )
		{
			factory.getHelper().putInt(json_out, E_JsonKey.appVersion, m_appServerVersion);
		}
	}
	
	public void readJson(A_JsonFactory factory, RequestPathManager requestPathMngr, I_JsonObject json)
	{
		super.readJson(factory, json);
		
		m_path = requestPathMngr.getFromJson(json);
		
		Integer libServerVersion = factory.getHelper().getInt(json, E_JsonKey.libVersion);
		Integer appServerVersion = factory.getHelper().getInt(json, E_JsonKey.appVersion);
		
		m_libServerVersion = libServerVersion;
		m_appServerVersion = appServerVersion;
	}
	
	@Override
	public String toString()
	{
		return m_path.getName() + " " + super.toString();
	}
}