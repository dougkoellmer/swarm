package swarm.shared.code;

import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

public class CompilerMessage extends A_JsonEncodable
{
	private E_CompilerMessageLevel m_level;
	private String m_message;
	private FileRange m_range;
	
	public CompilerMessage(E_CompilerMessageLevel type, String message, FileRange range)
	{
		m_level = type;
		m_message = message;
		m_range = range;
	}
	
	public E_CompilerMessageLevel getLevel()
	{
		return m_level;
	}
	
	public CompilerMessage(A_JsonFactory jsonFactory, I_JsonObject json)
	{
		super(jsonFactory, json);
	}
	
	public String getMessage()
	{
		return m_message;
	}
	
	public FileRange getFileRange()
	{
		return m_range;
	}
	
	@Override
	public void writeJson(I_JsonObject json_out, A_JsonFactory factory)
	{
		factory.getHelper().putString(json_out, E_JsonKey.compilerErrorMessage, m_message);
		factory.getHelper().putEnum(json_out, E_JsonKey.compilerErrorLevel, m_level);
		m_range.writeJson(json_out, factory);
	}

	@Override
	public void readJson(I_JsonObject json, A_JsonFactory factory)
	{
		m_message = factory.getHelper().getString(json, E_JsonKey.compilerErrorMessage);
		m_level = factory.getHelper().getEnum(json, E_JsonKey.compilerErrorLevel, E_CompilerMessageLevel.values());
		
		m_range = new FileRange(factory, json);
	}
}
