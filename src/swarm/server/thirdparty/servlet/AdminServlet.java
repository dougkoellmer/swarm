package swarm.server.thirdparty.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import swarm.server.account.E_Role;
import swarm.server.app.A_ServerApp;
import swarm.server.app.A_ServerJsonFactory;
import swarm.server.app.ServerContext;
import swarm.server.session.SessionManager;
import swarm.server.transaction.ServerTransactionManager;
import swarm.shared.app.BaseAppContext;
import swarm.shared.app.S_CommonApp;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.transaction.I_RequestPath;
import swarm.shared.transaction.S_Transaction;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;


public class AdminServlet extends A_BaseServlet
{
	private static final Logger s_logger = Logger.getLogger(AdminServlet.class.getName());
	
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
		ServerContext context = A_ServerApp.getInstance().getContext();
		
		((A_ServerJsonFactory)context.jsonFactory).startScope(true);
		
		//TODO: Move this check into an abstract base class or utlity method or
		//		something so we can have other admin servlets with same behavior.
		if( !context.sessionMngr.isAuthorized(nativeRequest, nativeResponse, E_Role.ADMIN) )
		{
			((A_ServerJsonFactory)context.jsonFactory).endScope();

			context.redirector.redirectToMainPage(nativeResponse);
			
			return;
		}
		
		try
		{
			nativeResponse.setContentType("text/html");
			
			I_JsonObject requestJson = null;
			
			String requestPathString = "";
			String requestJsonString = "";
			String requestJsonArgsString = "";
			
			if( !isGet )
			{
				requestPathString = nativeRequest.getParameter("requestPath");
				I_RequestPath requestPath = context.requestPathMngr.getPath(requestPathString);
				requestJson = context.jsonFactory.createJsonObject();
				context.requestPathMngr.putToJson(requestPath, requestJson);
				
				requestJsonArgsString = nativeRequest.getParameter("args");
				I_JsonObject requestJsonArgs = context.jsonFactory.createJsonObject(requestJsonArgsString);
				if( requestJsonArgs != null )
				{
					context.jsonFactory.getHelper().putJsonObject(requestJson, E_JsonKey.txnArgs, requestJsonArgs);
					
				}
			}
			
			PrintWriter writer = nativeResponse.getWriter();
			
			writer.write("<html><head><title>API</title></head><body onKeyPress=\"if(event.keyCode==13){document.forms['the_form'].submit();}\"><form name='the_form' method='POST'>");
			
			String selectTag = "<select style='width:300px;' name='requestPath'>";
			{
				Iterator<String> pathIterator = context.requestPathMngr.getPaths();
				while( pathIterator.hasNext() )
				{
					String path = pathIterator.next();
					String selected = path.equals(requestPathString) ? "selected" : "";
					selectTag += "<option "+selected+">"+path+"</option>";
				}
			}
			selectTag += "</select><br>";
			
			writer.write(selectTag);
			writer.write("<textarea style='width:300px; height:50px;' name='args'>"+requestJsonArgsString+"</textarea><br>");
			writer.write("<input type='submit' value='Submit'>");
			writer.write("</form></body></html>");
			
			if( !isGet )
			{
				I_JsonObject responseJson = context.jsonFactory.createJsonObject();
				
				context.txnMngr.handleRequestFromClient(nativeRequest, nativeResponse, this.getServletContext(), requestJson, responseJson, true);
				
				U_Servlet.writeJsonResponse(responseJson, nativeResponse.getWriter());
			}
			
			writer.flush();
		}
		finally
		{
			((A_ServerJsonFactory)context.jsonFactory).endScope();
		}
	}
}
