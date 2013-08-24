package swarm.shared.code;

import swarm.shared.app.sm;
import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;

public class bhCompilerMessage extends bhA_JsonEncodable
{
	private bhE_CompilerMessageLevel m_level;
	private String m_message;
	private bhFileRange m_range;
	
	public bhCompilerMessage(bhE_CompilerMessageLevel type, String message, bhFileRange range)
	{
		m_level = type;
		m_message = message;
		m_range = range;
	}
	
	public bhE_CompilerMessageLevel getLevel()
	{
		return m_level;
	}
	
	public bhCompilerMessage(bhI_JsonObject json)
	{
		super(json);
	}
	
	public String getMessage()
	{
		return m_message;
	}
	
	public bhFileRange getFileRange()
	{
		return m_range;
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		sm.jsonFactory.getHelper().putString(json, bhE_JsonKey.compilerErrorMessage, m_message);
		sm.jsonFactory.getHelper().putEnum(json, bhE_JsonKey.compilerErrorLevel, m_level);
		m_range.writeJson(json);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_message = sm.jsonFactory.getHelper().getString(json, bhE_JsonKey.compilerErrorMessage);
		m_level = sm.jsonFactory.getHelper().getEnum(json, bhE_JsonKey.compilerErrorLevel, bhE_CompilerMessageLevel.values());
		
		m_range = new bhFileRange(json);
	}
}
