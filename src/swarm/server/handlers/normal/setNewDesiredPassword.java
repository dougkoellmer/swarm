package swarm.server.handlers.normal;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.http.HttpServletRequest;

import swarm.server.account.E_Role;
import swarm.server.account.S_ServerAccount;
import swarm.server.account.ServerAccountManager;
import swarm.server.account.UserSession;

import swarm.server.session.SessionManager;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.account.E_SignInCredentialType;
import swarm.shared.account.S_Account;
import swarm.shared.account.SignInCredentials;
import swarm.shared.account.SignInValidationResult;
import swarm.shared.account.SignInValidator;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class setNewDesiredPassword extends A_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(setNewDesiredPassword.class.getName());
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		ServerAccountManager accountManager = m_serverContext.accountMngr;
		
		//--- DRK > Just a sanity check...probably meaningless.
		UserSession session = m_serverContext.sessionMngr.getSession(request, response);
		if( session != null )
		{
			response.setError(E_ResponseError.BAD_STATE);
			
			return;
		}
		
		//--- DRK > Get password change token.
		SignInCredentials creds = new SignInCredentials(m_serverContext.jsonFactory, request.getJsonArgs());
		creds.setIsForNewPassword(true);
		SignInValidationResult result = new SignInValidationResult();
		String changeToken = accountManager.setNewDesiredPassword(creds, result);
		
		if( changeToken != null )
		{
			HttpServletRequest nativeRequest = (HttpServletRequest) request.getNativeRequest();

			Properties props = new Properties();
	        Session mailSession = Session.getDefaultInstance(props, null);
	        
	        String serverAddress = "http://www.b33hive.net";
	        serverAddress += "?" + S_ServerAccount.PASSWORD_CHANGE_TOKEN_PARAMETER_NAME + "=" + changeToken;

	        String msgBody =
    			"Hello,<br><br>" +
    			"In order to confirm your password change, click on the following link:<br><br>" +
    			"<a href=\""+serverAddress+"\">"+serverAddress+"</a><br><br>" +
    			"Just reply to this e-mail if you have any questions.<br><br>" +
    			"Thanks,<br><br>" +
    			"The b33hive team";
	        try
	        {
	            Message msg = new MimeMessage(mailSession);
	            msg.setFrom(new InternetAddress("support@b33hive.net", "b33hive Support"));
	            msg.addRecipient(Message.RecipientType.TO,  new InternetAddress(creds.get(E_SignInCredentialType.EMAIL)));
	            msg.setSubject("Password Change Verification");
	            msg.setContent(msgBody, "text/html");
	            Transport.send(msg);
	        }
	        catch (AddressException e)
	        {
	        	s_logger.log(Level.SEVERE, "Could not send e-mail.", e);
	        	response.setError(E_ResponseError.SERVICE_EXCEPTION);
	        }
	        catch (MessagingException e)
	        {
	        	s_logger.log(Level.SEVERE, "Could not send e-mail.", e);
	        	response.setError(E_ResponseError.SERVICE_EXCEPTION);
	        }
			catch (UnsupportedEncodingException e)
			{
				s_logger.log(Level.SEVERE, "Could not send e-mail.", e);
				response.setError(E_ResponseError.SERVICE_EXCEPTION);
			}
		}
		else
		{
			//--- DRK > We actually don't set a response error here, because we want a failure to look the same as success
			//---		so that we don't have a path for "hackers" to figure out legitimate e-mails.
			
			//TODO: Should probably change account manager API to differeniate between "e-mail not found" and "data error".
			//		We probably want to send a data error back to the client if there's an SQL connection problem or something.
		}
	}
}
