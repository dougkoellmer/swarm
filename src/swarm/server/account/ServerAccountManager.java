package swarm.server.account;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import swarm.server.data.sql.A_SqlDatabase;
import swarm.shared.account.E_SignInCredentialType;
import swarm.shared.account.E_SignInValidationError;
import swarm.shared.account.E_SignUpCredentialType;
import swarm.shared.account.E_SignUpValidationError;
import swarm.shared.account.S_Account;
import swarm.shared.account.SignInCredentials;
import swarm.shared.account.SignInValidationResult;
import swarm.shared.account.SignInValidator;
import swarm.shared.account.SignUpCredentials;
import swarm.shared.account.SignUpValidationResult;
import swarm.shared.account.SignUpValidator;

/**
 * 
 */
public class ServerAccountManager
{
	private static final Logger s_logger = Logger.getLogger(ServerAccountManager.class.getName());
	
	private static final String[] RESERVED_USERNAMES =
	{
		"admin",
		"_ah" // some app engine services potentially require this as a path...not sure, but just being safe.
	};
	
	private final I_AccountDatabase m_accountDb;
	private final SignInValidator m_signInValidator;
	private final SignUpValidator m_signUpValidator;
	
	public ServerAccountManager(SignInValidator signInValidator, SignUpValidator signUpValidator, I_AccountDatabase database)
	{
		m_signInValidator = signInValidator;
		m_signUpValidator = signUpValidator;
		m_accountDb = database;
	}
	
	//TODO: profanity filter?
	private boolean isAllowedUsername(String username)
	{
		for( int i = 0; i < RESERVED_USERNAMES.length; i++ )
		{
			if( username.equals(RESERVED_USERNAMES[i]) )
			{
				return false;
			}
		}
		
		return true;
	}
	
	private boolean signIn_earlyOut(SignInCredentials creds, SignInValidationResult result_out)
	{
		m_signInValidator.validate(creds, result_out);
		
		return !result_out.isEverythingOk();
	}
	
	private boolean signUp_earlyOut(SignUpCredentials creds, SignUpValidationResult result_out)
	{
		m_signUpValidator.validate(creds, result_out);
		
		return !result_out.isEverythingOk();
	}
	
	
	//TODO: Probably have to flush this out a bit.
	private boolean isAllowedEmail(String email)
	{
		return !email.contains("b33hive.net");
	}
	
	private Timestamp getPasswordChangeTokenTimestamp()
	{
		DateTime date = new DateTime();
		date = date.minusMinutes(S_Account.PASSWORD_RESET_TOKEN_EXPIRATION);
		
		Timestamp time = new Timestamp(date.getMillis());
		
		return time;
	}
	
	public boolean isPasswordChangeTokenValid(String changeToken)
	{
		byte[] tokenBytes = U_Hashing.convertHashStringToBytes(changeToken);
		
		Timestamp time = getPasswordChangeTokenTimestamp();
		
		try
		{
			if( m_accountDb.isPasswordChangeTokenValid(tokenBytes, time) )
			{
				return true;
			}
		}
		catch(SQLException e)
		{
			s_logger.log(Level.SEVERE, "Could not see if change token was valid.", e);
		}
		
		return false;
	}
	
	/**
	 * 
	 * @return A password change token to be e-mailed to the user, or null in case of issues.
	 */
	public String setNewDesiredPassword(SignInCredentials credentials, SignInValidationResult result_out)
	{
		if( signIn_earlyOut(credentials, result_out) )  return null;
		
		String email				= credentials.get(E_SignInCredentialType.EMAIL);
		String plainTextPassword	= credentials.get(E_SignInCredentialType.PASSWORD);
    	
    	byte[] passwordSalt = U_Hashing.calcRandomSaltBytes(S_ServerAccount.PASSWORD_SALT_BYTE_LENGTH);
		byte[] passwordHash = U_Hashing.hashWithSalt(plainTextPassword, passwordSalt);
		byte[] changeToken = U_Hashing.calcRandomSaltBytes(S_ServerAccount.PASSWORD_CHANGE_TOKEN_BYTE_LENGTH);
		
    	try
    	{
    		Timestamp time = new Timestamp(new java.util.Date().getTime());
    		if( m_accountDb.setNewDesiredPassword(email, passwordHash, passwordSalt, changeToken, time) )
    		{
    			return U_Hashing.convertBytesToUrlSafeString(changeToken);
    		}
    	}
    	catch(SQLException e)
    	{
    		s_logger.log(Level.SEVERE, "Could not set new desired password.", e);
    		
    		return null;
    	}
    	
    	return null;
	}
	
