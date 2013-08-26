package swarm.shared.code;

import swarm.shared.app.sm;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;

public class smFileRange extends smA_JsonEncodable
{
	public static enum E_Position
	{
		START, END;
	}
	
	private Integer[] m_range = null;
	
	public smFileRange(smI_JsonObject json)
	{
		super(json);
	}
	
	public smFileRange(int startLine, int startColumn, int endLine, int endColumn)
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
	public void writeJson(smI_JsonObject json)
	{
		sm.jsonFactory.getHelper().putJavaVarArgs(json, smE_JsonKey.fileRange, (Object[])m_range);
	}

	@Override
	public void readJson(smI_JsonObject json)
	{
		m_range = new Integer[4];
		smI_JsonArray range = sm.jsonFactory.getHelper().getJsonArray(json, smE_JsonKey.fileRange);
		
		for( int i = 0; i < range.getSize(); i++ )
		{
			m_range[i] = range.getInt(i);
		}
	}
}
