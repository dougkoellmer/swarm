package swarm.shared.entities;

import swarm.shared.lang.I_SerializableEnum;

public enum E_CodeSafetyLevel implements I_SerializableEnum
{
	//---TODO(DRK):	At some point it will be ok to get rid of some "old names",
	//				like if we manually update all db entities to latest versions.
	
	NO_SANDBOX_STATIC("NO_SANDBOX", "SAFE"),
	NO_SANDBOX_DYNAMIC(),
	VIRTUAL_STATIC_SANDBOX("REQUIRES_STATIC_SANDBOX", "REQUIRES_SANDBOX"),
	VIRTUAL_DYNAMIC_SANDBOX("REQUIRES_VIRTUAL_SANDBOX", "REQUIRES_DYNAMIC_SANDBOX"),
	LOCAL_SANDBOX,
	REMOTE_SANDBOX,
	META_IMAGE;
	
	private String m_oldNames[];
	
	private E_CodeSafetyLevel(String ... oldNames)
	{
		m_oldNames = oldNames;
	}
	
	public boolean isStatic()
	{
		return this == NO_SANDBOX_STATIC || this == VIRTUAL_STATIC_SANDBOX || this == META_IMAGE;
	}
	
	public boolean isVirtual()
	{
		return this == VIRTUAL_STATIC_SANDBOX || this == VIRTUAL_DYNAMIC_SANDBOX;
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