package swarm.server.thirdparty.servlet;

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

import swarm.server.account.bhE_Role;
import swarm.server.account.bhServerAccountManager;
import swarm.server.account.bhUserSession;
import swarm.server.account.sm_s;
import swarm.server.app.bhA_ServerJsonFactory;
import swarm.server.session.bhSessionManager;
import swarm.server.transaction.bhServerTransactionManager;
import swarm.shared.account.bhSignInCredentials;
import swarm.shared.account.bhSignInValidationResult;
import swarm.shared.account.bhSignInValidator;
import swarm.shared.app.sm;
import swarm.shared.app.bhS_App;
import swarm.shared.json.bhA_JsonFactory;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.transaction.bhS_Transaction;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;


public class bhSignInServlet extends bhA_BaseServlet
{
	private static final Logger s_logger = Logger.getLogger(bhSignInServlet.class.getName());
	
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
		bhSessionManager sessionMngr = sm_s.sessionMngr;
		bhServerAccountManager accountMngr = sm_s.accountMngr;
		
		((bhA_ServerJsonFactory)sm_s.jsonFactory).startScope(true);
		
		try
		{
			nativeResponse.setContentType("text/html");
			
			bhI_JsonObject requestJson = null;
			String email = "";
			String password = "";
			
			boolean isSignedIn = sessionMngr.isAuthenticated(nativeRequest, nativeResponse);
			
			if( !isGet )
			{
				if( nativeRequest.getParameter("signin") != null )
				{
					email = nativeRequest.getParameter("email");
					password = nativeRequest.getParameter("password");
					
					if( !isSignedIn )
					{
						bhSignInCredentials creds = new bhSignInCredentials(false, email, password);
						bhSignInValidationResult result = new bhSignInValidationResult();

						bhUserSession session = accountMngr.attemptSignIn(creds, result);
						
						if( session != null )
						{
							sessionMngr.startSession(session, nativeResponse, creds.rememberMe());
							
							isSignedIn = true;
						}
						
					}
				}
				else if ( nativeRequest.getParameter("signout") != null )
				{
					if( isSignedIn )
					{
						sessionMngr.endSession(nativeRequest, nativeResponse);
						isSignedIn = false;
					}
				}
			}
			
			PrintWriter writer = nativeResponse.getWriter();
			
			writer.write("<form method='POST'>");
			if( isSignedIn )
			{
				writer.write("<input type='submit' name='signout' value='Sign Out'>");
			}
			else
			{
				writer.write("<input placeholder='E-mail' type='text' name='email' value='"+email+"' ></input>");
				writer.write("<input placeholder='Password' type='password' name='password'></input>");
				writer.write("<input type='submit' name='signin' value='Sign In'>");
			}
				
			writer.write("</form>");
			
			writer.flush();
		}
		finally
		{
			((bhA_ServerJsonFactory)sm_s.jsonFactory).endScope();
		}
	}
}
