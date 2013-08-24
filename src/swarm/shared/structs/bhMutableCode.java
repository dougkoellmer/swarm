package swarm.shared.structs;

import swarm.shared.entities.bhE_CodeType;
import swarm.shared.json.bhI_JsonObject;

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
