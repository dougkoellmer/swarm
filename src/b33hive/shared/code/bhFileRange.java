package b33hive.shared.code;

import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;

public class bhFileRange extends bhA_JsonEncodable
{
	public static enum E_Position
	{
		START, END;
	}
	
	private Integer[] m_range = null;
	
	public bhFileRange(bhI_JsonObject json)
	{
		super(json);
	}
	
	public bhFileRange(int startLine, int startColumn, int endLine, int endColumn)
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
	public void writeJson(bhI_JsonObject json)
	{
		bhJsonHelper.getInstance().putJavaVarArgs(json, bhE_JsonKey.fileRange, (Object[])m_range);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_range = new Integer[4];
		bhI_JsonArray range = bhJsonHelper.getInstance().getJsonArray(json, bhE_JsonKey.fileRange);
		
		for( int i = 0; i < range.getSize(); i++ )
		{
			m_range[i] = range.getInt(i);
		}
	}
}
