package com.b33hive.shared.structs;

import com.b33hive.shared.entities.bhE_CodeType;
import com.b33hive.shared.json.bhI_JsonObject;

public class bhMutableCode extends bhCode
{
	public bhMutableCode(bhE_CodeType ... types)
	{
		super("", types);
	}
	
	public void setRawCode(String rawCode)
	{
		m_rawCode = rawCode;
	}
}
