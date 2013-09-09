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

import swarm.server.account.smE_Role;

import swarm.server.app.smA_ServerApp;
import swarm.server.app.smA_ServerJsonFactory;
import swarm.server.app.smServerContext;
import swarm.server.session.smSessionManager;
import swarm.server.transaction.smServerTransactionManager;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.app.smS_App;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.transaction.smI_RequestPath;
import swarm.shared.transaction.smS_Transaction;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;


public class smAdminServlet extends smA_BaseServlet
{
	private static final Logger s_logger = Logger.getLogger(smAdminServlet.class.getName());
	
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
		smServerContext context = smA_ServerApp.getInstance().getContext();
		
		((smA_ServerJsonFactory)context.jsonFactory).startScope(true);
		
		//TODO: Move this check into an abstract base class or utlity method or
		//		something so we can have other admin servlets with same behavior.
		if( !context.sessionMngr.isAuthorized(nativeRequest, nativeResponse, smE_Role.ADMIN) )
		{
			((smA_ServerJsonFactory)context.jsonFactory).endScope();

			context.redirector.redirectToMainPage(nativeResponse);
			
			return;
		}
		
		try
		{
			nativeResponse.setContentType("text/html");
			
			smI_JsonObject requestJson = null;
			
			String requestPathString = "";
			
			if( !isGet )
			{
				requestPathString = nativeRequest.getParameter("requestPath");
				smI_RequestPath requestPath = context.requestPathMngr.getPath(requestPathString);
				requestJson = context.jsonFactory.createJsonObject();
				context.requestPathMngr.putToJson(requestJson, requestPath);
			}
			
			PrintWriter writer = nativeResponse.getWriter();
			
			writer.write("<html><head></head><body onKeyPress=\"if(event.keyCode==13){document.forms['the_form'].submit();}\"><form name='the_form' method='POST'>");
			
			String selectTag = "<select name='requestPath'>";
			{
				Iterator<String> pathIterator = context.requestPathMngr.getPaths();
				while( pathIterator.hasNext() )
				{
					String path = pathIterator.next();
					String selected = path.equals(requestPathString) ? "selected" : "";
					selectTag += "<option "+selected+">"+path+"</option>";
				}
			}
			selectTag += "</select>";
			
			writer.write(selectTag);
			//writer.write("<textarea style='width:400px; height:200px;' name='json'>"+requestJsonString+"</textarea>");
			writer.write("<input type='submit' value='Submit'>");
			writer.write("</form></body></html>");
			
			if( !isGet )
			{
				smI_JsonObject responseJson = context.jsonFactory.createJsonObject();
				
				context.txnMngr.handleRequestFromClient(nativeRequest, nativeResponse, this.getServletContext(), requestJson, responseJson, true);
				
				smU_Servlet.writeJsonResponse(responseJson, nativeResponse.getWriter());
			}
			
			writer.flush();
		}
		finally
		{
			((smA_ServerJsonFactory)context.jsonFactory).endScope();
		}
	}
}
