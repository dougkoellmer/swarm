package swarm.shared.code;

import swarm.shared.app.sm;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

public class smCompilerMessage extends smA_JsonEncodable
{
	private smE_CompilerMessageLevel m_level;
	private String m_message;
	private bhFileRange m_range;
	
	public smCompilerMessage(smE_CompilerMessageLevel type, String message, smFileRange range)
	{
		m_level = type;
		m_message = message;
		m_range = range;
	}
	
	public smE_CompilerMessageLevel getLevel()
	{
		return m_level;
	}
	
	public smCompilerMessage(smI_JsonObject json)
	{
		super(json);
	}
	
	public String getMessage()
	{
		return m_message;
	}
	
	public smFileRange getFileRange()
	{
		return m_range;
	}
	
	@Override
	public void writeJson(smI_JsonObject json)
	{
		sm.jsonFactory.getHelper().putString(json, smE_JsonKey.compilerErrorMessage, m_message);
		sm.jsonFactory.getHelper().putEnum(json, smE_JsonKey.compilerErrorLevel, m_level);
		m_range.writeJson(json);
	}

	@Override
	public void readJson(smI_JsonObject json)
	{
		m_message = sm.jsonFactory.getHelper().getString(json, smE_JsonKey.compilerErrorMessage);
		m_level = sm.jsonFactory.getHelper().getEnum(json, smE_JsonKey.compilerErrorLevel, smE_CompilerMessageLevel.values());
		
		m_range = new smFileRange(json);
	}
}
