package swarm.shared.utils;

import swarm.shared.lang.I_SerializableEnum;

public class U_TypeConversion
{
	private U_TypeConversion()
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
		
		if( enumValues.length > 0 && (enumValues[0] instanceof I_SerializableEnum) )
		{
			for( int i = 0; i < enumValues.length; i++ )
			{
				if( ((I_SerializableEnum)enumValues[i]).matchesOldName(enumName) )
				{
					return enumValues[i];
				}
			}
		}
			
		return null;
	}
}
