package swarm.shared.account;

import swarm.shared.utils.bhU_Regex;

public class bhSignUpCredentialValidator implements bhI_SignUpCredentialValidator
{
	private final int m_maxCellAddressPartLength;
	
	public bhSignUpCredentialValidator(int maxCellAddressPartLength)
	{
		m_maxCellAddressPartLength = maxCellAddressPartLength;
	}
	
	@Override
	public bhE_SignUpValidationError validateCredential(String credential)
	{
		if( credential.length() == 0 )
		{
			return bhE_SignUpValidationError.EMPTY;
		}
		else if( credential.length() > m_maxCellAddressPartLength )
		{
			return bhE_SignUpValidationError.USERNAME_TOO_LONG;
		}
		else if( !bhU_Regex.calcIsMatch(credential, bhS_Account.USERNAME_REGEX) )
		{
			return bhE_SignUpValidationError.USERNAME_INVALID;
		}
		
		return bhE_SignUpValidationError.NO_ERROR;
	}

}
