package swarm.shared.structs;

import swarm.shared.entities.smE_CodeType;
import swarm.shared.json.smI_JsonObject;

public class smMutableCode extends smCode
{
	public smMutableCode(smE_CodeType ... types)
	{
		super("", types);
	}
	
	public void setRawCode(String rawCode)
	{
		m_rawCode = rawCode;
	}
}
