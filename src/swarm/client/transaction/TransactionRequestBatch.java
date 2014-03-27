package swarm.client.transaction;

import java.util.ArrayList;
import java.util.logging.Logger;

import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_ReadsJson;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonQuery;
import swarm.shared.json.JsonHelper;
import swarm.shared.transaction.E_HttpMethod;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.E_ReservedRequestPath;
import swarm.shared.transaction.I_RequestPath;
import swarm.shared.transaction.RequestPathManager;
import swarm.shared.transaction.TransactionRequest;

import com.google.gwt.http.client.RequestBuilder;


/**
 * ...
 * @author 
 */
public class TransactionRequestBatch extends TransactionRequest
{
	private static final Logger s_logger = Logger.getLogger(TransactionRequestBatch.class.getName());
	
	private final ArrayList<TransactionRequest> m_requestList = new ArrayList<TransactionRequest>();
	
	public TransactionRequestBatch(A_JsonFactory jsonFactory)
	{
		super(jsonFactory, E_ReservedRequestPath.batch);
	}
	
	public TransactionRequestBatch(A_JsonFactory jsonFactory, Object nativeRequest)
	{
		super(jsonFactory, E_ReservedRequestPath.batch, nativeRequest);
	}
	
	public int getSize()
	{
		return m_requestList.size();
	}
	
	public TransactionRequest getRequest(int index)
	{
		return m_requestList.get(index);
	}
	
	public void addRequest(TransactionRequest request)
	{
		m_requestList.add(request);
		
		if( request.getMethod() == E_HttpMethod.POST )
		{
			this.m_method = E_HttpMethod.POST;
		}
	}
	
	public TransactionRequest getRequest(I_RequestPath path, A_JsonFactory jsonFactory, JsonQuery jsonQuery)
	{
		for( int i = m_requestList.size()-1; i >= 0; i-- )
		{
			TransactionRequest ithRequest =  m_requestList.get(i);
			
			if( ithRequest.isCancelled() )  continue;
			
			boolean match = false;
			
			if( ithRequest.getPath() == path )
			{
				if( jsonQuery != null )
				{
					if( jsonQuery.evaluate(jsonFactory, ithRequest.getJsonArgs()) )
					{
						match = true;
					}
				}
				else
				{
					match = true;
				}
			}
			
			if( match )
			{
				return ithRequest;
			}
		}
		
		return null;
	}
	
	public boolean cancelRequestsByPath(I_RequestPath path, TransactionRequest exclusion_nullable)
	{
		int cancelCount = 0;
		
		for( int i = m_requestList.size()-1; i >= 0; i-- )
		{
			TransactionRequest request = m_requestList.get(i);
			
			if( request.getPath() == path && request != exclusion_nullable )
			{
				if( !request.isCancelled() )
				{
					request.cancel();
				}
			}
			
			//--- DRK > This check takes into account the fact that the request could
			//---		have been cancelled earlier than this method invocation.
			if( request.isCancelled() )
			{
				cancelCount++;
			}
		}
		
		return cancelCount == m_requestList.size();
	}
	
	@Override
	public void onDispatch(long timeInMilliseconds, int libServerVersion, int appServerVersion)
	{
		super.onDispatch(timeInMilliseconds, libServerVersion, appServerVersion);
		
		for( int i = 0; i < m_requestList.size(); i++ )
		{
			TransactionRequest ithRequest = m_requestList.get(i);
			
			ithRequest.onDispatch(timeInMilliseconds);
		}
	}
	
	@Override
	public void writeJson(A_JsonFactory factory, RequestPathManager requestPathMngr, I_JsonObject json_out)
	{
		super.writeJson(factory, requestPathMngr, json_out);
		
		final I_JsonArray requestList = factory.createJsonArray();
		
		for ( int i = 0; i < m_requestList.size(); i++ )
		{
			TransactionRequest ithRequest = m_requestList.get(i);
			
			//--- DRK > Request can be cancelled by a synchronous request dispatcher.
			if( !ithRequest.isCancelled() )
			{
				I_JsonObject requestJson = factory.createJsonObject();
				ithRequest.writeJson(factory, requestPathMngr, requestJson);
				requestList.addObject(requestJson);
			}
			else
			{
				m_requestList.remove(i);
				i--;
			}
		}
		
		 // DRK > Can't think of a reason this would be false...just in case.
		if( requestList.getSize() > 0 )
		{
			factory.getHelper().putJsonArray(json_out, E_JsonKey.requestList, requestList);
		}
		else
		{
			s_logger.severe("Expected at least one request in batch."); // Most likely indicates problem in library somewhere.
		}
	}
}