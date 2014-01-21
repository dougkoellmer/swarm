package swarm.shared.entities;

import swarm.server.structs.smServerBitArray;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.app.smS_App;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.structs.smBitArray;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;

/**
 * ...
 * @author 
 */
public abstract class smA_Grid extends smA_JsonEncodable
{
	protected int m_width;
	protected int m_height;
	protected int m_cellWidth;
	protected int m_cellHeight;
	protected int m_cellPadding;
	
	protected smBitArray m_ownership = null;
	
	public smA_Grid()
	{
	}
	
	public void calcCoordTopLeftPoint(smGridCoordinate coordinate, int subCellDimension, smPoint point_out)
	{
		m_cellWidth = m_cellWidth * subCellDimension;
		m_cellHeight = m_cellHeight * subCellDimension;
		m_cellPadding = m_cellPadding * subCellDimension;
		double x = coordinate.getM() * m_cellWidth + (coordinate.getM() * m_cellPadding);
		double y = coordinate.getN() * m_cellHeight + (coordinate.getN() * m_cellPadding);
		
		point_out.set(x, y, 0.0);
	}
	
	public void calcCoordCenterPoint(smGridCoordinate coordinate, int subCellDimension, smPoint point_out)
	{
		calcCoordTopLeftPoint(coordinate, subCellDimension, point_out);
		
		//--- TODO(DRK): Probably needs to take into account padding for higher sub-cell dimensions.
		double cellWidth = m_cellWidth * subCellDimension;
		double cellHeight = m_cellHeight * subCellDimension;
		
		point_out.inc(cellWidth/2.0, cellHeight/2.0, 0.0);
	}
	
	public void claimCoordinate(smGridCoordinate coordinate)
	{
		int bitIndex = coordinate.calcArrayIndex(m_width);
		
		if( m_ownership == null )
		{
			m_ownership = createBitArray(bitIndex+1);
		}
		else
		{
			if( !this.isInBounds(coordinate) )
			{
				expandToCoordinate(coordinate);
			}
		}
		
		m_ownership.set(bitIndex, true);
	}
	
	public boolean isTaken(int bitIndex)
	{
		if( m_ownership == null )  return false;
		
		return m_ownership.isSet(bitIndex);
	}

	public boolean isTaken(smGridCoordinate coordinate)
	{
		if( m_ownership == null )  return false;
		
		int bitIndex = coordinate.calcArrayIndex(m_width);
		
		return m_ownership.isSet(bitIndex);
	}
	
	public smA_Grid(int width, int height)
	{
		m_width = width;
		m_height = height;
	}
	
	protected smBitArray createBitArray()
	{
		return new smBitArray();
	}
	
	protected smBitArray createBitArray(int bitCount)
	{
		return new smBitArray(bitCount);
	}
	
	public boolean isInBounds(smGridCoordinate coordinate)
	{
		return coordinate.getM() >= 0 && coordinate.getN() >= 0 && coordinate.getM() < m_width && coordinate.getN() < m_height;
	}
	
	public smA_Grid(int width, int height, int cellWidth, int cellHeight, int cellPadding) 
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
	
	private void expandToCoordinate(smGridCoordinate coordinate)
	{
		int newWidth = coordinate.getM() + 1;
		int newHeight = coordinate.getM() + 1;
		
		newWidth = newWidth < getWidth() ? getWidth() : newWidth;
		newHeight = newHeight < getWidth() ? getHeight() : newHeight;
		
		expandToSize(newWidth, newHeight);
	}
	
	protected void expandToSize(int width, int height)
	{
		if( m_width > width || m_height > height )
		{
			smU_Debug.ASSERT(false, "expected larger dimensions");
			
			return;
		}
		else if( m_width == width && m_height == height )
		{
			return;
		}
		
		int oldWidth = this.getWidth();

		m_width = width;
		m_height = height;
		
		smBitArray oldArray = m_ownership;
		
		m_ownership = this.createBitArray(m_width*m_height);
		
		if( oldArray != null )
		{
			m_ownership.or(oldArray, oldWidth, m_width);
		}
		
		//smT_Grid.validateExpansion(m_bitArray, oldSize, m_size);
	}
	
	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		Integer width = factory.getHelper().getInt(json, smE_JsonKey.gridWidth);
		Integer height = factory.getHelper().getInt(json, smE_JsonKey.gridHeight);
		Integer cellWidth = factory.getHelper().getInt(json, smE_JsonKey.gridCellWidth);
		Integer cellHeight = factory.getHelper().getInt(json, smE_JsonKey.gridCellHeight);
		Integer cellPadding = factory.getHelper().getInt(json, smE_JsonKey.gridCellPadding);	
		
		int newWidth = width != null ? width : m_width;
		int newHeight = height != null ? height : m_height;
		
		m_cellWidth = cellWidth != null ? cellWidth : m_cellWidth;
		m_cellHeight = cellHeight != null ? cellHeight : m_cellHeight;
		m_cellPadding = cellPadding != null ? cellPadding : m_cellPadding;
		
		if( factory.getHelper().containsAnyKeys(json, smE_JsonKey.bitArray) )
		{
			m_ownership = m_ownership != null ? m_ownership : createBitArray();
			
			m_ownership.readJson(factory, json);
			
			m_width = newWidth;
			m_height = newHeight;
		}
		else
		{
			if( newWidth > m_width || newHeight > m_height )
			{
				this.expandToSize(newWidth, newHeight);
			}
		}
	}
	
	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		factory.getHelper().putInt(json_out, smE_JsonKey.gridWidth, m_width);
		factory.getHelper().putInt(json_out, smE_JsonKey.gridHeight, m_height);
		factory.getHelper().putInt(json_out, smE_JsonKey.gridCellWidth, m_cellWidth);
		factory.getHelper().putInt(json_out, smE_JsonKey.gridCellHeight, m_cellHeight);
		factory.getHelper().putInt(json_out, smE_JsonKey.gridCellPadding, m_cellPadding);
		
		if( m_ownership != null )
		{
			m_ownership.writeJson(factory, json_out);
		}
	}
}