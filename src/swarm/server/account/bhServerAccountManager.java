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

import swarm.server.account.bhAccountDatabase.E_PasswordType;
import swarm.server.data.sql.bhA_SqlDatabase;
import swarm.shared.account.bhE_SignInCredentialType;
import swarm.shared.account.bhE_SignInValidationError;
import swarm.shared.account.bhE_SignUpCredentialType;
import swarm.shared.account.bhE_SignUpValidationError;
import swarm.shared.account.bhS_Account;
import swarm.shared.account.bhSignInCredentials;
import swarm.shared.account.bhSignInValidationResult;
import swarm.shared.account.bhSignInValidator;
import swarm.shared.account.bhSignUpCredentials;
import swarm.shared.account.bhSignUpValidationResult;
import swarm.shared.account.bhSignUpValidator;

/**
 * 
 */
public class bhServerAccountManager
{
	private static final Logger s_logger = Logger.getLogger(bhServerAccountManager.class.getName());
	
	private static final String[] RESERVED_USERNAMES =
	{
		"admin",
		"_ah" // some app engine services potentially require this as a path...not sure, but just being safe.
	};
	
	private final bhAccountDatabase m_accountDb;
	
	public bhServerAccountManager(bhAccountDatabase database)
	{
		m_accountDb = database;
	}
	
