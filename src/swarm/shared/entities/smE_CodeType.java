package swarm.shared.entities;

import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonKeySource;

public enum smE_CodeType
{
	SOURCE				(smE_JsonKey.codeSource),
	SPLASH				(smE_JsonKey.codeSplash),
	COMPILED			(smE_JsonKey.codeCompiled);
	
	
	private final smI_JsonKeySource m_jsonKey;
	
	private smE_CodeType(smI_JsonKeySource key)
	{
		m_jsonKey = key;
	}
	
	public smI_JsonKeySource getJsonKey()
	{
		return m_jsonKey;
	}
}
