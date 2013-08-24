package swarm.server.account;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import swarm.server.data.sql.bhA_SqlDatabase;
import swarm.server.data.sql.bhU_Sql;
import swarm.shared.utils.bhU_TypeConversion;
import com.google.appengine.api.rdbms.AppEngineDriver;

/**
 * Abstraction for dealing with the account database.
 */
public class bhAccountDatabase extends bhA_SqlDatabase
{
	public static enum E_PasswordType
	{
		CURRENT, NEW;
	}

	private final static Logger s_logger = Logger.getLogger(bhAccountDatabase.class.getName());

	public bhAccountDatabase(String databaseUrl, String databaseName)
	{
		super(databaseUrl, databaseName);
	}

	public void addAccount(int id, String email, String username, byte[] passwordHash, byte[] passwordSalt, bhE_Role role) throws SQLException
	{
		Connection connection = getConnection();

		PreparedStatement statement = connection.prepareStatement(bhS_AccountQuery.ADD_ACCOUNT);

		statement.setInt(1,		id);
		statement.setString(2,	email);
		statement.setString(3,	username);
		statement.setBytes(4,	passwordHash);
		statement.setBytes(5,	passwordSalt);
		statement.setString(6,	bhU_TypeConversion.convertEnumToString(role));

		statement.executeUpdate();
	}

	public byte[] getPasswordSalt(String email, E_PasswordType passwordType) throws SQLException
	{
		byte[] salt = null;
		
		Connection connection = getConnection();
		
		String query = passwordType == E_PasswordType.CURRENT ? bhS_AccountQuery.GET_SALT : bhS_AccountQuery.GET_NEW_SALT;
		
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, email);
		ResultSet result = statement.executeQuery();

		if (result.next())
		{
			salt = result.getBytes(1);
		}

		return salt;
	}
	
	public int containsSignUpCredentials(String email, String username) throws SQLException
	{
		int flags = 0;
		
		Connection connection = getConnection();
			
		PreparedStatement statement = connection.prepareStatement(bhS_AccountQuery.CONTAINS_SIGNUP);
		statement.setString(1, email);
		statement.setString(2, username);
		ResultSet result = statement.executeQuery();

		while(result.next())
		{
			String resultEmail = result.getString(1);
			if( resultEmail != null )
			{
				if( resultEmail.equals(email) )
				{
					flags |= bhF_SignUpExistance.EMAIL_EXISTS;
				}
			}
			
			String resultUsername = result.getString(2);
			if( resultUsername != null )
			{
				if( resultUsername.equals(username) )
				{
					flags |= bhF_SignUpExistance.USERNAME_EXISTS;
				}
			}
		}
		
		return flags;
	}
	
	/*public bhUserSession containsAccountId(int accountId) throws SQLException
	{
		Connection connection = null;
		
		bhUserSession userSession = null;
		connection = getConnection();
		
		PreparedStatement statement = connection.prepareStatement(bhS_AccountQuery.CONTAINS_ACCOUNT_ID);
		statement.setInt(1, accountId);

		ResultSet result = statement.executeQuery();

		if (result.next())
		{
			int id = result.getInt(1);
			String username = result.getString(2);
			String roleName = result.getString(3);
			bhE_Role role = bhU_TypeConversion.convertStringToEnum(roleName, bhE_Role.values());
			
			userSession = new bhUserSession(id, username, role);
		}

		return userSession;
	}*/
	
	private bhUserSession getUserSessionFromSignInQuery(PreparedStatement statement) throws SQLException
	{
		ResultSet result = statement.executeQuery();

		if (result.next())
		{
			int id = result.getInt(1);
			String username = result.getString(2);
			String roleName = result.getString(3);
			bhE_Role role = bhU_TypeConversion.convertStringToEnum(roleName, bhE_Role.values());
			
			return new bhUserSession(id, username, role);
		}
		
		return null;
	}

	public bhUserSession containsSignInCredentials(String email, byte[] passwordHash) throws SQLException
	{
		Connection connection = getConnection();
		
		PreparedStatement statement = connection.prepareStatement(bhS_AccountQuery.CONTAINS_SIGNIN);
		statement.setString(1, email);
		statement.setBytes(2, passwordHash);

		return getUserSessionFromSignInQuery(statement);
	}
	
	public bhUserSession confirmNewPassword(String email, byte[] newPasswordHash, byte[] newPasswordSalt, byte[] changeToken, Timestamp expirationThreshold) throws SQLException
	{
		Connection connection = getConnection();
		
		PreparedStatement statement = connection.prepareStatement(bhS_AccountQuery.CONTAINS_NEW_SIGNIN);
		statement.setString(1, email);
		statement.setBytes(2, newPasswordHash);
		statement.setBytes(3, changeToken);
		statement.setTimestamp(4, expirationThreshold);

		bhUserSession userSession = getUserSessionFromSignInQuery(statement);
		
		if( userSession != null )
		{
			statement = connection.prepareStatement(bhS_AccountQuery.SWITCH_TO_NEW_PASSWORD);
			statement.setBytes(1, newPasswordHash);
			statement.setBytes(2, newPasswordSalt);
			statement.setBytes(3, null);
			statement.setBytes(4, null);
			statement.setBytes(5, null);
			statement.setTimestamp(6, null);
			statement.setString(7, email);
			statement.setBytes(8, newPasswordHash);
			
			if( !bhU_Sql.isSuccessfulUpdate(statement.executeUpdate()) )
			{
				s_logger.warning("Was able to confirm new password, but couldn't then switch password.");
				
				userSession = null;
			}
		}

		return userSession;
	}
	
	public boolean isPasswordChangeTokenValid(byte[] changeToken, Timestamp expirationThreshold) throws SQLException
	{
		Connection connection = getConnection();
		
		PreparedStatement statement = connection.prepareStatement(bhS_AccountQuery.IS_PASSWORD_CHANGE_TOKEN_VALID);
		
		statement.setBytes(1, changeToken);
		statement.setTimestamp(2, expirationThreshold);

		ResultSet result = statement.executeQuery();
		if( result.next() )
		{
			boolean found = result.getInt("count") > 0;
			return found;
		}
		else
		{
			return false;
		}
	}
	
	public boolean setNewDesiredPassword(String email, byte[] passwordHash, byte[] passwordSalt, byte[] changeToken, Timestamp time) throws SQLException
	{
		Connection connection = getConnection();
		
		PreparedStatement statement = connection.prepareStatement(bhS_AccountQuery.SET_NEW_DESIRED_PASSWORD);
		
		statement.setBytes(1, passwordHash);
		statement.setBytes(2, passwordSalt);
		statement.setBytes(3, changeToken);
		statement.setTimestamp(4, time);
		statement.setString(5, email);

		return bhU_Sql.isSuccessfulUpdate(statement.executeUpdate());
	}
}
