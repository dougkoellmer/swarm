package swarm.server.account;

public class bhS_AccountQuery
{
	//--- DRK > General queries for signing in/up.
	final static String ADD_ACCOUNT						= "INSERT INTO account (id, email, username, password, salt, role) VALUES(?,?,?,?,?,?);";
	final static String GET_SALT						= "SELECT salt FROM account WHERE email=?;";
	final static String CONTAINS_SIGNIN					= "SELECT id, username, role FROM account WHERE email=? AND password=?;";
	final static String CONTAINS_ACCOUNT_ID				= "SELECT username, role FROM account WHERE id=?";
	final static String CONTAINS_SIGNUP					= "SELECT email, username FROM account WHERE email=? OR username=?;";
	
	//--- DRK > All the crap for resetting password...hmm, that's a lot.
	final static String GET_NEW_SALT					= "SELECT new_salt FROM account WHERE email=?;";
	final static String SET_NEW_DESIRED_PASSWORD		= "UPDATE account SET new_password=?, new_salt=?, password_change_token=?, password_change_date=? WHERE email=?;";
	final static String IS_PASSWORD_CHANGE_TOKEN_VALID	= "SELECT COUNT(*) as count FROM account WHERE password_change_token=? AND password_change_date>?;";
	final static String CONTAINS_NEW_SIGNIN				= "SELECT id, username, role FROM account WHERE email=? AND new_password=? AND password_change_token=? AND password_change_date>?;";
	final static String SWITCH_TO_NEW_PASSWORD			= "UPDATE account SET password=?, salt=?, new_password=?, new_salt=?, password_change_token=?, password_change_date=? WHERE email=? AND new_password=?;";
}