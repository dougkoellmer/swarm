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
package swarm.server.transaction;

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

import swarm.server.account.smAccountDatabase;
import swarm.server.app.smA_ServerJsonFactory;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.session.smSessionManager;
import swarm.server.telemetry.smTelemetryDatabase;
import swarm.shared.app.smA_App;
import swarm.shared.app.smS_App;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_ReadsJson;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonQuery;
import swarm.shared.json.smJsonHelper;
import swarm.shared.reflection.smI_Class;
import swarm.shared.structs.smOptHashMap;
import swarm.shared.transaction.smA_TransactionObject;
import swarm.shared.transaction.smCachePolicy;
import swarm.shared.transaction.smE_ReservedRequestPath;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smI_RequestPath;
import swarm.shared.transaction.smRequestPathManager;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;
import swarm.shared.transaction.smU_RequestBatch;

public class smServerTransactionManager
{
	private static final Logger s_logger = Logger.getLogger(smServerTransactionManager.class.getName());
	
	private final HashMap<Integer, smI_RequestHandler> m_handlers = new HashMap<Integer, smI_RequestHandler>();
	private final HashSet<Integer> m_debugResponseErrorPaths = new HashSet<Integer>();
	private final ArrayList<smI_DeferredRequestHandler> m_deferredHandlers = new ArrayList<smI_DeferredRequestHandler>();
	private final ArrayList<smI_TransactionScopeListener> m_scopeListeners = new ArrayList<smI_TransactionScopeListener>();
	private final smA_ServerJsonFactory m_jsonFactory;
	private final boolean m_verboseJson;
	
	public smServerTransactionManager(smA_ServerJsonFactory jsonFactory, smRequestPathManager requestPathMngr, boolean verboseTransactions)
	{
		m_jsonFactory = jsonFactory;
		
		requestPathMngr.register(smE_ReservedRequestPath.values());
		
		m_verboseJson = verboseTransactions;
	}
	
	public smI_RequestHandler getRequestHandler(smI_RequestPath path)
	{
		return m_handlers.get(path.getId());
	}
	
	public void setRequestHandler(smI_RequestHandler handler, smI_RequestPath path)
	{
		m_handlers.put(path.getId(), handler);
	}
	
	public void setDebugResponseError(smI_RequestPath path)
	{
		m_debugResponseErrorPaths.add(path.getId());
	}
	
	public void addDeferredHandler(smI_DeferredRequestHandler handler)
	{
		m_deferredHandlers.add(handler);
	}
	
	public void addScopeListener(smI_TransactionScopeListener listener)
	{
		m_scopeListeners.add(listener);
	}
	
	private smTransactionResponse createEarlyOutResponse(smE_ResponseError responseError)
	{
		smTransactionResponse responseToReturn = new smTransactionResponse(m_jsonFactory);
		responseToReturn.setError(responseError);
		
		return responseToReturn;
	}
	
	public void handleRequestFromClient(final Object nativeRequest, final Object nativeResponse, Object nativeContext, smI_JsonObject requestJson, smI_JsonObject responseJson_out)
	{
		this.handleRequestFromClient(nativeRequest, nativeResponse, nativeContext, requestJson, responseJson_out, m_verboseJson);
	}
	
