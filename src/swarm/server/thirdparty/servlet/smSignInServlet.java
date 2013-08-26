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

import swarm.server.account.smE_Role;
import swarm.server.account.smServerAccountManager;
import swarm.server.account.smUserSession;
import swarm.server.account.sm_s;
import swarm.server.app.smA_ServerJsonFactory;
import swarm.server.session.smSessionManager;
import swarm.server.transaction.smServerTransactionManager;
import swarm.shared.account.smSignInCredentials;
import swarm.shared.account.smSignInValidationResult;
import swarm.shared.account.smSignInValidator;
import swarm.shared.app.sm;
import swarm.shared.app.smS_App;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.transaction.smS_Transaction;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;


public class smSignInServlet extends smA_BaseServlet
{
	private static final Logger s_logger = Logger.getLogger(smSignInServlet.class.getName());
	
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
		smSessionManager sessionMngr = sm_s.sessionMngr;
		smServerAccountManager accountMngr = sm_s.accountMngr;
		
		((smA_ServerJsonFactory)sm_s.jsonFactory).startScope(true);
		
		try
		{
			nativeResponse.setContentType("text/html");
			
			smI_JsonObject requestJson = null;
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
						smSignInCredentials creds = new smSignInCredentials(false, email, password);
						smSignInValidationResult result = new smSignInValidationResult();

						smUserSession session = accountMngr.attemptSignIn(creds, result);
						
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
			((smA_ServerJsonFactory)sm_s.jsonFactory).endScope();
		}
	}
}
