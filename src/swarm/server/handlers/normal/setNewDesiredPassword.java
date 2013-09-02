package swarm.server.handlers.normal;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.http.HttpServletRequest;

import swarm.server.account.smE_Role;
import swarm.server.account.smS_ServerAccount;
import swarm.server.account.smServerAccountManager;
import swarm.server.account.smUserSession;

import swarm.server.session.smSessionManager;
import swarm.server.transaction.smA_DefaultRequestHandler;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.account.smE_SignInCredentialType;
import swarm.shared.account.smS_Account;
import swarm.shared.account.smSignInCredentials;
import swarm.shared.account.smSignInValidationResult;
import swarm.shared.account.smSignInValidator;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class setNewDesiredPassword extends smA_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(setNewDesiredPassword.class.getName());
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		smServerAccountManager accountManager = m_context.accountMngr;
		
		//--- DRK > Just a sanity check...probably meaningless.
		smUserSession session = m_context.sessionMngr.getSession(request, response);
		if( session != null )
		{
			response.setError(smE_ResponseError.BAD_STATE);
			
			return;
		}
		
		//--- DRK > Get password change token.
		smSignInCredentials creds = new smSignInCredentials(request.getJsonArgs());
		creds.setIsForNewPassword(true);
		smSignInValidationResult result = new smSignInValidationResult();
		String changeToken = accountManager.setNewDesiredPassword(creds, result);
		
		if( changeToken != null )
		{
			HttpServletRequest nativeRequest = (HttpServletRequest) request.getNativeRequest();

			Properties props = new Properties();
	        Session mailSession = Session.getDefaultInstance(props, null);
	        
	        String serverAddress = "http://www.b33hive.net";
	        serverAddress += "?" + smS_ServerAccount.PASSWORD_CHANGE_TOKEN_PARAMETER_NAME + "=" + changeToken;

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
	            msg.addRecipient(Message.RecipientType.TO,  new InternetAddress(creds.get(smE_SignInCredentialType.EMAIL)));
	            msg.setSubject("Password Change Verification");
	            msg.setContent(msgBody, "text/html");
	            Transport.send(msg);
	        }
	        catch (AddressException e)
	        {
	        	s_logger.log(Level.SEVERE, "Could not send e-mail.", e);
	        	response.setError(smE_ResponseError.SERVICE_EXCEPTION);
	        }
	        catch (MessagingException e)
	        {
	        	s_logger.log(Level.SEVERE, "Could not send e-mail.", e);
	        	response.setError(smE_ResponseError.SERVICE_EXCEPTION);
	        }
			catch (UnsupportedEncodingException e)
			{
				s_logger.log(Level.SEVERE, "Could not send e-mail.", e);
				response.setError(smE_ResponseError.SERVICE_EXCEPTION);
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
