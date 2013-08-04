package b33hive.server.thirdparty.servlet;

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

import b33hive.server.transaction.bhServerTransactionManager;
import b33hive.shared.app.bhS_App;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.transaction.bhS_Transaction;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;
import b33hive.shared.utils.bhU_Singletons;

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
		
		bhA_JsonFactory jsonFactory = bhU_Singletons.get(bhA_JsonFactory.class);
		bhI_JsonObject requestJson = bhU_Servlet.getRequestJson(nativeRequest, isGet);
		bhI_JsonObject responseJson = jsonFactory.createJsonObject();
		
		bhServerTransactionManager.getInstance().handleRequestFromClient(nativeRequest, nativeResponse, this.getServletContext(), requestJson, responseJson, bhS_App.VERBOSE_TRANSACTIONS);
		
		bhU_Servlet.writeJsonResponse(responseJson, nativeResponse.getWriter());
		
		nativeResponse.getWriter().flush();
	}
}
