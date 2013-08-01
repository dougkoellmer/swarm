package com.b33hive.server.session;

public class bhS_Session
{
	public static final long SESSION_TIMEOUT		= 86400*30; // 30 days, in seconds
	public static final int SESSION_TOKEN_BYTES		= 16;
	
	public static final String ACCOUNT_ID_PROPERTY		= "account_id";
	public static final String DATE_PROPERTY			= "last_touched";
}
