package swarm.shared.account;

import swarm.shared.app.S_CommonApp;

public class S_Account
{
	public static final int MIN_PASSWORD_LENGTH = 6;

	// not visibly enforced, but max length sent up to server, and truncated to on server for extra measure
	// against a DoS attack or something.
	public static final int MAX_PASSWORD_LENGTH = 255;

	public static final int MAX_USERNAME_LENGTH = 16;

	public static final int MAX_EMAIL_LENGTH = 255;

	public static final String USERNAME_REGEX = S_CommonApp.CELL_ADDRESS_REGEX;

	public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
	
	public static final int PASSWORD_RESET_TOKEN_EXPIRATION = 30; // How long, in minutes, a user has to confirm a new password.
}
