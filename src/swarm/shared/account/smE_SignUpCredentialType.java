package swarm.shared.account;

public enum smE_SignUpCredentialType implements smI_CredentialType
{
	EMAIL(smI_SignUpCredentialValidator.EMAIL_VALIDATOR),
	USERNAME(smI_SignUpCredentialValidator.USERNAME_VALIDATOR),
	PASSWORD(smI_SignUpCredentialValidator.PASSWORD_VALIDATOR),
	CAPTCHA_RESPONSE(smI_SignUpCredentialValidator.CAPTCHA_RESPONSE_VALIDATOR);
	
	private final smI_SignUpCredentialValidator m_validator;
	
	private smE_SignUpCredentialType(smI_SignUpCredentialValidator validator)
	{
		m_validator = validator;
	}
	
	public smI_SignUpCredentialValidator getValidator()
	{
		return m_validator;
	}
}
