package swarm.server.account;

public class S_ServerAccount
{
	static final int RETRY_COUNT_FOR_ID_UNIQUENESS				= 4;
	public static final int PASSWORD_SALT_BYTE_LENGTH					= 32;
	static final int DUPLICATE_ENTRY_ERROR_CODE					= 1062;
	static final int INVALID_USER_ID							= Integer.MIN_VALUE;
	static final int PASSWORD_CHANGE_TOKEN_BYTE_LENGTH			= PASSWORD_SALT_BYTE_LENGTH;
	
	public static String PASSWORD_CHANGE_TOKEN_PARAMETER_NAME	= "pct";
}
