package com.b33hive.shared.entities;

import com.b33hive.shared.json.bhE_JsonKey;
import com.b33hive.shared.json.bhI_JsonKeySource;

public enum bhE_CodeType
{
	SOURCE				(bhE_JsonKey.codeSource),
	SPLASH				(bhE_JsonKey.codeSplash),
	COMPILED			(bhE_JsonKey.codeCompiled);
	
	
	private final bhI_JsonKeySource m_jsonKey;
	
	private bhE_CodeType(bhI_JsonKeySource key)
	{
		m_jsonKey = key;
	}
	
	public bhI_JsonKeySource getJsonKey()
	{
		return m_jsonKey;
	}
}
