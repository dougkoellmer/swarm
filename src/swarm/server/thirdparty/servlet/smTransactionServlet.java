package swarm.server.thirdparty.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;


import swarm.server.app.smA_ServerApp;
import swarm.server.app.smServerContext;
import swarm.server.transaction.smServerTransactionManager;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.app.smS_App;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.transaction.smS_Transaction;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;


public class smTransactionServlet extends smA_BaseServlet
{
	private static final Logger s_logger = Logger.getLogger(smTransactionServlet.class.getName());
	
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
		//smU_Servlet.simulateLag(10000);
		//smU_Servlet.simulateException(true);
		
		smServerContext context = smA_ServerApp.getInstance().getContext();
		
		smI_JsonObject requestJson = smU_Servlet.getRequestJson(nativeRequest, isGet);
		smI_JsonObject responseJson = context.jsonFactory.createJsonObject();
		
		context.txnMngr.handleRequestFromClient(nativeRequest, nativeResponse, this.getServletContext(), requestJson, responseJson);
		
		smU_Servlet.writeJsonResponse(responseJson, nativeResponse.getWriter());
		
		nativeResponse.getWriter().flush();
	}
}
