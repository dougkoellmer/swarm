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

import swarm.server.data.sql.smA_SqlDatabase;
import swarm.server.data.sql.smU_Sql;
import swarm.shared.utils.smU_TypeConversion;
import com.google.appengine.api.rdbms.AppEngineDriver;

/**
 * Abstraction for dealing with the account database.
 */
public interface smI_AccountDatabase
{
	public static enum E_PasswordType
	{
		CURRENT, NEW;
	}

	void addAccount(int id, String email, String username, byte[] passwordHash, byte[] passwordSalt, smE_Role role) throws SQLException;

	byte[] getPasswordSalt(String email, E_PasswordType passwordType) throws SQLException;
	
	int containsSignUpCredentials(String email, String username) throws SQLException;

	smUserSession containsSignInCredentials(String email, byte[] passwordHash) throws SQLException;
	
	smUserSession confirmNewPassword(String email, byte[] newPasswordHash, byte[] newPasswordSalt, byte[] changeToken, Timestamp expirationThreshold) throws SQLException;
	
	boolean isPasswordChangeTokenValid(byte[] changeToken, Timestamp expirationThreshold) throws SQLException;
	
	boolean setNewDesiredPassword(String email, byte[] passwordHash, byte[] passwordSalt, byte[] changeToken, Timestamp time) throws SQLException;
}
