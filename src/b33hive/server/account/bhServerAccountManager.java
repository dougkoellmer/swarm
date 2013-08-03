package b33hive.server.account;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import b33hive.server.account.bhAccountDatabase.E_PasswordType;
import b33hive.shared.account.bhE_SignInCredentialType;
import b33hive.shared.account.bhE_SignInValidationError;
import b33hive.shared.account.bhE_SignUpCredentialType;
import b33hive.shared.account.bhE_SignUpValidationError;
import b33hive.shared.account.bhS_Account;
import b33hive.shared.account.bhSignInCredentials;
import b33hive.shared.account.bhSignInValidationResult;
import b33hive.shared.account.bhSignUpCredentials;
import b33hive.shared.account.bhSignUpValidationResult;

/**
 * 
 */
public class bhServerAccountManager
{
	private static final Logger s_logger = Logger.getLogger(bhServerAccountManager.class.getName());
	
	private static bhServerAccountManager s_instance = null;
	
	private static final String[] RESERVED_USERNAMES =
	{
		"admin",
		"_ah" // some app engine services potentially require this as a path...not sure, but just being safe.
	};
	
	private bhServerAccountManager()
	{
	}
	
	public static void startUp()
	{
		s_instance = new bhServerAccountManager();
	}
	
	public static bhServerAccountManager getInstance()
	{
		return s_instance;
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
			if( bhAccountDatabase.getInstance().isPasswordChangeTokenValid(tokenBytes, time) )
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
	public String setNewDesiredPassword(bhSignInCredentials credentials)
	{
		String email				= credentials.get(bhE_SignInCredentialType.EMAIL);
		String plainTextPassword	= credentials.get(bhE_SignInCredentialType.PASSWORD);
		
    	bhAccountDatabase database = bhAccountDatabase.getInstance();
    	
    	byte[] passwordSalt = bhU_Hashing.calcRandomSaltBytes(bhS_ServerAccount.PASSWORD_SALT_BYTE_LENGTH);
		byte[] passwordHash = bhU_Hashing.hashWithSalt(plainTextPassword, passwordSalt);
		byte[] changeToken = bhU_Hashing.calcRandomSaltBytes(bhS_ServerAccount.PASSWORD_CHANGE_TOKEN_BYTE_LENGTH);
		
    	try
    	{
    		Timestamp time = new Timestamp(new java.util.Date().getTime());
    		if( database.setNewDesiredPassword(email, passwordHash, passwordSalt, changeToken, time) )
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
	
	public bhUserSession attemptSignUp(bhSignUpValidationResult outResult, bhSignUpCredentials credentials)
	{
		String email				= credentials.get(bhE_SignUpCredentialType.EMAIL);
		String username				= credentials.get(bhE_SignUpCredentialType.USERNAME);
		String plainTextPassword	= credentials.get(bhE_SignUpCredentialType.PASSWORD);
		
		if( !isAllowedEmail(email) )
		{
			outResult.setError(bhE_SignUpCredentialType.EMAIL, bhE_SignUpValidationError.EMAIL_TAKEN);
			return null;
		}
		
		if( !isAllowedUsername(username) )
		{
			outResult.setError(bhE_SignUpCredentialType.USERNAME, bhE_SignUpValidationError.USERNAME_TAKEN);
			return null;
		}
		
		bhAccountDatabase database = bhAccountDatabase.getInstance();
		
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
				database.addAccount(id, email, username, passwordHash, passwordSalt, role);
				
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
						flags = database.containsSignUpCredentials(email, username);
					}
					catch(SQLException e2)
					{
						s_logger.log(Level.SEVERE, "Could not check which credentials already exist.", e1);
						
						outResult.setResponseError();
						
						return null;
					}
					
					if( flags != 0 )
					{
						if ( (flags & bhF_SignUpExistance.EMAIL_EXISTS) != 0 )
						{
							outResult.setError(bhE_SignUpCredentialType.EMAIL, bhE_SignUpValidationError.EMAIL_TAKEN);
						}
						
						if( (flags & bhF_SignUpExistance.USERNAME_EXISTS) != 0 )
						{
							outResult.setError(bhE_SignUpCredentialType.USERNAME, bhE_SignUpValidationError.USERNAME_TAKEN);
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
					
					outResult.setResponseError();
					
					return null;
				}
			}
		}
		
		//--- DRK > This means (should mean) that we tried bhS_AccountManagement.RETRY_COUNT_FOR_ID_UNIQUENESS times
		//---		to generate a unique id, and they all failed...since this is highly unlikely, this is logged in case
		//---		there's some other bug-related condition that makes it here.
		//---		This simply results in the user seeing a "server error" type of message, and they can try again with the same credentials.
		s_logger.warning("Exceeded retry count for finding a unique user id.");
		
		outResult.setResponseError();
		
		return null;
	}
	
	/*public bhUserSession createSession(int accountId)
	{
		bhAccountDatabase database = bhAccountDatabase.getInstance();
    	
    	try
    	{
        	bhUserSession userSession = database.containsAccountId(accountId);

        	return userSession;
    	}
    	catch(SQLException e)
    	{
    		s_logger.log(Level.SEVERE, "Error occured while getting session data for account: " + accountId, e);
    	}
    	
    	return null;
	}*/
	
	public bhUserSession confirmNewPassword(bhSignInValidationResult outResult, bhSignInCredentials credentials, String passwordResetToken)
    {
    	String email				= credentials.get(bhE_SignInCredentialType.EMAIL);
		String plainTextPassword	= credentials.get(bhE_SignInCredentialType.PASSWORD);
		
    	bhAccountDatabase database = bhAccountDatabase.getInstance();
    	
    	try
    	{
	        byte[] salt = database.getPasswordSalt(email, E_PasswordType.NEW);
	        
	        if( salt == null )
	        {
	        	outResult.setResponseError();
	        }
	        else
	        {
	        	byte[] passwordHash	= bhU_Hashing.hashWithSalt(plainTextPassword, salt);
	        	Timestamp time = getPasswordChangeTokenTimestamp();
	        	byte[] changeTokenBytes = bhU_Hashing.convertHashStringToBytes(passwordResetToken);
	            
	        	bhUserSession userSession = database.confirmNewPassword(email, passwordHash, salt, changeTokenBytes, time);
	        	
	            if (userSession == null)
	            {
	            	outResult.setResponseError();
	            }
	            
	            return userSession;
	        }
    	}
    	catch(SQLException e)
    	{
    		s_logger.log(Level.SEVERE, "Could not sign user in with new password.", e);
    		
    		outResult.setResponseError();
    	}
    	
    	return null;
    }

	public bhUserSession attemptSignIn(bhSignInValidationResult outResult, bhSignInCredentials credentials)
    {
    	String email				= credentials.get(bhE_SignInCredentialType.EMAIL);
		String plainTextPassword	= credentials.get(bhE_SignInCredentialType.PASSWORD);
		
    	bhAccountDatabase database = bhAccountDatabase.getInstance();
    	
    	try
    	{
	        byte[] salt = database.getPasswordSalt(email, E_PasswordType.CURRENT);
	        
	        if( salt == null )
	        {
	        	outResult.setBadCombinationError();
	        }
	        else
	        {
	        	byte[] passwordHash	= bhU_Hashing.hashWithSalt(plainTextPassword, salt);
	            
	        	bhUserSession userSession = database.containsSignInCredentials(email, passwordHash);
	            if (userSession == null)
	            {
	            	outResult.setBadCombinationError();
	            }
	            
	            return userSession;
	        }
    	}
    	catch(SQLException e)
    	{
    		s_logger.log(Level.SEVERE, "Could not sign user in.", e);
    		outResult.setResponseError();
    	}
    	
    	return null;
    }
}
