package b33hive.shared.account;

public enum bhE_SignUpCredentialType implements bhI_CredentialType
{
	EMAIL(bhI_SignUpCredentialValidator.EMAIL_VALIDATOR),
	USERNAME(bhI_SignUpCredentialValidator.USERNAME_VALIDATOR),
	PASSWORD(bhI_SignUpCredentialValidator.PASSWORD_VALIDATOR),
	CAPTCHA_RESPONSE(bhI_SignUpCredentialValidator.CAPTCHA_RESPONSE_VALIDATOR);
	
	private final bhI_SignUpCredentialValidator m_validator;
	
	private bhE_SignUpCredentialType(bhI_SignUpCredentialValidator validator)
	{
		m_validator = validator;
	}
	
	public bhI_SignUpCredentialValidator getValidator()
	{
		return m_validator;
	}
}