	public UserSession attemptSignUp(SignUpCredentials credentials, SignUpValidationResult result_out)
	{
		if( signUp_earlyOut(credentials, result_out) )  return null;
		
		String email				= credentials.get(E_SignUpCredentialType.EMAIL);
		String username				= credentials.get(E_SignUpCredentialType.USERNAME);
		String plainTextPassword	= credentials.get(E_SignUpCredentialType.PASSWORD);
		
		if( !isAllowedEmail(email) )
		{
			result_out.setError(E_SignUpCredentialType.EMAIL, E_SignUpValidationError.EMAIL_TAKEN);
			return null;
		}
		
		if( !isAllowedUsername(username) )
		{
			result_out.setError(E_SignUpCredentialType.USERNAME, E_SignUpValidationError.USERNAME_TAKEN);
			return null;
		}
		
		Random random = new Random();
		int id = random.nextInt(Integer.MAX_VALUE);
		byte[] passwordSalt = U_Hashing.calcRandomSaltBytes(S_ServerAccount.PASSWORD_SALT_BYTE_LENGTH);
		byte[] passwordHash = U_Hashing.hashWithSalt(plainTextPassword, passwordSalt);
		E_Role role = E_Role.USER;
		
		int attemptCount = 0;
		
		while( attemptCount < S_ServerAccount.RETRY_COUNT_FOR_ID_UNIQUENESS )
		{
			try
			{
				m_accountDb.addAccount(id, email, username, passwordHash, passwordSalt, role);
				
				UserSession userSession = new UserSession(id, username, role);
				
				return userSession;
			}
			catch(SQLException e1)
			{
				if( e1.getErrorCode() == S_ServerAccount.DUPLICATE_ENTRY_ERROR_CODE )
				{
					s_logger.warning("Generated non-unique account id "+id+"...trying again.");
					
					int flags = 0;
					
					try
					{
						flags = m_accountDb.containsSignUpCredentials(email, username);
					}
					catch(SQLException e2)
					{
						s_logger.log(Level.SEVERE, "Could not check which credentials already exist.", e1);
						
						result_out.setResponseError();
						
						return null;
					}
					
					if( flags != 0 )
					{
						if ( (flags & F_SignUpExistance.EMAIL_EXISTS) != 0 )
						{
							result_out.setError(E_SignUpCredentialType.EMAIL, E_SignUpValidationError.EMAIL_TAKEN);
						}
						
						if( (flags & F_SignUpExistance.USERNAME_EXISTS) != 0 )
						{
							result_out.setError(E_SignUpCredentialType.USERNAME, E_SignUpValidationError.USERNAME_TAKEN);
						}
						
						return null;
					}
					else
					{
						//--- DRK > By elimination, the duplicate entry is the random id we generated, so we loop back and try again.
						//---		This should be pretty rare, but is of course possible.
						attemptCount++;
						
						continue; // < this is here just to be explicit about the control flow.
					}
				}
				else
				{
					s_logger.log(Level.SEVERE, "Could not add account due to exception.", e1);
					
					result_out.setResponseError();
					
					return null;
				}
			}
		}
		
		//--- DRK > This means (should mean) that we tried smS_AccountManagement.RETRY_COUNT_FOR_ID_UNIQUENESS times
		//---		to generate a unique id, and they all failed...since this is highly unlikely, this is logged in case
		//---		there's some other bug-related condition that makes it here.
		//---		This simply results in the user seeing a "server error" type of message, and they can try again with the same credentials.
		s_logger.warning("Exceeded retry count for finding a unique user id.");
		
		result_out.setResponseError();
		
		return null;
	}
	
	public UserSession confirmNewPassword(SignInCredentials credentials, String passwordResetToken, SignInValidationResult result_out)
    {
		if( signIn_earlyOut(credentials, result_out) )  return null;
		
    	String email				= credentials.get(E_SignInCredentialType.EMAIL);
		String plainTextPassword	= credentials.get(E_SignInCredentialType.PASSWORD);
		
    	try
    	{
	        byte[] salt = m_accountDb.getPasswordSalt(email, I_AccountDatabase.E_PasswordType.NEW);
	        
	        if( salt == null )
	        {
	        	result_out.setResponseError();
	        }
	        else
	        {
	        	byte[] passwordHash	= U_Hashing.hashWithSalt(plainTextPassword, salt);
	        	Timestamp time = getPasswordChangeTokenTimestamp();
	        	byte[] changeTokenBytes = U_Hashing.convertHashStringToBytes(passwordResetToken);
	            
	        	UserSession userSession = m_accountDb.confirmNewPassword(email, passwordHash, salt, changeTokenBytes, time);
	        	
	            if (userSession == null)
	            {
	            	result_out.setResponseError();
	            }
	            
	            return userSession;
	        }
    	}
    	catch(SQLException e)
    	{
    		s_logger.log(Level.SEVERE, "Could not sign user in with new password.", e);
    		
    		result_out.setResponseError();
    	}
    	
    	return null;
    }

	public UserSession attemptSignIn(SignInCredentials credentials, SignInValidationResult result_out)
    {
		if( signIn_earlyOut(credentials, result_out) )  return null;
		
    	String email				= credentials.get(E_SignInCredentialType.EMAIL);
		String plainTextPassword	= credentials.get(E_SignInCredentialType.PASSWORD);
	
    	try
    	{
	        byte[] salt = m_accountDb.getPasswordSalt(email, I_AccountDatabase.E_PasswordType.CURRENT);
	        
	        if( salt == null )
	        {
	        	result_out.setBadCombinationError();
	        }
	        else
	        {
	        	byte[] passwordHash	= U_Hashing.hashWithSalt(plainTextPassword, salt);
	            
	        	UserSession userSession = m_accountDb.containsSignInCredentials(email, passwordHash);
	            if (userSession == null)
	            {
	            	result_out.setBadCombinationError();
	            }
	            
	            return userSession;
	        }
    	}
    	catch(SQLException e)
    	{
    		s_logger.log(Level.SEVERE, "Could not sign user in.", e);
    		result_out.setResponseError();
    	}
    	
    	return null;
    }
}
