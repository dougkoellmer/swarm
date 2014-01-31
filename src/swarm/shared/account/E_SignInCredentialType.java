package swarm.shared.account;

public enum E_SignInCredentialType implements I_CredentialType
{
	EMAIL(I_SignInCredentialValidator.EMAIL_VALIDATOR),
	PASSWORD(I_SignInCredentialValidator.PASSWORD_VALIDATOR);
	
	private final I_SignInCredentialValidator m_validator;
	
	private E_SignInCredentialType(I_SignInCredentialValidator validator)
	{
		m_validator = validator;
	}
	
	public I_SignInCredentialValidator getValidator()
	{
		return m_validator;
	}
}
