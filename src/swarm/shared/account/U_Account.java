package swarm.shared.account;

import swarm.shared.app.S_CommonApp;

public class U_Account
{
	public static void cropPassword(String[] credentials, int index)
	{
		if( credentials[index].length() > S_Account.MAX_PASSWORD_LENGTH )
		{
			credentials[index] = credentials[index].substring(0, S_Account.MAX_PASSWORD_LENGTH);
		}
	}
}
