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

import swarm.server.account.sm_s;
import swarm.server.transaction.bhServerTransactionManager;
import swarm.shared.app.sm;
import swarm.shared.app.bhS_App;
import swarm.shared.json.bhA_JsonFactory;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.transaction.bhS_Transaction;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;


public class bhTransactionServlet extends bhA_BaseServlet
{
	private static final Logger s_logger = Logger.getLogger(bhTransactionServlet.class.getName());
	
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
		//bhU_Servlet.simulateLag(1000);
		//bhU_Servlet.simulateException(true);
		
		bhI_JsonObject requestJson = bhU_Servlet.getRequestJson(nativeRequest, isGet);
		bhI_JsonObject responseJson = sm.jsonFactory.createJsonObject();
		
		sm_s.txnMngr.handleRequestFromClient(nativeRequest, nativeResponse, this.getServletContext(), requestJson, responseJson);
		
		bhU_Servlet.writeJsonResponse(responseJson, nativeResponse.getWriter());
		
		nativeResponse.getWriter().flush();
	}
}
