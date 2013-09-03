package swarm.shared.code;

import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

public class smCompilerMessage extends smA_JsonEncodable
{
	private smE_CompilerMessageLevel m_level;
	private String m_message;
	private smFileRange m_range;
	
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
	
	public smCompilerMessage(smA_JsonFactory jsonFactory, smI_JsonObject json)
	{
		super(jsonFactory, json);
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
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		factory.getHelper().putString(json_out, smE_JsonKey.compilerErrorMessage, m_message);
		factory.getHelper().putEnum(json_out, smE_JsonKey.compilerErrorLevel, m_level);
		m_range.writeJson(factory, json_out);
	}

	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		m_message = factory.getHelper().getString(json, smE_JsonKey.compilerErrorMessage);
		m_level = factory.getHelper().getEnum(json, smE_JsonKey.compilerErrorLevel, smE_CompilerMessageLevel.values());
		
		m_range = new smFileRange(factory, json);
	}
}
