package swarm.shared.entities;

import swarm.shared.lang.smI_SerializableEnum;

public enum smE_CodeSafetyLevel implements smI_SerializableEnum
{
	SAFE,
	REQUIRES_STATIC_SANDBOX("REQUIRES_SANDBOX"),
	REQUIRES_DYNAMIC_SANDBOX;
	
	private String m_oldNames[];
	
	private smE_CodeSafetyLevel(String ... oldNames)
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