package b33hive.client.thirdparty.transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import b33hive.client.transaction.bhClientTransactionManager;
import b33hive.client.transaction.bhI_AsynchronousRequestDispatcher;
import b33hive.client.transaction.bhI_ResponseCallbacks;
import b33hive.client.transaction.bhTransactionRequestBatch;
import b33hive.shared.app.bh;
import b33hive.shared.app.bhS_App;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonQuery;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.transaction.bhE_HttpMethod;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhI_RequestPath;
import b33hive.shared.transaction.bhS_Transaction;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

public class bhGwtRequestDispatcher implements bhI_AsynchronousRequestDispatcher, RequestCallback
{
	private static final Logger s_logger = Logger.getLogger(bhGwtRequestDispatcher.class.getName());
	
	final HashMap<Request, bhTransactionRequest> m_nativeRequestMap = new HashMap<Request, bhTransactionRequest>();
	
	private final bhTransactionResponse m_reusedResponse = new bhTransactionResponse();
	
	private bhI_ResponseCallbacks m_callbacks = null;
	private int m_maxGetUrlLength;
	
	@Override
	public void initialize(bhI_ResponseCallbacks callbacks, int maxGetUrlLength)
	{
		m_maxGetUrlLength = maxGetUrlLength;
		m_callbacks = callbacks;
	}
	
	@Override
	public boolean dispatch(bhTransactionRequest request)
	{
		String baseUrl = GWT.getModuleBaseURL() + "t";
		String url = baseUrl;
		String jsonString = request.writeJson().toString();
		
		RequestBuilder.Method method = request.getMethod() == bhE_HttpMethod.GET ? RequestBuilder.GET  : RequestBuilder.POST;
		boolean isGet = method == RequestBuilder.GET;
		
		if( isGet )
		{
			String jsonEncoded = URL.encodeQueryString(jsonString);
			url += "?"+bhS_Transaction.JSON_URL_PARAM+"=" + jsonEncoded;
			url = URL.encode(url);
			
			//--- DRK > Fringe case precaution that we don't exceed practical URL length restrictions.
			//---		This could happen if someone had a huge monitor I guess, and was scrolling through tons
			//---		of cells at one time.  In this case, we change the request to a post.
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
		bhTransactionRequest request = this.getDispatchedRequest(nativeRequest);
		m_reusedResponse.reset();
		int statusCode = nativeResponse.getStatusCode();
		
		if( statusCode != Response.SC_OK )
		{
			String statusText = nativeResponse.getStatusText();
			m_reusedResponse.setError(bhE_ResponseError.HTTP_ERROR);
			
			this.onError(nativeRequest, request, m_reusedResponse);
			
			return;
		}
		
		bhI_JsonObject responseJson = bh.jsonFactory.createJsonObject(nativeResponse.getText());
		m_reusedResponse.readJson(responseJson);
		
		if( !(request instanceof bhTransactionRequestBatch) )
		{
			m_callbacks.onResponseReceived(request, m_reusedResponse);
		}
		else
		{
			if( m_reusedResponse.getError() != bhE_ResponseError.NO_ERROR )
			{
				this.onError(nativeRequest, request, m_reusedResponse);
				
				return;
			}
			
			bhTransactionRequestBatch batch = (bhTransactionRequestBatch) request;

			bhI_JsonArray responseList = bh.jsonFactory.getHelper().getJsonArray(responseJson, bhE_JsonKey.responseList);
			
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
		bhTransactionRequest request = this.getDispatchedRequest(nativeRequest);
		
		m_reusedResponse.reset();
		m_reusedResponse.setError(bhE_ResponseError.CLIENT_ERROR);
		
		onError(nativeRequest, request, m_reusedResponse);
	}
	
	private void onError(Request nativeRequest, bhTransactionRequest request, bhTransactionResponse response)
	{
		m_callbacks.onError(request, response);
		
		m_nativeRequestMap.remove(nativeRequest);
	}
	
	private bhTransactionRequest getDispatchedRequest(Request nativeRequest)
	{
		if( this.m_nativeRequestMap.containsKey(nativeRequest) )
		{
			return this.m_nativeRequestMap.get(nativeRequest);
		}
		
		bhU_Debug.ASSERT(false, "getRequest1");
		
		return null;
	}
	
	@Override
	public bhTransactionRequest getDispatchedRequest(bhI_RequestPath path, bhJsonQuery jsonQuery, bhTransactionRequest exclusion_nullable)
	{
		for (Request nativeRequest : m_nativeRequestMap.keySet())
		{
			bhTransactionRequest request = m_nativeRequestMap.get(nativeRequest);
			
			if( request instanceof bhTransactionRequestBatch )
			{
				bhTransactionRequestBatch batch = ((bhTransactionRequestBatch) request);
				
				bhTransactionRequest foundRequest = batch.getRequest(path, jsonQuery);
				
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
						if( jsonQuery.evaluate(request.getJson()) )
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
	public void cancelRequestsByPath(bhI_RequestPath path, bhTransactionRequest exclusion_nullable )
	{
		Iterator<Request> iterator = m_nativeRequestMap.keySet().iterator();
		
		while ( iterator.hasNext() )
		{
			Request nativeRequest = iterator.next();
			bhTransactionRequest request = m_nativeRequestMap.get(nativeRequest);
			
			boolean cancel = false;
			
			if( request instanceof bhTransactionRequestBatch )
			{
				bhTransactionRequestBatch batch = ((bhTransactionRequestBatch) request);
				
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
