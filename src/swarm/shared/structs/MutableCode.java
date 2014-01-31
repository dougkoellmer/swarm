package swarm.shared.structs;

import swarm.shared.entities.E_CodeType;
import swarm.shared.json.I_JsonObject;

public class MutableCode extends Code
{
	public MutableCode(E_CodeType ... types)
	{
		super("", types);
	}
	
	public void setRawCode(String rawCode)
	{
		m_rawCode = rawCode;
	}
}
