package com.b33hive.server.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.b33hive.server.account.bhE_Role;
import com.b33hive.server.json.bhServerJsonObject;
import com.b33hive.server.session.bhSessionManager;
import com.b33hive.server.transaction.bhJsonHelperProvider;
import com.b33hive.server.transaction.bhServerTransactionManager;
import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.json.bhA_JsonFactory;
import com.b33hive.shared.json.bhI_JsonObject;
import com.b33hive.shared.transaction.bhS_Transaction;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

public class bhAdminServlet extends bhA_BaseServlet
{
	private static final Logger s_logger = Logger.getLogger(bhAdminServlet.class.getName());
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doGetOrPost(req, resp, true);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doGetOrPost(req, resp, false);
	}
	
	private void doGetOrPost(HttpServletRequest nativeRequest, HttpServletResponse nativeResponse, boolean isGet) throws ServletException, IOException
	{
		bhJsonHelperProvider.getInstance().startScope(bhS_App.VERBOSE_TRANSACTIONS);
		
		if( !bhSessionManager.getInstance().isAuthorized(nativeRequest, nativeResponse, bhE_Role.ADMIN) )
		{
			bhJsonHelperProvider.getInstance().endScope();
			
			bhU_Servlet.redirectToMainPage(nativeResponse);
			
			return;
		}
		
		bhJsonHelperProvider.getInstance().endScope();
		
		
		nativeResponse.setContentType("text/html");
		
		bhI_JsonObject requestJson = null;
		String requestJsonString = "";
		
		if( !isGet )
		{
			requestJsonString = nativeRequest.getParameter("json");
			requestJson = requestJsonString == null ? null : bhA_JsonFactory.getInstance().createJsonObject(requestJsonString);
		}
		
		PrintWriter writer = nativeResponse.getWriter();
		
		writer.write("<form method='POST'>");
		writer.write("<textarea style='width:400px; height:200px;' name='json'>"+requestJsonString+"</textarea>");
		writer.write("<input type='submit' value='Submit'>");
		writer.write("</form>");
		
		if( !isGet )
		{
			bhI_JsonObject responseJson = bhA_JsonFactory.getInstance().createJsonObject();
			
			bhServerTransactionManager.getInstance().handleRequestFromClient(nativeRequest, nativeResponse, this.getServletContext(), requestJson, responseJson, true);
			
			bhU_Servlet.writeJsonResponse(responseJson, nativeResponse.getWriter());
		}
		
		writer.flush();
	}
}
