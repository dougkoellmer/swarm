package swarm.client.transaction;

import java.util.ArrayList;

import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_ReadsJson;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonQuery;
import swarm.shared.json.smJsonHelper;
import swarm.shared.transaction.smE_HttpMethod;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_ReservedRequestPath;
import swarm.shared.transaction.smI_RequestPath;
import swarm.shared.transaction.smRequestPathManager;
import swarm.shared.transaction.smTransactionRequest;
import com.google.gwt.http.client.RequestBuilder;


/**
 * ...
 * @author 
 */
public class smTransactionRequestBatch extends smTransactionRequest
{	
	private final ArrayList<smTransactionRequest> m_requestList = new ArrayList<smTransactionRequest>();
	
	public smTransactionRequestBatch(smA_JsonFactory jsonFactory)
	{
		super(jsonFactory, smE_ReservedRequestPath.batch);
	}
	
	public smTransactionRequestBatch(smA_JsonFactory jsonFactory, Object nativeRequest)
	{
		super(jsonFactory, smE_ReservedRequestPath.batch, nativeRequest);
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
	
	public smTransactionRequest getRequest(smI_RequestPath path, smA_JsonFactory jsonFactory, smJsonQuery jsonQuery)
	{
		for( int i = m_requestList.size()-1; i >= 0; i-- )
		{
			smTransactionRequest ithRequest =  m_requestList.get(i);
			
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
	
	public boolean cancelRequestsByPath(smI_RequestPath path, smTransactionRequest exclusion_nullable)
	{
		int cancelCount = 0;
		
		for( int i = m_requestList.size()-1; i >= 0; i-- )
		{
			smTransactionRequest request = m_requestList.get(i);
			
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
	public void onDispatch(long timeInMilliseconds, int serverVersion)
	{
		super.onDispatch(timeInMilliseconds, serverVersion);
		
		for( int i = 0; i < m_requestList.size(); i++ )
		{
			smTransactionRequest ithRequest = m_requestList.get(i);
			
			ithRequest.onDispatch(timeInMilliseconds);
		}
	}
	
	@Override
	public void writeJson(smA_JsonFactory factory, smRequestPathManager requestPathMngr, smI_JsonObject json_out)
	{
		super.writeJson(factory, json_out);
		
		final smI_JsonArray requestList = factory.createJsonArray();
		
		for ( int i = 0; i < m_requestList.size(); i++ )
		{
			smTransactionRequest ithRequest = m_requestList.get(i);
			
			//--- DRK > Request can be cancelled by a synchronous request dispatcher.
			if( !ithRequest.isCancelled() )
			{
				smI_JsonObject requestJson = factory.createJsonObject();
				ithRequest.writeJson(factory, requestPathMngr, requestJson);
				requestList.addObject(requestJson);
			}
		}
		
		factory.getHelper().putJsonArray(json_out, smE_JsonKey.requestList, requestList);
	}
}