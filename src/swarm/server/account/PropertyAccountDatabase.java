package swarm.server.account;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletContext;


public class PropertyAccountDatabase implements I_AccountDatabase
{
	private static final Logger s_logger = Logger.getLogger(PropertyAccountDatabase.class.getName());
	
	private final String m_email;
	private final byte[] m_hash; 
	private final byte[] m_salt;
	
	public PropertyAccountDatabase(ServletContext servletContext, String propertiesFile)
	{
		InputStream is = servletContext.getResourceAsStream(propertiesFile);
		Properties props = new Properties();
		try
		{
			props.load(is);
		}
		catch (Exception e)
		{
			s_logger.severe("Couldn't load account properties file.");
		}
		
		String hashString = props.getProperty("hash");
		String saltString = props.getProperty("salt");
		m_email = props.getProperty("email");
		m_hash = U_Hashing.convertHashStringToBytes(hashString);
		m_salt = U_Hashing.convertHashStringToBytes(saltString);
	}

	@Override
	public void addAccount(int id, String email, String username, byte[] passwordHash, byte[] passwordSalt, E_Role role) throws SQLException
	{
		throw new SQLException("Sign Ups not allowed.");
	}

	@Override
	public byte[] getPasswordSalt(String email, E_PasswordType passwordType) throws SQLException
	{
		if( email.equals(m_email) && passwordType == E_PasswordType.CURRENT )
		{
			return m_salt;
		}
		else if( passwordType == E_PasswordType.NEW )
		{
			throw new SQLException("Password change not allowed.");
		}
		
		return null;
	}

	@Override
	public int containsSignUpCredentials(String email, String username) throws SQLException
	{
		throw new SQLException("Sign Ups not allowed.");
	}

	@Override
	public UserSession containsSignInCredentials(String email, byte[] passwordHash) throws SQLException
	{
		if( email.equals(m_email) && Arrays.equals(passwordHash, m_hash) )
		{
			return new UserSession(1, "admin", E_Role.ADMIN);
		}
		
		return null;
	}

	@Override
	public UserSession confirmNewPassword(String email, byte[] newPasswordHash, byte[] newPasswordSalt, byte[] changeToken, Timestamp expirationThreshold) throws SQLException
	{
		throw new SQLException("Password change not allowed.");
	}

	@Override
	public boolean isPasswordChangeTokenValid(byte[] changeToken, Timestamp expirationThreshold) throws SQLException
	{
		throw new SQLException("Password change not allowed.");
	}

	@Override
	public boolean setNewDesiredPassword(String email, byte[] passwordHash, byte[] passwordSalt, byte[] changeToken, Timestamp time) throws SQLException
	{
		throw new SQLException("Password change not allowed.");
	}
}
