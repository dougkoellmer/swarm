// Copyright 2011, Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package b33hive.server.transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import b33hive.server.account.bhAccountDatabase;
import b33hive.server.app.bhA_ServerJsonFactory;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.session.bhSessionManager;
import b33hive.server.telemetry.bhTelemetryDatabase;
import b33hive.shared.app.bhA_App;
import b33hive.shared.app.bhS_App;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonEncodable;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonQuery;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.reflection.bhI_Class;
import b33hive.shared.structs.bhOptHashMap;
import b33hive.shared.transaction.bhA_TransactionObject;
import b33hive.shared.transaction.bhCachePolicy;
import b33hive.shared.transaction.bhE_ReservedRequestPath;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhI_RequestPath;
import b33hive.shared.transaction.bhRequestPathManager;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;
import b33hive.shared.transaction.bhU_RequestBatch;

public class bhServerTransactionManager
{
	private static final Logger s_logger = Logger.getLogger(bhServerTransactionManager.class.getName());
	
	private final HashMap<Integer, bhI_RequestHandler> m_handlers = new HashMap<Integer, bhI_RequestHandler>();
	private final HashSet<Integer> m_debugResponseErrorPaths = new HashSet<Integer>();
	private final ArrayList<bhI_DeferredRequestHandler> m_deferredHandlers = new ArrayList<bhI_DeferredRequestHandler>();
	private final ArrayList<bhI_TransactionScopeListener> m_scopeListeners = new ArrayList<bhI_TransactionScopeListener>();
	private final bhA_ServerJsonFactory m_jsonFactory;
	
	public bhServerTransactionManager(bhA_ServerJsonFactory jsonFactory, bhRequestPathManager requestPathMngr)
	{
		m_jsonFactory = jsonFactory;
		
		requestPathMngr.register(bhE_ReservedRequestPath.values());
	}
	
	public bhI_RequestHandler getRequestHandler(bhI_RequestPath path)
	{
		return m_handlers.get(path.getId());
	}
	
	public void setRequestHandler(bhI_RequestHandler handler, bhI_RequestPath path)
	{
		m_handlers.put(path.getId(), handler);
	}
	
	public void setDebugResponseError(bhI_RequestPath path)
	{
		m_debugResponseErrorPaths.add(path.getId());
	}
	
	public void addDeferredHandler(bhI_DeferredRequestHandler handler)
	{
		m_deferredHandlers.add(handler);
	}
	
	public void addScopeListener(bhI_TransactionScopeListener listener)
	{
		m_scopeListeners.add(listener);
	}
	
	private void createEarlyOutResponse(bhE_ResponseError responseError, bhI_JsonObject responseJson)
	{
		bhTransactionResponse responseToReturn = new bhTransactionResponse();
		responseToReturn.setError(responseError);
		responseToReturn.writeJson(responseJson);
	}
	
