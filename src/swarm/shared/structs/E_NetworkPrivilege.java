package swarm.shared.structs;

import swarm.shared.lang.I_SerializableEnum;

public enum E_NetworkPrivilege implements I_SerializableEnum
{
	IMAGES ("NONE"),
	ALL;
	
	private String m_oldNames[];
	
	private E_NetworkPrivilege(String ... oldNames)
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
