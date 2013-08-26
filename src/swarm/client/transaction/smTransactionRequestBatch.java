package swarm.client.transaction;

import java.util.ArrayList;

import swarm.shared.app.sm;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonEncodable;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonQuery;
import swarm.shared.json.smJsonHelper;
import swarm.shared.transaction.smE_HttpMethod;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_ReservedRequestPath;
import swarm.shared.transaction.smI_RequestPath;
import swarm.shared.transaction.smTransactionRequest;
import com.google.gwt.http.client.RequestBuilder;


/**
 * ...
 * @author 
 */
public class smTransactionRequestBatch extends bhTransactionRequest
{	
	private final ArrayList<smTransactionRequest> m_requestList = new ArrayList<smTransactionRequest>();
	
	public smTransactionRequestBatch()
	{
		super(smE_ReservedRequestPath.batch);
	}
	
	public smTransactionRequestBatch(Object nativeRequest)
	{
		super(nativeRequest, smE_ReservedRequestPath.batch);
	}
	
	public int getSize()
	{
		return m_requestList.size();
	}
	
	public smTransactionRequest getRequest(int index)
	{
		return m_requestList.get(index);
	}
	
	public void addRequest(smTransactionRequest request)
	{
		m_requestList.add(request);
		
		if( request.getMethod() == smE_HttpMethod.POST )
		{
			this.m_method = smE_HttpMethod.POST;
		}
	}
	
	public smTransactionRequest getRequest(smI_RequestPath path, smJsonQuery jsonQuery)
	{
		for( int i = m_requestList.size()-1; i >= 0; i-- )
		{
			bhTransactionRequest ithRequest =  m_requestList.get(i);
			
			if( ithRequest.isCancelled() )  continue;
			
			boolean match = false;
			
			if( ithRequest.getPath() == path )
			{
				if( jsonQuery != null )
				{
					if( jsonQuery.evaluate(ithRequest.getJson()) )
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
	
	public boolean cancelRequestsByPath(smI_RequestPath path, smTransactionRequest exclusion_nullable)
	{
		int cancelCount = 0;
		
		for( int i = m_requestList.size()-1; i >= 0; i-- )
		{
			bhTransactionRequest request = m_requestList.get(i);
			
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
	public void onDispatch(long timeInMilliseconds)
	{
		for( int i = 0; i < m_requestList.size(); i++ )
		{
			m_requestList.get(i).onDispatch(timeInMilliseconds);
		}
	}
	
	@Override
	public void writeJson(smI_JsonObject json)
	{
		super.writeJson(json);
		
		final smI_JsonArray requestList = sm.jsonFactory.createJsonArray();
		
		for ( int i = 0; i < m_requestList.size(); i++ )
		{
			bhTransactionRequest ithRequest = m_requestList.get(i);
			
			//--- DRK > Request can be cancelled by a synchronous request dispatcher.
			if( !ithRequest.isCancelled() )
			{
				requestList.addObject(ithRequest.writeJson());
			}
		}
		
		sm.jsonFactory.getHelper().putJsonArray(json, smE_JsonKey.requestList, requestList);
	}
}