package b33hive.server.handlers.normal;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.http.HttpServletRequest;

import b33hive.server.account.bhE_Role;
import b33hive.server.account.bhS_ServerAccount;
import b33hive.server.account.bhServerAccountManager;
import b33hive.server.account.bhUserSession;
import b33hive.server.account.bh_s;
import b33hive.server.session.bhSessionManager;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.account.bhE_SignInCredentialType;
import b33hive.shared.account.bhS_Account;
import b33hive.shared.account.bhSignInCredentials;
import b33hive.shared.account.bhSignInValidationResult;
import b33hive.shared.account.bhSignInValidator;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class setNewDesiredPassword implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(setNewDesiredPassword.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhServerAccountManager accountManager = bh_s.accountMngr;
		
		//--- DRK > Just a sanity check...probably meaningless.
		bhUserSession session = bh_s.sessionMngr.getSession(request, response);
		if( session != null )
		{
			response.setError(bhE_ResponseError.BAD_STATE);
			
			return;
		}
		
		//--- DRK > Get password change token.
		bhSignInCredentials creds = new bhSignInCredentials(request.getJson());
		creds.setIsForNewPassword(true);
		bhSignInValidationResult result = new bhSignInValidationResult();
		String changeToken = accountManager.setNewDesiredPassword(creds, result);
		
		if( changeToken != null )
		{
			HttpServletRequest nativeRequest = (HttpServletRequest) request.getNativeRequest();

			Properties props = new Properties();
	        Session mailSession = Session.getDefaultInstance(props, null);
	        
	        String serverAddress = "http://www.b33hive.net";
	        serverAddress += "?" + bhS_ServerAccount.PASSWORD_CHANGE_TOKEN_PARAMETER_NAME + "=" + changeToken;

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
	            msg.addRecipient(Message.RecipientType.TO,  new InternetAddress(creds.get(bhE_SignInCredentialType.EMAIL)));
	            msg.setSubject("Password Change Verification");
	            msg.setContent(msgBody, "text/html");
	            Transport.send(msg);
	        }
	        catch (AddressException e)
	        {
	        	s_logger.log(Level.SEVERE, "Could not send e-mail.", e);
	        	response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
	        }
	        catch (MessagingException e)
	        {
	        	s_logger.log(Level.SEVERE, "Could not send e-mail.", e);
	        	response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
	        }
			catch (UnsupportedEncodingException e)
			{
				s_logger.log(Level.SEVERE, "Could not send e-mail.", e);
				response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
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
