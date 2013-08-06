package b33hive.client.transaction;

import java.util.ArrayList;

import b33hive.shared.app.bh;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.json.bhI_JsonEncodable;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonQuery;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.transaction.bhE_HttpMethod;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhE_ReservedRequestPath;
import b33hive.shared.transaction.bhI_RequestPath;
import b33hive.shared.transaction.bhTransactionRequest;
import com.google.gwt.http.client.RequestBuilder;


/**
 * ...
 * @author 
 */
public class bhTransactionRequestBatch extends bhTransactionRequest
{	
	private final ArrayList<bhTransactionRequest> m_requestList = new ArrayList<bhTransactionRequest>();
	
	public bhTransactionRequestBatch()
	{
		super(bhE_ReservedRequestPath.batch);
	}
	
	public bhTransactionRequestBatch(Object nativeRequest)
	{
		super(nativeRequest, bhE_ReservedRequestPath.batch);
	}
	
	public int getSize()
	{
		return m_requestList.size();
	}
	
	public bhTransactionRequest getRequest(int index)
	{
		return m_requestList.get(index);
	}
	
	public void addRequest(bhTransactionRequest request)
	{
		m_requestList.add(request);
		
		if( request.getMethod() == bhE_HttpMethod.POST )
		{
			this.m_method = bhE_HttpMethod.POST;
		}
	}
	
	public bhTransactionRequest getRequest(bhI_RequestPath path, bhJsonQuery jsonQuery)
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
	
	public boolean cancelRequestsByPath(bhI_RequestPath path, bhTransactionRequest exclusion_nullable)
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
	public void writeJson(bhI_JsonObject json)
	{
		super.writeJson(json);
		
		final bhI_JsonArray requestList = bh.jsonFactory.createJsonArray();
		
		for ( int i = 0; i < m_requestList.size(); i++ )
		{
			bhTransactionRequest ithRequest = m_requestList.get(i);
			
			//--- DRK > Request can be cancelled by a synchronous request dispatcher.
			if( !ithRequest.isCancelled() )
			{
				requestList.addObject(ithRequest.writeJson());
			}
		}
		
		bh.jsonFactory.getHelper().putJsonArray(json, bhE_JsonKey.requestList, requestList);
	}
}