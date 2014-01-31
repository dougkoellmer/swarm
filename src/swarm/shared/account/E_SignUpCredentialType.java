package swarm.shared.account;

public enum E_SignUpCredentialType implements I_CredentialType
{
	EMAIL(I_SignUpCredentialValidator.EMAIL_VALIDATOR),
	USERNAME(I_SignUpCredentialValidator.USERNAME_VALIDATOR),
	PASSWORD(I_SignUpCredentialValidator.PASSWORD_VALIDATOR),
	CAPTCHA_RESPONSE(I_SignUpCredentialValidator.CAPTCHA_RESPONSE_VALIDATOR);
	
	private final I_SignUpCredentialValidator m_validator;
	
	private E_SignUpCredentialType(I_SignUpCredentialValidator validator)
	{
		m_validator = validator;
	}
	
	public I_SignUpCredentialValidator getValidator()
	{
		return m_validator;
	}
}
