package swarm.shared.utils;

import swarm.shared.lang.smI_SerializableEnum;

public class smU_TypeConversion
{
	private bhU_TypeConversion()
	{
		
	}
	
	public static String convertEnumToString(Enum enumValue)
	{
		return enumValue.name();
	}
	
	public static <T extends Enum> T convertStringToEnum(String enumName, T[] enumValues)
	{
		for( int i = 0; i < enumValues.length; i++ )
		{
			if( enumName.equals(enumValues[i].name()) )
			{
				return enumValues[i];
			}
		}
		
		if( enumValues.length > 0 && (enumValues[0] instanceof smI_SerializableEnum) )
		{
			for( int i = 0; i < enumValues.length; i++ )
			{
				if( ((smI_SerializableEnum)enumValues[i]).matchesOldName(enumName) )
				{
					return enumValues[i];
				}
			}
		}
			
		return null;
	}
}
