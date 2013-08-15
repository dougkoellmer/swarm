package b33hive.server.thirdparty.servlet;

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

import b33hive.server.account.bhE_Role;
import b33hive.server.account.bh_s;
import b33hive.server.app.bhA_ServerJsonFactory;
import b33hive.server.session.bhSessionManager;
import b33hive.server.transaction.bhServerTransactionManager;
import b33hive.shared.app.bh;
import b33hive.shared.app.bhS_App;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.transaction.bhS_Transaction;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;


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
		((bhA_ServerJsonFactory)bh_s.jsonFactory).startScope(true);
		
		//TODO: Move this check into an abstract base class or utlity method or
		//		something so we can have other admin servlets with same behavior.
		if( !bh_s.sessionMngr.isAuthorized(nativeRequest, nativeResponse, bhE_Role.ADMIN) )
		{
			((bhA_ServerJsonFactory)bh_s.jsonFactory).endScope();

			bh_s.requestRedirector.redirectToMainPage(nativeResponse);
			
			return;
		}
		
		try
		{
			nativeResponse.setContentType("text/html");
			
			bhI_JsonObject requestJson = null;
			String requestJsonString = "";
			
			if( !isGet )
			{
				requestJsonString = nativeRequest.getParameter("json");
				requestJson = requestJsonString == null ? null : bh.jsonFactory.createJsonObject(requestJsonString);
			}
			
			PrintWriter writer = nativeResponse.getWriter();
			
			writer.write("<form method='POST'>");
			writer.write("<textarea style='width:400px; height:200px;' name='json'>"+requestJsonString+"</textarea>");
			writer.write("<input type='submit' value='Submit'>");
			writer.write("</form>");
			
			if( !isGet )
			{
				bhI_JsonObject responseJson = bh.jsonFactory.createJsonObject();
				
				bh_s.txnMngr.handleRequestFromClient(nativeRequest, nativeResponse, this.getServletContext(), requestJson, responseJson, true);
				
				bhU_Servlet.writeJsonResponse(responseJson, nativeResponse.getWriter());
			}
			
			writer.flush();
		}
		finally
		{
			((bhA_ServerJsonFactory)bh_s.jsonFactory).endScope();
		}
	}
}
