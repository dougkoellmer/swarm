package b33hive.client.ui;

import b33hive.client.managers.bhClientAccountManager;
import b33hive.client.managers.bhClientAccountManager.E_ResponseType;

public class bhU_ToString
{
	public static String get(bhClientAccountManager.E_ResponseType input)
	{
		switch(input)
		{
			case SIGN_IN_FAILURE:				return "Failed to sign in!";
			case SIGN_IN_SUCCESS:				return "Successfully signed in!";
			case SIGN_UP_FAILURE:				return "Failed to sign up!";
			case SIGN_UP_SUCCESS:				return "Successfully signed up!";
			case SIGN_OUT_FAILURE:				return "Failed to sign out!";
			case SIGN_OUT_SUCCESS:				return "Successfully signed out!";
			case PASSWORD_CHANGE_FAILURE:		return "Password change failed!";
		}
		
		return null; // returning null implies that there's a more explicit way that the response should be communicated to user (dialog, etc.).
	}
}
