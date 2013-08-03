package b33hive.shared.entities;

import b33hive.shared.lang.bhI_SerializableEnum;

public enum bhE_CodeSafetyLevel implements bhI_SerializableEnum
{
	SAFE,
	REQUIRES_STATIC_SANDBOX("REQUIRES_SANDBOX"),
	REQUIRES_DYNAMIC_SANDBOX;
	
	private String m_oldNames[];
	
	private bhE_CodeSafetyLevel(String ... oldNames)
	{
		m_oldNames = oldNames;
	}

	@Override
	public boolean matchesOldName(String name)
	{		
		if( m_oldNames != null )
		{
			for( int i = 0; i < m_oldNames.length; i++ )
			{
				if( name.equals(m_oldNames[i]) )
				{
					return true;
				}
			}
		}
		
		return false;
	}
}