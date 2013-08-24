package swarm.server.transaction;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import swarm.server.account.sm_s;
import swarm.server.app.bhA_ServerJsonFactory;
import swarm.shared.app.bhS_App;
import swarm.shared.json.bhI_JsonEncodable;
import swarm.shared.transaction.bhE_RequestPath;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class bhInlineTransactionManager
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
	
	private final ArrayList<bhI_TransactionScopeListener> m_scopeListeners = new ArrayList<bhI_TransactionScopeListener>();
	
	private final bhA_ServerJsonFactory m_jsonFactory;
	private final boolean m_verboseTransactions;
	private final String m_appId;
	
	public bhInlineTransactionManager(bhA_ServerJsonFactory jsonFactory, String appId, boolean verboseTransactions)
	{
		m_appId = appId;
		m_jsonFactory = jsonFactory;
		m_verboseTransactions = verboseTransactions;
	}
	
	public void addScopeListener(bhI_TransactionScopeListener listener)
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
	
	private void writeInlineTransaction(bhTransactionRequest request, bhTransactionResponse response) throws IOException
	{
		String requestJson = request.writeJson().writeString();
		String responseJson = response.writeJson().writeString();
		
		//TODO: Make this non application specific somehow, so the "bh" prefix is retreived from somewhere.
		m_context.get().m_out.write(m_appId+"_rl.push(['"+requestJson+"', '"+responseJson+"']);");
	}
	
	public void makeInlineRequest(bhTransactionRequest request, bhTransactionResponse response) throws IOException
	{
		sm_s.txnMngr.callRequestHandler(new bhTransactionContext(false, null), request, response);
		
		writeInlineTransaction(request, response);
	}
	
	public bhTransactionResponse makeInlineRequest(bhE_RequestPath path) throws IOException
	{
		return makeInlineRequest(path, null);
	}
	
	public bhTransactionResponse makeInlineRequest(bhE_RequestPath path, bhI_JsonEncodable jsonEncodable) throws IOException
	{
		bhTransactionRequest request = new bhTransactionRequest(m_context.get().m_nativeRequest);
		bhTransactionResponse response = new bhTransactionResponse(m_context.get().m_nativeResponse);
		
		request.setPath(path);
		
		if( jsonEncodable != null )
		{
			jsonEncodable.writeJson(request.getJson());
		}
		
		makeInlineRequest(request, response);
		
		return response;
	}
	
	public void makeInlineRequestWithResponse(bhE_RequestPath path, bhI_JsonEncodable requestJsonEncodable, bhI_JsonEncodable responseJsonEncodable) throws IOException
	{
		bhTransactionRequest request = new bhTransactionRequest(m_context.get().m_nativeRequest);
		bhTransactionResponse response = new bhTransactionResponse(m_context.get().m_nativeResponse);
		
		request.setPath(path);
		if( requestJsonEncodable != null )
		{
			requestJsonEncodable.writeJson(request.getJson());
		}
		
		responseJsonEncodable.writeJson(response.getJson());
		
		writeInlineTransaction(request, response);
	}
	
	public void makeInlineRequestWithResponse(bhE_RequestPath path, bhI_JsonEncodable responseJsonEncodable) throws IOException
	{
		makeInlineRequestWithResponse(path, null, responseJsonEncodable);
	}
}
