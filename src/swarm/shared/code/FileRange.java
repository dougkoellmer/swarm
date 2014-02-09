package swarm.shared.code;

import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;

public class FileRange extends A_JsonEncodable
{
	public static enum E_Position
	{
		START, END;
	}
	
	private Integer[] m_range = null;
	
	public FileRange(A_JsonFactory jsonFactory, I_JsonObject json)
	{
		super(jsonFactory, json);
	}
	
	public FileRange(int startLine, int startColumn, int endLine, int endColumn)
	{
		m_range = new Integer[4];
		
		m_range[0] = startLine;
		m_range[1] = startColumn;
		m_range[2] = endLine;
		m_range[3] = endColumn;
	}
	
	public int getLine(E_Position position)
	{
		return m_range[position.ordinal()*2];
	}
	
	public int getColumn(E_Position position)
	{
		return m_range[(position.ordinal()+1) * 2];
	}
	
	@Override
	public void writeJson(I_JsonObject json_out, A_JsonFactory factory)
	{
		factory.getHelper().putJavaVarArgs(factory, json_out, E_JsonKey.fileRange, (Object[])m_range);
	}

	@Override
	public void readJson(I_JsonObject json, A_JsonFactory factory)
	{
		m_range = new Integer[4];
		I_JsonArray range = factory.getHelper().getJsonArray(json, E_JsonKey.fileRange);
		
		for( int i = 0; i < range.getSize(); i++ )
		{
			m_range[i] = range.getInt(i);
		}
	}
}