	public void handleRequestFromClient(final Object nativeRequest, final Object nativeResponse, Object nativeContext, bhI_JsonObject requestJson, bhI_JsonObject responseJson_out, boolean verboseJson)
	{
		m_jsonFactory.startScope(verboseJson);
		
		//--- DRK > Early out for problems with request json.
		if( requestJson == null )
		{
			this.createEarlyOutResponse(bhE_ResponseError.REQUEST_READ_ERROR, responseJson_out);
			
			return;
		}
		
		bhTransactionResponse responseToReturn = null;
		
		//--- DRK > Just being anal and putting everything within a try.
		try
		{
			//--- DRK > Create a wrapper around the native request and see if there's a server version mismatch.
			bhTransactionRequest wrappedRequest = new bhTransactionRequest(nativeRequest);
			wrappedRequest.readJson(requestJson);
			Integer serverVersionAsFarAsClientKnows = wrappedRequest.getServerVersion();
			boolean serverVersionMismatch = false;
			
			if( serverVersionAsFarAsClientKnows != null && serverVersionAsFarAsClientKnows != bhS_App.SERVER_VERSION )
			{
				serverVersionMismatch = true;
			}
			
			//--- DRK > Early out for server version mismatches.
			if( serverVersionMismatch )
			{
				this.createEarlyOutResponse(bhE_ResponseError.VERSION_MISMATCH, responseJson_out);
				
				return;
			}
			
			if( wrappedRequest.getPath() == null )
			{
				this.createEarlyOutResponse(bhE_ResponseError.UNKNOWN_PATH, responseJson_out);
				
				return;
			}
			
			boolean isBatch = wrappedRequest.getPath() == bhE_ReservedRequestPath.batch;
			
			final bhTransactionContext context = new bhTransactionContext(isBatch, nativeContext);
			
			for( int i = 0; i < m_scopeListeners.size(); i++ )
			{
				m_scopeListeners.get(i).onEnterScope();
			}
	
			if( isBatch )
			{
				final bhTransactionResponseBatch responseBatch = new bhTransactionResponseBatch();
				responseToReturn = responseBatch;
				
				bhU_RequestBatch.I_JsonReadDelegate readDelegate = new bhU_RequestBatch.I_JsonReadDelegate()
				{
					@Override
					public void onRequestFound(bhI_JsonObject requestJson)
					{
						bhTransactionRequest batchedRequest = new bhTransactionRequest(nativeRequest);
						batchedRequest.readJson(requestJson);
						bhTransactionResponse batchedResponse = new bhTransactionResponse(nativeResponse);
						
						context.addTransaction(batchedRequest, batchedResponse);
					}
				};
				
				bhU_RequestBatch.readRequestList(requestJson, readDelegate);
				
				bhTransactionBatch transactionBatch = context.getBatch();
				
				int transactionCount = transactionBatch.getCount();
				if( transactionCount > 1 )
				{
					for( int i = 0; i < m_scopeListeners.size(); i++ )
					{
						m_scopeListeners.get(i).onBatchStart();
					}
				}
				
				for( int i = 0; i < transactionBatch.getCount(); i++ )
				{
					bhTransactionRequest batchedRequest = transactionBatch.getRequest(i);
					bhTransactionResponse batchedResponse = transactionBatch.getResponse(i);
					
					callRequestHandler(context, batchedRequest, batchedResponse);
					
					if( batchedResponse.getError() == bhE_ResponseError.DEFERRED )
					{
						context.queueDeferredTransaction(batchedRequest, batchedResponse);
					}
					
					responseBatch.addResponse(batchedResponse);
				}
				
				if( context.getDeferredCount() > 0 )
				{
					bhTransactionBatch deferredBatch = context.getDeferredBatch();
					
					for( int i = 0; i < m_deferredHandlers.size(); i++ )
					{
						try
						{
							m_deferredHandlers.get(i).handleDeferredRequests(context, deferredBatch);
						}
						catch(Exception e)
						{
							//--- DRK > Handlers should try their damnedest to not let exceptions get up to here, so this is bad...
							s_logger.log(Level.SEVERE, "Exception occurred while handling deferred requests.", e);

							deferredBatch.markUnhandledTransactions(bhE_ResponseError.HANDLER_EXCEPTION);
						}
						
						deferredBatch.removeHandledTransactions();
						
						if( deferredBatch.getCount() == 0 )
						{
							break;
						}
					}
					
					deferredBatch.markUnhandledTransactions(bhE_ResponseError.DEFERRED_BUT_NEVER_HANDLED);
				}
				
				if( transactionCount > 1 )
				{
					for( int i = 0; i < m_scopeListeners.size(); i++ )
					{
						m_scopeListeners.get(i).onBatchEnd();
					}
				}
			}
			else
			{
				responseToReturn = new bhTransactionResponse(nativeResponse);
				
				context.addTransaction(wrappedRequest, responseToReturn);
				
				callRequestHandler(context, wrappedRequest, responseToReturn);
				
				if( responseToReturn.getError() == bhE_ResponseError.DEFERRED )
				{
					responseToReturn.setError(bhE_ResponseError.DEFERRED_BUT_NEVER_HANDLED);
				}
			}
		}
		
		//--- DRK > Most likely means some problem with this class...unlikely, but catching everything just to be safe.
		catch(Throwable e)
		{
			s_logger.log(Level.SEVERE, "Transaction manager encountered problem while processing request for json: " + requestJson.writeString(), e);
			
			responseToReturn = null;
		}
		finally
		{
			if( responseToReturn == null )
			{
				responseToReturn = new bhTransactionResponse();
				responseToReturn.setError(bhE_ResponseError.SERVER_EXCEPTION);
			}
			
			responseToReturn.writeJson(responseJson_out);
			
			m_jsonFactory.endScope();
			
			for( int i = 0; i < m_scopeListeners.size(); i++ )
			{
				m_scopeListeners.get(i).onExitScope();
			}
		}
	}
	
	/**
	 * Probably mostly only for debug purposes, where you don't really care if the response succeeded or not.
	 * 
	 * @param request
	 */
	public void callRequestHandler(bhTransactionRequest request)
	{
		this.callRequestHandler(new bhTransactionContext(false, null), request, new bhTransactionResponse());
	}
	
	public void callRequestHandler(bhTransactionRequest request, bhTransactionResponse response)
	{
		this.callRequestHandler(new bhTransactionContext(false, null), request, response);
	}
	
	public void callRequestHandler(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		response.setError(bhE_ResponseError.NO_ERROR);
		
		bhI_RequestPath path = request.getPath();
		
		if( path == null )
		{
			s_logger.log(Level.WARNING, "Could not find a registered path.");
			
			response.setError(bhE_ResponseError.UNKNOWN_PATH);
			
			return;
		}
		
		if( m_debugResponseErrorPaths.contains(path.getId()) )
		{
			response.setError(bhE_ResponseError.SERVER_EXCEPTION);
			return;
		}
		
		bhI_RequestHandler handler = m_handlers.get(path.getId());
		
		if( handler != null )
		{
			try
			{
				handler.handleRequest(context, request, response);
			}
			catch (Exception e)
			{
				//--- DRK > Handlers should try their damnedest to not let exceptions get up to here, so this is bad...
				s_logger.log(Level.SEVERE, "Exception occurred while invoking " + path + ".", e);
				
				response.setError(bhE_ResponseError.HANDLER_EXCEPTION);
			}
		}
		else
		{
			s_logger.log(Level.WARNING, "Could not find handler for path: " + path);
			
			response.setError(bhE_ResponseError.UNKNOWN_PATH);
		}
	}
}