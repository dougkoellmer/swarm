package swarm.client.thirdparty.transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import swarm.client.transaction.ClientTransactionManager;
import swarm.client.transaction.I_AsyncRequestDispatcher;
import swarm.client.transaction.I_ResponseCallbacks;
import swarm.client.transaction.TransactionRequestBatch;
import swarm.shared.app.BaseAppContext;
import swarm.shared.app.S_CommonApp;
import swarm.shared.debugging.U_Debug;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonQuery;
import swarm.shared.json.JsonHelper;
import swarm.shared.transaction.E_HttpMethod;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.I_RequestPath;
import swarm.shared.transaction.RequestPathManager;
import swarm.shared.transaction.S_Transaction;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

public class GwtRequestDispatcher implements I_AsyncRequestDispatcher, RequestCallback
{
	private static final Logger s_logger = Logger.getLogger(GwtRequestDispatcher.class.getName());
	
	final HashMap<Request, TransactionRequest> m_nativeRequestMap = new HashMap<Request, TransactionRequest>();
	
	private final TransactionResponse m_reusedResponse;
	
	private I_ResponseCallbacks m_callbacks = null;
	private int m_maxGetUrlLength;
	private final A_JsonFactory m_jsonFactory;
	private final RequestPathManager m_requestPathMngr;
	
	public GwtRequestDispatcher(A_JsonFactory jsonFactory, RequestPathManager requestPathMngr)
	{
		m_jsonFactory = jsonFactory;
		m_requestPathMngr = requestPathMngr;
		m_reusedResponse = new TransactionResponse(m_jsonFactory);
	}
	
	@Override
	public void initialize(I_ResponseCallbacks callbacks, int maxGetUrlLength)
	{
		m_maxGetUrlLength = maxGetUrlLength;
		m_callbacks = callbacks;
	}
	
	@Override
	public boolean dispatch(TransactionRequest request)
	{
		String baseUrl = "/r.t";
		String url = baseUrl;
		I_JsonObject requestJson = m_jsonFactory.createJsonObject();
		request.writeJson(m_jsonFactory, m_requestPathMngr, requestJson);
		String jsonString = requestJson.toString();
		
		RequestBuilder.Method method = request.getMethod() == E_HttpMethod.GET ? RequestBuilder.GET  : RequestBuilder.POST;
		boolean isGet = method == RequestBuilder.GET;
		
		if( isGet )
		{
			String jsonEncoded = URL.encodeQueryString(jsonString);
			url += "?"+S_Transaction.JSON_URL_PARAM+"=" + jsonEncoded;
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
		TransactionRequest request = this.getDispatchedRequest(nativeRequest);
		m_reusedResponse.clear();
		int statusCode = nativeResponse.getStatusCode();
		
		if( statusCode != Response.SC_OK )
		{
			String statusText = nativeResponse.getStatusText();
			m_reusedResponse.setError(E_ResponseError.HTTP_ERROR);
			
			this.onError(nativeRequest, request, m_reusedResponse);
			
			return;
		}
		
		I_JsonObject responseJson = m_jsonFactory.createJsonObject(nativeResponse.getText());
		m_reusedResponse.readJson(m_jsonFactory, responseJson);
		
		if( !(request instanceof TransactionRequestBatch) )
		{
			m_callbacks.onResponseReceived(request, m_reusedResponse);
		}
		else
		{
			if( m_reusedResponse.getError() != E_ResponseError.NO_ERROR )
			{
				this.onError(nativeRequest, request, m_reusedResponse);
				
				return;
			}

			TransactionRequestBatch batch = (TransactionRequestBatch) request;

			I_JsonArray responseList = m_jsonFactory.getHelper().getJsonArray(responseJson, E_JsonKey.responseList);
			
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
		TransactionRequest request = this.getDispatchedRequest(nativeRequest);
		
		m_reusedResponse.clear();
		m_reusedResponse.setError(E_ResponseError.CLIENT_ERROR);
		
		onError(nativeRequest, request, m_reusedResponse);
	}
	
	private void onError(Request nativeRequest, TransactionRequest request, TransactionResponse response)
	{
		m_callbacks.onError(request, response);
		
		m_nativeRequestMap.remove(nativeRequest);
	}
	
	private TransactionRequest getDispatchedRequest(Request nativeRequest)
	{
		if( this.m_nativeRequestMap.containsKey(nativeRequest) )
		{
			return this.m_nativeRequestMap.get(nativeRequest);
		}
		
		U_Debug.ASSERT(false, "getRequest1");
		
		return null;
	}
	
	@Override
	public TransactionRequest getDispatchedRequest(I_RequestPath path, JsonQuery jsonQuery, TransactionRequest exclusion_nullable)
	{
		for (Request nativeRequest : m_nativeRequestMap.keySet())
		{
			TransactionRequest request = m_nativeRequestMap.get(nativeRequest);
			
			if( request instanceof TransactionRequestBatch )
			{
				TransactionRequestBatch batch = ((TransactionRequestBatch) request);
				
				TransactionRequest foundRequest = batch.getRequest(path, m_jsonFactory, jsonQuery);
				
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
	public void cancelRequestsByPath(I_RequestPath path, TransactionRequest exclusion_nullable )
	{
		Iterator<Request> iterator = m_nativeRequestMap.keySet().iterator();
		
		while ( iterator.hasNext() )
		{
			Request nativeRequest = iterator.next();
			TransactionRequest request = m_nativeRequestMap.get(nativeRequest);
			
			boolean cancel = false;
			
			if( request instanceof TransactionRequestBatch )
			{
				TransactionRequestBatch batch = ((TransactionRequestBatch) request);
				
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
