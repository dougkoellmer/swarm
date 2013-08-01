package com.b33hive.shared.account;

import com.b33hive.shared.app.bhS_App;

public class bhU_Account
{
	public static void cropPassword(String[] credentials, int index)
	{
		if( credentials[index].length() > bhS_Account.MAX_PASSWORD_LENGTH )
		{
			credentials[index] = credentials[index].substring(0, bhS_Account.MAX_PASSWORD_LENGTH);
		}
	}
}
