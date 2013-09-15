package swarm.shared.entities;

import swarm.shared.lang.smI_SerializableEnum;

public enum smE_CodeSafetyLevel implements smI_SerializableEnum
{
	//---TODO(DRK):	At some point it will be ok to get rid of some "old names",
	//				like if we manually update all db entities to latest versions.
	
	NO_SANDBOX_STATIC("NO_SANDBOX", "SAFE"),
	NO_SANDBOX_DYNAMIC(),
	VIRTUAL_STATIC_SANDBOX("REQUIRES_STATIC_SANDBOX", "REQUIRES_SANDBOX"),
	VIRTUAL_DYNAMIC_SANDBOX("REQUIRES_VIRTUAL_SANDBOX", "REQUIRES_DYNAMIC_SANDBOX"),
	LOCAL_SANDBOX,
	REMOTE_SANDBOX;
	
	private String m_oldNames[];
	
	private smE_CodeSafetyLevel(String ... oldNames)
	{
		m_oldNames = oldNames;
	}
	
	public static boolean isStatic(smE_CodeSafetyLevel value)
	{
		return value == NO_SANDBOX_STATIC || value == VIRTUAL_STATIC_SANDBOX;
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