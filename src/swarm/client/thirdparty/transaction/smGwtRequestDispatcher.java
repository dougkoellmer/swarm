package swarm.client.thirdparty.transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import swarm.client.transaction.smClientTransactionManager;
import swarm.client.transaction.smI_AsyncRequestDispatcher;
import swarm.client.transaction.smI_ResponseCallbacks;
import swarm.client.transaction.smTransactionRequestBatch;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.app.smS_App;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonQuery;
import swarm.shared.json.smJsonHelper;
import swarm.shared.transaction.smE_HttpMethod;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smI_RequestPath;
import swarm.shared.transaction.smS_Transaction;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

public class smGwtRequestDispatcher implements smI_AsyncRequestDispatcher, RequestCallback
{
	private static final Logger s_logger = Logger.getLogger(smGwtRequestDispatcher.class.getName());
	
	final HashMap<Request, smTransactionRequest> m_nativeRequestMap = new HashMap<Request, smTransactionRequest>();
	
	private final smTransactionResponse m_reusedResponse;
	
	private smI_ResponseCallbacks m_callbacks = null;
	private int m_maxGetUrlLength;
	private final smA_JsonFactory m_jsonFactory;
	
	public smGwtRequestDispatcher(smA_JsonFactory jsonFactory)
	{
		m_jsonFactory = jsonFactory;
		m_reusedResponse = new smTransactionResponse(m_jsonFactory);
	}
	
	@Override
	public void initialize(smI_ResponseCallbacks callbacks, int maxGetUrlLength)
	{
		m_maxGetUrlLength = maxGetUrlLength;
		m_callbacks = callbacks;
	}
	
	@Override
	public boolean dispatch(smTransactionRequest request)
	{
		String baseUrl = "/r.t";
		String url = baseUrl;
		smI_JsonObject requestJson = m_jsonFactory.createJsonObject();
		request.writeJson(m_jsonFactory, requestJson);
		String jsonString = requestJson.toString();
		
		RequestBuilder.Method method = request.getMethod() == smE_HttpMethod.GET ? RequestBuilder.GET  : RequestBuilder.POST;
		boolean isGet = method == RequestBuilder.GET;
		
		if( isGet )
		{
			String jsonEncoded = URL.encodeQueryString(jsonString);
			url += "?"+smS_Transaction.JSON_URL_PARAM+"=" + jsonEncoded;
			url = URL.encode(url);
			
			//--- DRK > Fringe case precaution that we don't exceed practical URL length restrictions.
			//---		In this case, we change the request to a post.
			if( url.length() >= m_maxGetUrlLength )
			{
				url = URL.encode(baseUrl);
				method = RequestBuilder.POST;
				isGet = false;
			}
		}
		else
		{
			url = URL.encode(baseUrl);
		}
	
		RequestBuilder builder = new RequestBuilder(method, url);

		try
		{
			builder.setHeader("Content-Type", "application/json");
			
			Request nativeRequest = builder.sendRequest(isGet ? null : jsonString, this);
			
			m_nativeRequestMap.put(nativeRequest, request);
		}
		catch (RequestException e)
		{
			s_logger.severe(e.toString());
			
			return false;
		}
		
		return true;
	}
	
	@Override
	public void onResponseReceived(Request nativeRequest, Response nativeResponse)
	{
		smTransactionRequest request = this.getDispatchedRequest(nativeRequest);
		m_reusedResponse.clear();
		int statusCode = nativeResponse.getStatusCode();
		
		if( statusCode != Response.SC_OK )
		{
			String statusText = nativeResponse.getStatusText();
			m_reusedResponse.setError(smE_ResponseError.HTTP_ERROR);
			
			this.onError(nativeRequest, request, m_reusedResponse);
			
			return;
		}
		
		smI_JsonObject responseJson = m_jsonFactory.createJsonObject(nativeResponse.getText());
		m_reusedResponse.readJson(m_jsonFactory, responseJson);
		
		if( !(request instanceof smTransactionRequestBatch) )
		{
			m_callbacks.onResponseReceived(request, m_reusedResponse);
		}
		else
		{
			if( m_reusedResponse.getError() != smE_ResponseError.NO_ERROR )
			{
				this.onError(nativeRequest, request, m_reusedResponse);
				
				return;
			}
			
			smTransactionRequestBatch batch = (smTransactionRequestBatch) request;

			smI_JsonArray responseList = m_jsonFactory.getHelper().getJsonArray(responseJson, smE_JsonKey.responseList);
			
			m_callbacks.onResponseReceived(batch, responseList);
		}
		
		this.m_nativeRequestMap.remove(nativeRequest);
	}

	/**
	 * As far as I know, this can only mean that the request couldn't even be sent out, maybe due to no connection or something.
	 * I'm guessing it should have nothing to do with an error received from any internet...it's never been hit though.
	 */
	@Override
	public void onError(Request nativeRequest, Throwable exception)
	{
		smTransactionRequest request = this.getDispatchedRequest(nativeRequest);
		
		m_reusedResponse.clear();
		m_reusedResponse.setError(smE_ResponseError.CLIENT_ERROR);
		
		onError(nativeRequest, request, m_reusedResponse);
	}
	
	private void onError(Request nativeRequest, smTransactionRequest request, smTransactionResponse response)
	{
		m_callbacks.onError(request, response);
		
		m_nativeRequestMap.remove(nativeRequest);
	}
	
	private smTransactionRequest getDispatchedRequest(Request nativeRequest)
	{
		if( this.m_nativeRequestMap.containsKey(nativeRequest) )
		{
			return this.m_nativeRequestMap.get(nativeRequest);
		}
		
		smU_Debug.ASSERT(false, "getRequest1");
		
		return null;
	}
	
	@Override
	public smTransactionRequest getDispatchedRequest(smI_RequestPath path, smJsonQuery jsonQuery, smTransactionRequest exclusion_nullable)
	{
		for (Request nativeRequest : m_nativeRequestMap.keySet())
		{
			smTransactionRequest request = m_nativeRequestMap.get(nativeRequest);
			
			if( request instanceof smTransactionRequestBatch )
			{
				smTransactionRequestBatch batch = ((smTransactionRequestBatch) request);
				
				smTransactionRequest foundRequest = batch.getRequest(path, m_jsonFactory, jsonQuery);
				
				if( foundRequest != null && foundRequest != exclusion_nullable )
				{
					return foundRequest;
				}
			}
			else
			{
				if( request.getPath() == path && request != exclusion_nullable )
				{
					if( jsonQuery != null )
					{
						if( jsonQuery.evaluate(m_jsonFactory, request.getJsonArgs()) )
						{
							return request;
						}
					}
					else
					{
						return request;
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void cancelRequestsByPath(smI_RequestPath path, smTransactionRequest exclusion_nullable )
	{
		Iterator<Request> iterator = m_nativeRequestMap.keySet().iterator();
		
		while ( iterator.hasNext() )
		{
			Request nativeRequest = iterator.next();
			smTransactionRequest request = m_nativeRequestMap.get(nativeRequest);
			
			boolean cancel = false;
			
			if( request instanceof smTransactionRequestBatch )
			{
				smTransactionRequestBatch batch = ((smTransactionRequestBatch) request);
				
				if( batch.cancelRequestsByPath(path, exclusion_nullable) )
				{
					cancel = true;
				}
			}
			else
			{
				if( request.getPath() == path && request != exclusion_nullable )
				{
					cancel = true;
				}
			}
			
			if( cancel )
			{
				nativeRequest.cancel();
				request.cancel();
				iterator.remove();
			}
		}
	}
}
