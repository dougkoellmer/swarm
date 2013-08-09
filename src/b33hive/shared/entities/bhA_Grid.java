package b33hive.shared.entities;

import b33hive.shared.app.bh;
import b33hive.shared.app.bhS_App;
import b33hive.shared.json.bhA_JsonEncodable;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.structs.bhGridCoordinate;

/**
 * ...
 * @author 
 */
public abstract class bhA_Grid extends bhA_JsonEncodable
{
	protected int m_width;
	protected int m_height;
	protected int m_cellWidth;
	protected int m_cellHeight;
	protected int m_cellPadding;
	
	public bhA_Grid()
	{
		
	}
	
	public bhA_Grid(int width, int height)
	{
		m_width = width;
		m_height = height;
	}
	
	public boolean isInBounds(bhGridCoordinate coordinate)
	{
		return coordinate.getM() >= 0 && coordinate.getN() >= 0 && coordinate.getM() < m_width && coordinate.getN() < m_height;
	}
	
	public bhA_Grid(int width, int height, int cellWidth, int cellHeight, int cellPadding) 
	{
		m_width = width;
		m_height = height;
		m_cellWidth = cellWidth;
		m_cellHeight = cellHeight;
		m_cellPadding = cellPadding;
	}
	
	public boolean isEmpty()
	{
		return m_width == 0 || m_height == 0;
	}
	
	public int getWidth()
	{
		return m_width;
	}
	
	public int getHeight()
	{
		return m_height;
	}
	
	public int getCellWidth()
	{
		return m_cellWidth;
	}
	
	public int getCellHeight()
	{
		return m_cellHeight;
	}
	
	public int getCellPadding()
	{
		return m_cellPadding;
	}
	
	public int calcTotalCellCount()
	{
		return m_width * m_height;
	}
	
	public double calcPixelWidth()
	{
		double gridWidth = m_width * m_cellWidth + ((m_width-1) * m_cellPadding);
		
		return gridWidth;
	}
	
	public double calcPixelHeight()
	{
		double gridHeight = m_height * m_cellHeight + ((m_height-1) * m_cellPadding);
		
		return gridHeight;
	}
	
	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_width = bh.jsonFactory.getHelper().getInt(json, bhE_JsonKey.gridWidth);
		m_height = bh.jsonFactory.getHelper().getInt(json, bhE_JsonKey.gridHeight);
		m_cellWidth = bh.jsonFactory.getHelper().getInt(json, bhE_JsonKey.gridCellWidth);
		m_cellHeight = bh.jsonFactory.getHelper().getInt(json, bhE_JsonKey.gridCellHeight);
		m_cellPadding = bh.jsonFactory.getHelper().getInt(json, bhE_JsonKey.gridCellPadding);		
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		bh.jsonFactory.getHelper().putInt(json, bhE_JsonKey.gridWidth, m_width);
		bh.jsonFactory.getHelper().putInt(json, bhE_JsonKey.gridHeight, m_height);
		bh.jsonFactory.getHelper().putInt(json, bhE_JsonKey.gridCellWidth, m_cellWidth);
		bh.jsonFactory.getHelper().putInt(json, bhE_JsonKey.gridCellHeight, m_cellHeight);
		bh.jsonFactory.getHelper().putInt(json, bhE_JsonKey.gridCellPadding, m_cellPadding);
	}
}