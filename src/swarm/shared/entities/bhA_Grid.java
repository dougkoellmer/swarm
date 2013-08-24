package swarm.shared.entities;

import swarm.server.structs.bhServerBitArray;
import swarm.shared.app.sm;
import swarm.shared.app.bhS_App;
import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.structs.bhBitArray;
import swarm.shared.structs.bhGridCoordinate;

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
	
	protected bhBitArray m_ownership = null;
	
	public bhA_Grid()
	{
	}

	public boolean isTaken(bhGridCoordinate coordinate)
	{
		if( m_ownership == null )  return false;
		
		int bitIndex = coordinate.calcArrayIndex(m_width);
		
		return m_ownership.isSet(bitIndex);
	}
	
	public bhA_Grid(int width, int height)
	{
		m_width = width;
		m_height = height;
	}
	
	protected bhBitArray createBitArray()
	{
		return new bhBitArray();
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
		Integer width = sm.jsonFactory.getHelper().getInt(json, bhE_JsonKey.gridWidth);
		Integer height = sm.jsonFactory.getHelper().getInt(json, bhE_JsonKey.gridHeight);
		Integer cellWidth = sm.jsonFactory.getHelper().getInt(json, bhE_JsonKey.gridCellWidth);
		Integer cellHeight = sm.jsonFactory.getHelper().getInt(json, bhE_JsonKey.gridCellHeight);
		Integer cellPadding = sm.jsonFactory.getHelper().getInt(json, bhE_JsonKey.gridCellPadding);	
		
		m_width = width != null ? width : m_width;
		m_height = height != null ? height : m_height;
		m_cellWidth = cellWidth != null ? cellWidth : m_cellWidth;
		m_cellHeight = cellHeight != null ? cellHeight : m_cellHeight;
		m_cellPadding = cellPadding != null ? cellPadding : m_cellPadding;
		
		if( sm.jsonFactory.getHelper().containsAnyKeys(json, bhE_JsonKey.bitArray) )
		{
			m_ownership = m_ownership != null ? m_ownership : createBitArray();
			
			m_ownership.readJson(json);
		}
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		sm.jsonFactory.getHelper().putInt(json, bhE_JsonKey.gridWidth, m_width);
		sm.jsonFactory.getHelper().putInt(json, bhE_JsonKey.gridHeight, m_height);
		sm.jsonFactory.getHelper().putInt(json, bhE_JsonKey.gridCellWidth, m_cellWidth);
		sm.jsonFactory.getHelper().putInt(json, bhE_JsonKey.gridCellHeight, m_cellHeight);
		sm.jsonFactory.getHelper().putInt(json, bhE_JsonKey.gridCellPadding, m_cellPadding);
		
		if( m_ownership != null )
		{
			m_ownership.writeJson(json);
		}
	}
}