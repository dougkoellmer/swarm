package com.b33hive.shared;

public class bhU_TypeConversion
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
		
		if( enumValues.length > 0 && (enumValues[0] instanceof bhI_SerializableEnum) )
		{
			for( int i = 0; i < enumValues.length; i++ )
			{
				if( ((bhI_SerializableEnum)enumValues[i]).matchesOldName(enumName) )
				{
					return enumValues[i];
				}
			}
		}
			
		return null;
	}
}
