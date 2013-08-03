package b33hive.shared.entities;

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
	protected int m_size = 0;
	
	public bhA_Grid() 
	{
		
	}
	
	public boolean isInBounds(bhGridCoordinate coordinate)
	{
		return coordinate.getM() >= 0 && coordinate.getN() >= 0 && coordinate.getM() < m_size && coordinate.getN() < m_size;
	}
	
	public bhA_Grid(int size) 
	{
		m_size = size;
	}
	
	public int getSize()
	{
		return m_size;
	}
	
	public int calcTotalCellCount()
	{
		return m_size * m_size;
	}
	
	public double calcPixelWidth()
	{
		double gridWidth = m_size * bhS_App.CELL_PIXEL_COUNT + ((m_size-1) * bhS_App.CELL_SPACING_PIXEL_COUNT);
		return gridWidth;
	}
	
	public double calcPixelHeight()
	{
		double gridHeight = m_size * bhS_App.CELL_PIXEL_COUNT + ((m_size-1) * bhS_App.CELL_SPACING_PIXEL_COUNT);
		return gridHeight;
	}
	
	@Override
	public void readJson(bhI_JsonObject json)
	{
		Integer size = bhJsonHelper.getInstance().getInt(json, bhE_JsonKey.gridSize);
		
		if( size != null )
		{	
			m_size = size;
		}
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		bhJsonHelper.getInstance().putInt(json, bhE_JsonKey.gridSize, m_size);
	}
}