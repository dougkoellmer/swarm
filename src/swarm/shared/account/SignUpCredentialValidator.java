package swarm.shared.account;

import swarm.shared.utils.U_Regex;

public class SignUpCredentialValidator implements I_SignUpCredentialValidator
{
	private final int m_maxCellAddressPartLength;
	
	public SignUpCredentialValidator(int maxCellAddressPartLength)
	{
		m_maxCellAddressPartLength = maxCellAddressPartLength;
	}
	
	@Override
	public E_SignUpValidationError validateCredential(String credential)
	{
		if( credential.length() == 0 )
		{
			return E_SignUpValidationError.EMPTY;
		}
		else if( credential.length() > m_maxCellAddressPartLength )
		{
			return E_SignUpValidationError.USERNAME_TOO_LONG;
		}
		else if( !U_Regex.calcIsMatch(credential, S_Account.USERNAME_REGEX) )
		{
			return E_SignUpValidationError.USERNAME_INVALID;
		}
		
		return E_SignUpValidationError.NO_ERROR;
	}

}
