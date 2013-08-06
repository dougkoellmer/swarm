package b33hive.shared.code;

import b33hive.shared.app.bh;
import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;

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
		bh.jsonFactory.getHelper().putString(json, bhE_JsonKey.compilerErrorMessage, m_message);
		bh.jsonFactory.getHelper().putEnum(json, bhE_JsonKey.compilerErrorLevel, m_level);
		m_range.writeJson(json);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_message = bh.jsonFactory.getHelper().getString(json, bhE_JsonKey.compilerErrorMessage);
		m_level = bh.jsonFactory.getHelper().getEnum(json, bhE_JsonKey.compilerErrorLevel, bhE_CompilerMessageLevel.values());
		
		m_range = new bhFileRange(json);
	}
}
