package com.b33hive.shared.structs;

import com.b33hive.shared.bhI_SerializableEnum;

public enum bhE_NetworkPrivilege implements bhI_SerializableEnum
{
	IMAGES ("NONE"),
	ALL;
	
	private String m_oldNames[];
	
	private bhE_NetworkPrivilege(String ... oldNames)
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
