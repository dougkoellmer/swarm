package b33hive.server.transaction;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import b33hive.shared.app.bhS_App;
import b33hive.shared.json.bhI_JsonEncodable;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class bhInlineTransactionManager
{
	private static bhInlineTransactionManager s_instance = null;
	
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
	
	private bhInlineTransactionManager()
	{
		
	}
	
	public void addScopeListener(bhI_TransactionScopeListener listener)
	{
		m_scopeListeners.add(listener);
	}
	
	public static void startUp()
	{
		s_instance = new bhInlineTransactionManager();
	}
	
	public static bhInlineTransactionManager getInstance()
	{
		return s_instance;
	}
	
	public void beginBatch(Writer out, HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		//TODO: Shouldn't be b33hive specific here...need to get application prefix from elsewhere.
		out.write("var bh_rl = [];");
		m_context.set(new Context(out, request, response));
		
		bhJsonHelperProvider.getInstance().startScope(bhS_App.VERBOSE_TRANSACTIONS);
		
		for( int i = 0; i < m_scopeListeners.size(); i++ )
		{
			m_scopeListeners.get(i).onEnterScope();
			m_scopeListeners.get(i).onBatchStart();
		}
	}
	
	public void endBatch()
	{
		m_context.remove();
		
		bhJsonHelperProvider.getInstance().endScope();
		
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
		m_context.get().m_out.write("bh_rl.push(['"+requestJson+"', '"+responseJson+"']);");
	}
	
	public void makeInlineRequest(bhTransactionRequest request, bhTransactionResponse response) throws IOException
	{
		bhServerTransactionManager.getInstance().callRequestHandler(new bhTransactionContext(false, null), request, response);
		
		writeInlineTransaction(request, response);
	}
	
	public void makeInlineRequest(bhE_RequestPath path) throws IOException
	{
		makeInlineRequest(path, null);
	}
	
	public void makeInlineRequest(bhE_RequestPath path, bhI_JsonEncodable jsonEncodable) throws IOException
	{
		bhTransactionRequest request = new bhTransactionRequest(m_context.get().m_nativeRequest);
		bhTransactionResponse response = new bhTransactionResponse(m_context.get().m_nativeResponse);
		
		request.setPath(path);
		
		if( jsonEncodable != null )
		{
			jsonEncodable.writeJson(request.getJson());
		}
		
		makeInlineRequest(request, response);
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
