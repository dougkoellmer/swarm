package swarm.shared.account;

import swarm.shared.app.smS_App;

public class smU_Account
{
	public static void cropPassword(String[] credentials, int index)
	{
		if( credentials[index].length() > smS_Account.MAX_PASSWORD_LENGTH )
		{
			credentials[index] = credentials[index].substring(0, smS_Account.MAX_PASSWORD_LENGTH);
		}
	}
}