	public bhA_SqlDatabase getAccountDb()
	{
		return m_accountDb;
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
	
	private boolean earlyOut_signIn(bhSignInCredentials creds, bhSignInValidationResult result_out)
	{
		bhSignInValidator.getInstance().validate(creds, result_out);
		
		return !result_out.isEverythingOk();
	}
	
	private boolean earlyOut_signUp(bhSignUpCredentials creds, bhSignUpValidationResult result_out)
	{
		bhSignUpValidator.getInstance().validate(creds, result_out);
		
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
		date = date.minusMinutes(bhS_Account.PASSWORD_RESET_TOKEN_EXPIRATION);
		
		Timestamp time = new Timestamp(date.getMillis());
		
		return time;
	}
	
	public boolean isPasswordChangeTokenValid(String changeToken)
	{
		byte[] tokenBytes = bhU_Hashing.convertHashStringToBytes(changeToken);
		
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
	public String setNewDesiredPassword(bhSignInCredentials credentials, bhSignInValidationResult result_out)
	{
		if( earlyOut_signIn(credentials, result_out) )  return null;
		
		String email				= credentials.get(bhE_SignInCredentialType.EMAIL);
		String plainTextPassword	= credentials.get(bhE_SignInCredentialType.PASSWORD);
    	
    	byte[] passwordSalt = bhU_Hashing.calcRandomSaltBytes(bhS_ServerAccount.PASSWORD_SALT_BYTE_LENGTH);
		byte[] passwordHash = bhU_Hashing.hashWithSalt(plainTextPassword, passwordSalt);
		byte[] changeToken = bhU_Hashing.calcRandomSaltBytes(bhS_ServerAccount.PASSWORD_CHANGE_TOKEN_BYTE_LENGTH);
		
    	try
    	{
    		Timestamp time = new Timestamp(new java.util.Date().getTime());
    		if( m_accountDb.setNewDesiredPassword(email, passwordHash, passwordSalt, changeToken, time) )
    		{
    			return bhU_Hashing.convertBytesToUrlSafeString(changeToken);
    		}
    	}
    	catch(SQLException e)
    	{
    		s_logger.log(Level.SEVERE, "Could not set new desired password.", e);
    		
    		return null;
    	}
    	
    	return null;
	}
	
	public bhUserSession attemptSignUp(bhSignUpCredentials credentials, bhSignUpValidationResult result_out)
	{
		if( earlyOut_signUp(credentials, result_out) )  return null;
		
		String email				= credentials.get(bhE_SignUpCredentialType.EMAIL);
		String username				= credentials.get(bhE_SignUpCredentialType.USERNAME);
		String plainTextPassword	= credentials.get(bhE_SignUpCredentialType.PASSWORD);
		
		if( !isAllowedEmail(email) )
		{
			result_out.setError(bhE_SignUpCredentialType.EMAIL, bhE_SignUpValidationError.EMAIL_TAKEN);
			return null;
		}
		
		if( !isAllowedUsername(username) )
		{
			result_out.setError(bhE_SignUpCredentialType.USERNAME, bhE_SignUpValidationError.USERNAME_TAKEN);
			return null;
		}
		
		Random random = new Random();
		int id = random.nextInt(Integer.MAX_VALUE);
		byte[] passwordSalt = bhU_Hashing.calcRandomSaltBytes(bhS_ServerAccount.PASSWORD_SALT_BYTE_LENGTH);
		byte[] passwordHash = bhU_Hashing.hashWithSalt(plainTextPassword, passwordSalt);
		bhE_Role role = bhE_Role.USER;
		
		int attemptCount = 0;
		
		while( attemptCount < bhS_ServerAccount.RETRY_COUNT_FOR_ID_UNIQUENESS )
		{
			try
			{
				m_accountDb.addAccount(id, email, username, passwordHash, passwordSalt, role);
				
				bhUserSession userSession = new bhUserSession(id, username, role);
				
				return userSession;
			}
			catch(SQLException e1)
			{
				if( e1.getErrorCode() == bhS_ServerAccount.DUPLICATE_ENTRY_ERROR_CODE )
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
						if ( (flags & bhF_SignUpExistance.EMAIL_EXISTS) != 0 )
						{
							result_out.setError(bhE_SignUpCredentialType.EMAIL, bhE_SignUpValidationError.EMAIL_TAKEN);
						}
						
						if( (flags & bhF_SignUpExistance.USERNAME_EXISTS) != 0 )
						{
							result_out.setError(bhE_SignUpCredentialType.USERNAME, bhE_SignUpValidationError.USERNAME_TAKEN);
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
		
		//--- DRK > This means (should mean) that we tried bhS_AccountManagement.RETRY_COUNT_FOR_ID_UNIQUENESS times
		//---		to generate a unique id, and they all failed...since this is highly unlikely, this is logged in case
		//---		there's some other bug-related condition that makes it here.
		//---		This simply results in the user seeing a "server error" type of message, and they can try again with the same credentials.
		s_logger.warning("Exceeded retry count for finding a unique user id.");
		
		result_out.setResponseError();
		
		return null;
	}
	
	public bhUserSession confirmNewPassword(bhSignInCredentials credentials, String passwordResetToken, bhSignInValidationResult result_out)
    {
		if( earlyOut_signIn(credentials, result_out) )  return null;
		
    	String email				= credentials.get(bhE_SignInCredentialType.EMAIL);
		String plainTextPassword	= credentials.get(bhE_SignInCredentialType.PASSWORD);
		
    	try
    	{
	        byte[] salt = m_accountDb.getPasswordSalt(email, E_PasswordType.NEW);
	        
	        if( salt == null )
	        {
	        	result_out.setResponseError();
	        }
	        else
	        {
	        	byte[] passwordHash	= bhU_Hashing.hashWithSalt(plainTextPassword, salt);
	        	Timestamp time = getPasswordChangeTokenTimestamp();
	        	byte[] changeTokenBytes = bhU_Hashing.convertHashStringToBytes(passwordResetToken);
	            
	        	bhUserSession userSession = m_accountDb.confirmNewPassword(email, passwordHash, salt, changeTokenBytes, time);
	        	
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

	public bhUserSession attemptSignIn(bhSignInCredentials credentials, bhSignInValidationResult result_out)
    {
		if( earlyOut_signIn(credentials, result_out) )  return null;
		
    	String email				= credentials.get(bhE_SignInCredentialType.EMAIL);
		String plainTextPassword	= credentials.get(bhE_SignInCredentialType.PASSWORD);
	
    	try
    	{
	        byte[] salt = m_accountDb.getPasswordSalt(email, E_PasswordType.CURRENT);
	        
	        if( salt == null )
	        {
	        	result_out.setBadCombinationError();
	        }
	        else
	        {
	        	byte[] passwordHash	= bhU_Hashing.hashWithSalt(plainTextPassword, salt);
	            
	        	bhUserSession userSession = m_accountDb.containsSignInCredentials(email, passwordHash);
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