	public void handleRequestFromClient(final Object nativeRequest, final Object nativeResponse, Object nativeContext, smI_JsonObject requestJson, smI_JsonObject responseJson_out, boolean verboseJson)
	{
		m_jsonFactory.startScope(verboseJson);
		
		smTransactionResponse responseToReturn = null;
		
		//--- DRK > Just being anal and putting everything within a try.
		try
		{
			//--- DRK > Early out for problems with request json.
			if( requestJson == null )
			{
				responseToReturn = this.createEarlyOutResponse(smE_ResponseError.REQUEST_READ_ERROR);
				
				return; // hits finally block
			}
			
			//--- DRK > Create a wrapper around the native request and see if there's a server version mismatch.
			smTransactionRequest wrappedRequest = new smTransactionRequest(m_jsonFactory, nativeRequest);
			wrappedRequest.readJson(m_jsonFactory, requestJson);
			Integer serverVersionAsFarAsClientKnows = wrappedRequest.getServerVersion();
			boolean serverVersionMismatch = false;
			
			if( serverVersionAsFarAsClientKnows != null && serverVersionAsFarAsClientKnows != smS_App.SERVER_VERSION )
			{
				serverVersionMismatch = true;
			}
			
			//--- DRK > Early out for server version mismatches.
			if( serverVersionMismatch )
			{
				responseToReturn = this.createEarlyOutResponse(smE_ResponseError.VERSION_MISMATCH);
				
				return; // hits finally block
			}
			
			if( wrappedRequest.getPath() == null )
			{
				responseToReturn = this.createEarlyOutResponse(smE_ResponseError.UNKNOWN_PATH);

				return; // hits finally block
			}
			
			boolean isBatch = wrappedRequest.getPath() == smE_ReservedRequestPath.batch;
			
			final smTransactionContext context = new smTransactionContext(isBatch, nativeContext);
			
			for( int i = 0; i < m_scopeListeners.size(); i++ )
			{
				m_scopeListeners.get(i).onEnterScope();
			}
	
			if( isBatch )
			{
				final smTransactionResponseBatch responseBatch = new smTransactionResponseBatch(m_jsonFactory);
				responseToReturn = responseBatch;
				
				smU_RequestBatch.I_JsonReadDelegate readDelegate = new smU_RequestBatch.I_JsonReadDelegate()
				{
					@Override
					public void onRequestFound(smI_JsonObject requestJson)
					{
						smTransactionRequest batchedRequest = new smTransactionRequest(m_jsonFactory, nativeRequest);
						batchedRequest.readJson(smServerTransactionManager.this.m_jsonFactory, requestJson);
						smTransactionResponse batchedResponse = new smTransactionResponse(m_jsonFactory, nativeResponse);
						
						context.addTransaction(batchedRequest, batchedResponse);
					}
				};
				
				smU_RequestBatch.readRequestList(m_jsonFactory, requestJson, readDelegate);
				
				smTransactionBatch transactionBatch = context.getBatch();
				
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
					smTransactionRequest batchedRequest = transactionBatch.getRequest(i);
					smTransactionResponse batchedResponse = transactionBatch.getResponse(i);
					
					callRequestHandler(context, batchedRequest, batchedResponse);
					
					if( batchedResponse.getError() == smE_ResponseError.DEFERRED )
					{
						context.queueDeferredTransaction(batchedRequest, batchedResponse);
					}
					
					responseBatch.addResponse(batchedResponse);
				}
				
				if( context.getDeferredCount() > 0 )
				{
					smTransactionBatch deferredBatch = context.getDeferredBatch();
					
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

							deferredBatch.markUnhandledTransactions(smE_ResponseError.HANDLER_EXCEPTION);
						}
						
						deferredBatch.removeHandledTransactions();
						
						if( deferredBatch.getCount() == 0 )
						{
							break;
						}
					}
					
					deferredBatch.markUnhandledTransactions(smE_ResponseError.DEFERRED_BUT_NEVER_HANDLED);
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
				responseToReturn = new smTransactionResponse(m_jsonFactory, nativeResponse);
				
				context.addTransaction(wrappedRequest, responseToReturn);
				
				callRequestHandler(context, wrappedRequest, responseToReturn);
				
				if( responseToReturn.getError() == smE_ResponseError.DEFERRED )
				{
					responseToReturn.setError(smE_ResponseError.DEFERRED_BUT_NEVER_HANDLED);
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
				responseToReturn = new smTransactionResponse(m_jsonFactory);
				responseToReturn.setError(smE_ResponseError.SERVER_EXCEPTION);
				
				s_logger.log(Level.SEVERE, "Response should not have been null.");
			}
			
			responseToReturn.writeJson(m_jsonFactory, responseJson_out);
			
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
	public void callRequestHandler(smTransactionRequest request)
	{
		this.callRequestHandler(new smTransactionContext(false, null), request, new smTransactionResponse(m_jsonFactory));
	}
	
	public void callRequestHandler(smTransactionRequest request, smTransactionResponse response)
	{
		this.callRequestHandler(new smTransactionContext(false, null), request, response);
	}
	
	public void callRequestHandler(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		response.setError(smE_ResponseError.NO_ERROR);
		
		smI_RequestPath path = request.getPath();
		
		if( path == null )
		{
			s_logger.log(Level.WARNING, "Could not find a registered path.");
			
			response.setError(smE_ResponseError.UNKNOWN_PATH);
			
			return;
		}
		
		if( m_debugResponseErrorPaths.contains(path.getId()) )
		{
			response.setError(smE_ResponseError.SERVER_EXCEPTION);
			return;
		}
		
		smI_RequestHandler handler = m_handlers.get(path.getId());
		
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
				
				response.setError(smE_ResponseError.HANDLER_EXCEPTION);
			}
		}
		else
		{
			s_logger.log(Level.WARNING, "Could not find handler for path: " + path);
			
			response.setError(smE_ResponseError.UNKNOWN_PATH);
		}
	}
}