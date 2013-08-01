package com.b33hive.shared.account;

public enum bhE_SignInCredentialType implements bhI_CredentialType
{
	EMAIL(bhI_SignInCredentialValidator.EMAIL_VALIDATOR),
	PASSWORD(bhI_SignInCredentialValidator.PASSWORD_VALIDATOR);
	
	private final bhI_SignInCredentialValidator m_validator;
	
	private bhE_SignInCredentialType(bhI_SignInCredentialValidator validator)
	{
		m_validator = validator;
	}
	
	public bhI_SignInCredentialValidator getValidator()
	{
		return m_validator;
	}
}
