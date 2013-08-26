package swarm.shared.account;

import swarm.shared.utils.smU_Regex;

public class smSignUpCredentialValidator implements smI_SignUpCredentialValidator
{
	private final int m_maxCellAddressPartLength;
	
	public smSignUpCredentialValidator(int maxCellAddressPartLength)
	{
		m_maxCellAddressPartLength = maxCellAddressPartLength;
	}
	
	@Override
	public smE_SignUpValidationError validateCredential(String credential)
	{
		if( credential.length() == 0 )
		{
			return smE_SignUpValidationError.EMPTY;
		}
		else if( credential.length() > m_maxCellAddressPartLength )
		{
			return smE_SignUpValidationError.USERNAME_TOO_LONG;
		}
		else if( !bhU_Regex.calcIsMatch(credential, smS_Account.USERNAME_REGEX) )
		{
			return smE_SignUpValidationError.USERNAME_INVALID;
		}
		
		return smE_SignUpValidationError.NO_ERROR;
	}

}
