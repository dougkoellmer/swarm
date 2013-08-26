package swarm.shared.account;

public enum smE_SignUpValidationError implements smI_ValidationError
{
	NO_ERROR,
	
	EMPTY,
	
	PASSWORD_TOO_SHORT,
	
	USERNAME_TOO_SHORT, // not used for now, but maybe could be in future, who knows.
	USERNAME_TOO_LONG, // generally should not be hit on client because UI enforces max length
	USERNAME_INVALID,
	USERNAME_TAKEN, // only generated on server
	
	EMAIL_TOO_LONG, // generally should not be client because UI enforces max length
	EMAIL_INVALID,
	EMAIL_TAKEN, // only generated on server

	CAPTCHA_INCORRECT, // only generated on server
	
	RESPONSE_ERROR; // for any kind of response error not really related to validation
	
	private smE_SignUpValidationError()
	{
	}
	
	/**
	 * These aren't directly associated with the enums so that in the future we can support dynamic localization.
	 * 
	 * @param error
	 * @return
	 */
	@Override
	public String calcErrorText(smI_CredentialType credential)
	{
		switch(this)
		{
			case NO_ERROR:
			{
				return credential != smE_SignUpCredentialType.CAPTCHA_RESPONSE ? "Good" : "Good (maybe)";
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
