package swarm.shared.entities;

import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonKeySource;

public enum E_CodeType
{
	SOURCE				(E_JsonKey.codeSource),
	SPLASH				(E_JsonKey.codeSplash),
	COMPILED			(E_JsonKey.codeCompiled);
	
	
	private final I_JsonKeySource m_jsonKey;
	
	private E_CodeType(I_JsonKeySource key)
	{
		m_jsonKey = key;
	}
	
	public I_JsonKeySource getJsonKey()
	{
		return m_jsonKey;
	}
}
