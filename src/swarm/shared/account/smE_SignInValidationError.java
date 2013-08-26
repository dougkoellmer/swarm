package swarm.shared.account;

public enum smE_SignInValidationError implements smI_ValidationError
{
	NO_ERROR,
	
	EMPTY,
	
	PASSWORD_TOO_SHORT, // used when setting new password

	UNKNOWN_COMBINATION,
	
	RESPONSE_ERROR; // for any kind of unknown response error
	
	private smE_SignInValidationError()
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
			case NO_ERROR:				return "";
			
			case EMPTY:					return "Can't be empty";
			
			case PASSWORD_TOO_SHORT:	return "Too short";
			
			case UNKNOWN_COMBINATION:	return "Unknown combo";
			
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

	@Override
	public boolean isServerGeneratedError()
	{
		return this == UNKNOWN_COMBINATION;
	}

	@Override
	public boolean isRetryable()
	{
		return this == RESPONSE_ERROR;
	}
}
