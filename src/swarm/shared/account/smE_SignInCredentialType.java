package swarm.shared.account;

public enum smE_SignInCredentialType implements smI_CredentialType
{
	EMAIL(smI_SignInCredentialValidator.EMAIL_VALIDATOR),
	PASSWORD(smI_SignInCredentialValidator.PASSWORD_VALIDATOR);
	
	private final smI_SignInCredentialValidator m_validator;
	
	private smE_SignInCredentialType(smI_SignInCredentialValidator validator)
	{
		m_validator = validator;
	}
	
	public smI_SignInCredentialValidator getValidator()
	{
		return m_validator;
	}
}
