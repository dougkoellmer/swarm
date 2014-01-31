package swarm.shared.entities;

import swarm.server.structs.ServerBitArray;
import swarm.shared.app.BaseAppContext;
import swarm.shared.app.S_CommonApp;
import swarm.shared.debugging.U_Debug;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.structs.BitArray;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;

/**
 * ...
 * @author 
 */
public abstract class A_Grid extends A_JsonEncodable
{
	protected int m_width;
	protected int m_height;
	protected int m_cellWidth;
	protected int m_cellHeight;
	protected int m_cellPadding;
	
	protected BitArray m_ownership = null;
	
	public A_Grid()
	{
	}
	
	public void calcCoordTopLeftPoint(GridCoordinate coordinate, int subCellDimension, Point point_out)
	{
		m_cellWidth = m_cellWidth * subCellDimension;
		m_cellHeight = m_cellHeight * subCellDimension;
		m_cellPadding = m_cellPadding * subCellDimension;
		double x = coordinate.getM() * m_cellWidth + (coordinate.getM() * m_cellPadding);
		double y = coordinate.getN() * m_cellHeight + (coordinate.getN() * m_cellPadding);
		
		point_out.set(x, y, 0.0);
	}
	
	public void calcCoordCenterPoint(GridCoordinate coordinate, int subCellDimension, Point point_out)
	{
		calcCoordTopLeftPoint(coordinate, subCellDimension, point_out);
		
		//--- TODO(DRK): Probably needs to take into account padding for higher sub-cell dimensions.
		double cellWidth = m_cellWidth * subCellDimension;
		double cellHeight = m_cellHeight * subCellDimension;
		
		point_out.inc(cellWidth/2.0, cellHeight/2.0, 0.0);
	}
	
	public void claimCoordinate(GridCoordinate coordinate)
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

	public boolean isTaken(GridCoordinate coordinate)
	{
		if( m_ownership == null )  return false;
		
		int bitIndex = coordinate.calcArrayIndex(m_width);
		
		return m_ownership.isSet(bitIndex);
	}
	
	public A_Grid(int width, int height)
	{
		m_width = width;
		m_height = height;
	}
	
	protected BitArray createBitArray()
	{
		return new BitArray();
	}
	
	protected BitArray createBitArray(int bitCount)
	{
		return new BitArray(bitCount);
	}
	
	public boolean isInBounds(GridCoordinate coordinate)
	{
		return coordinate.getM() >= 0 && coordinate.getN() >= 0 && coordinate.getM() < m_width && coordinate.getN() < m_height;
	}
	
	public A_Grid(int width, int height, int cellWidth, int cellHeight, int cellPadding) 
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
	
	private void expandToCoordinate(GridCoordinate coordinate)
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
			U_Debug.ASSERT(false, "expected larger dimensions");
			
			return;
		}
		else if( m_width == width && m_height == height )
		{
			return;
		}
		
		int oldWidth = this.getWidth();

		m_width = width;
		m_height = height;
		
		BitArray oldArray = m_ownership;
		
		m_ownership = this.createBitArray(m_width*m_height);
		
		if( oldArray != null )
		{
			m_ownership.or(oldArray, oldWidth, m_width);
		}
		
		//smT_Grid.validateExpansion(m_bitArray, oldSize, m_size);
	}
	
	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		Integer width = factory.getHelper().getInt(json, E_JsonKey.gridWidth);
		Integer height = factory.getHelper().getInt(json, E_JsonKey.gridHeight);
		Integer cellWidth = factory.getHelper().getInt(json, E_JsonKey.gridCellWidth);
		Integer cellHeight = factory.getHelper().getInt(json, E_JsonKey.gridCellHeight);
		Integer cellPadding = factory.getHelper().getInt(json, E_JsonKey.gridCellPadding);	
		
		int newWidth = width != null ? width : m_width;
		int newHeight = height != null ? height : m_height;
		
		m_cellWidth = cellWidth != null ? cellWidth : m_cellWidth;
		m_cellHeight = cellHeight != null ? cellHeight : m_cellHeight;
		m_cellPadding = cellPadding != null ? cellPadding : m_cellPadding;
		
		if( factory.getHelper().containsAnyKeys(json, E_JsonKey.bitArray) )
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
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		factory.getHelper().putInt(json_out, E_JsonKey.gridWidth, m_width);
		factory.getHelper().putInt(json_out, E_JsonKey.gridHeight, m_height);
		factory.getHelper().putInt(json_out, E_JsonKey.gridCellWidth, m_cellWidth);
		factory.getHelper().putInt(json_out, E_JsonKey.gridCellHeight, m_cellHeight);
		factory.getHelper().putInt(json_out, E_JsonKey.gridCellPadding, m_cellPadding);
		
		if( m_ownership != null )
		{
			m_ownership.writeJson(factory, json_out);
		}
	}
}