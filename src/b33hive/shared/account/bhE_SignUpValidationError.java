package b33hive.shared.account;

public enum bhE_SignUpValidationError implements bhI_ValidationError
{
	NO_ERROR,
	
	EMPTY,
	
	PASSWORD_TOO_SHORT,
	
	USERNAME_TOO_SHORT, // not used for now, but maybe could be in future, who knows.
	USERNAME_TOO_LONG,
	USERNAME_INVALID,
	USERNAME_TAKEN, // only generated on server
	
	EMAIL_TOO_LONG,
	EMAIL_INVALID,
	EMAIL_TAKEN, // only generated on server

	CAPTCHA_INCORRECT,
	
	RESPONSE_ERROR; // for any kind of response error not really related to validation
	
	private bhE_SignUpValidationError()
	{
	}
	
	/**
	 * These aren't directly associated with the enums so that in the future we can support dynamic localization.
	 * 
	 * @param error
	 * @return
	 */
	@Override
	public String calcErrorText(bhI_CredentialType credential)
	{
		switch(this)
		{
			case NO_ERROR:
			{
				return credential != bhE_SignUpCredentialType.CAPTCHA_RESPONSE ? "Good" : "Good (maybe)";
			}
			
			case EMPTY:					return "Can't be empty";
			
			case PASSWORD_TOO_SHORT:	return "Too short";
			
			case USERNAME_TOO_SHORT:	return "Too short";
			case USERNAME_TOO_LONG:		return "Too long";
			case USERNAME_INVALID:		return "Invalid characters";
			case USERNAME_TAKEN:		return "Already taken";
			
			case EMAIL_TOO_LONG:		return "Too long";
			case EMAIL_INVALID:			return "Seems invalid";
			case EMAIL_TAKEN:			return "Already taken";
			
			case CAPTCHA_INCORRECT:		return "Nope, try again";
			
			case RESPONSE_ERROR:		return "Connection error";
		}
		
		return null;
	}
	
	@Override
	public boolean isError()
	{
		return this != NO_ERROR;
	}

	@Override
	public boolean isEmptyError()
	{
		return this == EMPTY;
	}
	
	public boolean isServerGeneratedError()
	{
		return this == USERNAME_TAKEN || this == EMAIL_TAKEN || this == CAPTCHA_INCORRECT;
	}
	
	@Override
	public boolean isRetryable()
	{
		return this == RESPONSE_ERROR;
	}
}
