package swarm.server.transaction;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;


import swarm.server.app.smA_ServerJsonFactory;
import swarm.shared.app.smS_App;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smI_ReadsJson;
import swarm.shared.json.smI_WritesJson;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smRequestPathManager;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class smInlineTransactionManager
{	
	private static final class Context
	{
		private final Writer m_out;
		private final HttpServletRequest m_nativeRequest;
		private final HttpServletResponse m_nativeResponse;
		
		private Context(Writer out, HttpServletRequest nativeRequest, HttpServletResponse nativeResponse)
		{
			m_out = out;
			m_nativeRequest = nativeRequest;
			m_nativeResponse = nativeResponse;
		}
	}
	
	private final ThreadLocal<Context> m_context = new ThreadLocal<Context>();
	
	private final ArrayList<smI_TransactionScopeListener> m_scopeListeners = new ArrayList<smI_TransactionScopeListener>();
	
	private final smA_ServerJsonFactory m_jsonFactory;
	private final boolean m_verboseTransactions;
	private final String m_appId;
	private final smServerTransactionManager m_txnMngr;
	private final smRequestPathManager m_requestPathMngr;
	
	public smInlineTransactionManager(smServerTransactionManager txnMngr, smRequestPathManager requestPathMngr, smA_ServerJsonFactory jsonFactory, String appId, boolean verboseTransactions)
	{
		m_txnMngr = txnMngr;
		m_requestPathMngr = requestPathMngr;
		m_appId = appId;
		m_jsonFactory = jsonFactory;
		m_verboseTransactions = verboseTransactions;
	}
	
	public void addScopeListener(smI_TransactionScopeListener listener)
	{
		m_scopeListeners.add(listener);
	}
	
	public void beginBatch(Writer out, HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		//TODO: Shouldn't be b33hive specific here...need to get application prefix from elsewhere.
		out.write("var "+m_appId+"_rl = [];");
		m_context.set(new Context(out, request, response));
		
		m_jsonFactory.startScope(m_verboseTransactions);
		
		for( int i = 0; i < m_scopeListeners.size(); i++ )
		{
			m_scopeListeners.get(i).onEnterScope();
			m_scopeListeners.get(i).onBatchStart();
		}
	}
	
	public void endBatch()
	{
		m_context.remove();
		
		m_jsonFactory.endScope();
		
		for( int i = 0; i < m_scopeListeners.size(); i++ )
		{
			m_scopeListeners.get(i).onBatchEnd();
			m_scopeListeners.get(i).onExitScope();
		}
	}
	
	private void writeInlineTransaction(smTransactionRequest request, smTransactionResponse response) throws IOException
	{
		smI_JsonObject requestJson = m_jsonFactory.createJsonObject();
		smI_JsonObject responseJson = m_jsonFactory.createJsonObject();
		
		request.writeJson(m_jsonFactory, m_requestPathMngr, requestJson);
		response.writeJson(m_jsonFactory, responseJson);
		
		String requestJsonString = requestJson.writeString();
		String responseJsonString = responseJson.writeString();
		
		//TODO: Make this non application specific somehow, so the "sm" prefix is retreived from somewhere.
		m_context.get().m_out.write(m_appId+"_rl.push(['"+requestJsonString+"', '"+responseJsonString+"']);");
	}
	
	public void makeInlineRequest(smTransactionRequest request, smTransactionResponse response) throws IOException
	{
		m_txnMngr.callRequestHandler(new smTransactionContext(false, null), request, response);
		
		writeInlineTransaction(request, response);
	}
	
	public smTransactionResponse makeInlineRequest(smE_RequestPath path) throws IOException
	{
		return makeInlineRequest(path, null);
	}
	
	public smTransactionResponse makeInlineRequest(smE_RequestPath path, smI_WritesJson writesJson) throws IOException
	{
		smTransactionRequest request = new smTransactionRequest(m_jsonFactory, path, m_context.get().m_nativeRequest);
		smTransactionResponse response = new smTransactionResponse(m_jsonFactory, m_context.get().m_nativeResponse);
		
		if( writesJson != null )
		{
			writesJson.writeJson(m_jsonFactory, request.getJsonArgs());
		}
		
		makeInlineRequest(request, response);
		
		return response;
	}
	
	public void makeInlineRequestWithResponse(smE_RequestPath path, smI_WritesJson requestJsonEncodable, smI_WritesJson responseJsonEncodable) throws IOException
	{
		smTransactionRequest request = new smTransactionRequest(m_jsonFactory, path, m_context.get().m_nativeRequest);
		smTransactionResponse response = new smTransactionResponse(m_jsonFactory, m_context.get().m_nativeResponse);
		
		if( requestJsonEncodable != null )
		{
			requestJsonEncodable.writeJson(m_jsonFactory, request.getJsonArgs());
		}
		
		responseJsonEncodable.writeJson(m_jsonFactory, response.getJsonArgs());
		
		writeInlineTransaction(request, response);
	}
	
	public void makeInlineRequestWithResponse(smE_RequestPath path, smI_WritesJson responseJsonEncodable) throws IOException
	{
		makeInlineRequestWithResponse(path, null, responseJsonEncodable);
	}
}
