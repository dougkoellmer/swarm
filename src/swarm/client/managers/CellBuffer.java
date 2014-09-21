package swarm.client.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;





import swarm.client.entities.BufferCell;
import swarm.client.entities.ClientGrid;
import swarm.client.structs.BufferCellPool;
import swarm.client.structs.I_LocalCodeRepository;
import swarm.shared.utils.U_Bits;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Grid;
import swarm.shared.entities.E_CodeType;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.GridCoordinate;


/**
 * ...
 * @author 
 */
public class CellBuffer
{
	private static final Logger s_logger = Logger.getLogger(CellBuffer.class.getName());
	
	private static final CellAddressMapping s_utilMapping = new CellAddressMapping();
	
	private final GridCoordinate m_coord = new GridCoordinate();
//	private final ArrayList<BufferCell> m_cells = new ArrayList<BufferCell>();
	private int m_width = 0;
	private int m_height = 0;
	
	private final ArrayList<BufferCell> m_cellList = new ArrayList<BufferCell>();
	
	private final int m_subCellCount;
	
	private final CellBufferManager m_parent;
	private final CellCodeManager m_codeMngr;
	private final CellSizeManager m_cellSizeMngr;
	private final BufferCellPool m_cellPool;
	
	CellBuffer(CellBufferManager parent, CellCodeManager codeMngr, BufferCellPool cellPool, CellSizeManager cellSizeMngr, int subCellCount)
	{
		m_codeMngr = codeMngr;
		m_cellPool = cellPool;
		m_cellSizeMngr = cellSizeMngr;
		m_parent = parent;
		m_subCellCount = subCellCount;
	}
	
	public GridCoordinate getCoordinate()
	{
		return m_coord;
	}
	
	public int getSubCellCount()
	{
		return m_subCellCount;
	}
	
	public int getWidth()
	{
		return m_width;
	}
	
	public int getHeight()
	{
		return m_height;
	}
	
	public int getCellCount()
	{
		return m_cellList.size();
	}
	
	void setExtents(int m, int n, int width, int height)
	{
		m_coord.set(m, n);
		
		m_width = width;
		m_height = height;
	}
	
	public BufferCell getCellAtIndex(int index)
	{
		return m_cellList.get(index);
	}
	
	public boolean isInBoundsAbsolute(GridCoordinate absolute)
	{
		return isInBoundsAbsolute(absolute.getM(), absolute.getN());
	}
	
	public boolean isInBoundsAbsolute(int m, int n)
	{
		return m >= m_coord.getM() && m < m_coord.getM()+m_width && n >= m_coord.getN() && n < m_coord.getN()+m_height;
	}
	
	public BufferCell getCellAtAbsoluteCoord(GridCoordinate absoluteCoord)
	{
		return getCellAtAbsoluteCoord(absoluteCoord.getM(), absoluteCoord.getN());
	}
	
	public BufferCell getCellAtAbsoluteCoord(int m, int n)
	{
		for( int i = 0; i < m_cellList.size(); i++ )
		{
			BufferCell ithCell = m_cellList.get(i);
			
			if( ithCell.getCoordinate().isEqualTo(m, n) )
			{
				return ithCell;
			}
		}
		
		return null;
	}
	
	private BufferCell removeCellAtAbsCoord(int m, int n)
	{
		for( int i = 0; i < m_cellList.size(); i++ )
		{
			BufferCell ithCell = m_cellList.get(i);
			
			if( ithCell == null )  continue;
			
			if( ithCell.getCoordinate().isEqualTo(m, n) )
			{
				m_cellList.set(i, null);
				
				return ithCell;
			}
		}
		
		return null;
	}
	
	private static boolean swap(int m, int n, CellBuffer from, CellBuffer to)
	{
		BufferCell cell = from.removeCellAtAbsCoord(m, n);
		
		if( cell == null )  return false;
		
		to.m_cellList.add(cell);
		
		return true;
	}
	
	void imposeBuffer(ClientGrid grid, CellBuffer otherBuffer, I_LocalCodeRepository localCodeSource, int currentSubCellCount, int options__extends__smF_BufferUpdateOption)
	{
		boolean createVisualizations = (options__extends__smF_BufferUpdateOption & F_BufferUpdateOption.CREATE_VISUALIZATIONS) != 0;
		boolean communicateWithServer = (options__extends__smF_BufferUpdateOption & F_BufferUpdateOption.COMMUNICATE_WITH_SERVER) != 0;
		boolean flushPopulator = (options__extends__smF_BufferUpdateOption & F_BufferUpdateOption.FLUSH_CELL_POPULATOR) != 0;

		int i;
		int m, n;
		
		int otherBufferCellCount = otherBuffer.getCellCount();
		
		
		//--- DRK > We're currently "below" the zoom level of this buffer so early-out and clear other buffer of its cells.
		if( currentSubCellCount < m_subCellCount )
		{
			for( i = 0; i < otherBuffer.m_cellList.size(); i++ )
			{
				BufferCell ithCellFromOtherBuffer = otherBuffer.m_cellList.get(i);
				
				m_cellPool.deallocCell(ithCellFromOtherBuffer);
			}
			
			
			otherBuffer.m_cellList.clear();
			
			return;
		}
		
		for( i = 0; i < otherBufferCellCount; i++ )
		{
			BufferCell ithCell = otherBuffer.m_cellList.get(i);
			if( !this.isInBoundsAbsolute(ithCell.getCoordinate()) )
			{
				otherBuffer.m_cellList.set(i, null);
				
				m_cellPool.deallocCell(ithCell);
			}
		}

		int limitN = m_coord.getN() + m_height;
		int limitM = m_coord.getM() + m_width;
		for ( n = m_coord.getN(); n < limitN; n++ )
		{
			for ( m = m_coord.getM(); m < limitM; m++ )
			{
				if( currentSubCellCount > m_subCellCount )
				{
					int offset = grid.getObscureOffset(m, n, m_subCellCount, currentSubCellCount);
					
					if( offset > 1 )
					{
						m += offset;
						m -= 1; // cancel out next increment in for loop
						
						continue;
					}
				}
				
				if( !grid.isTaken(m, n, m_subCellCount) )  continue;
				
				if( swap(m, n, otherBuffer, this) )  continue;
				
				BufferCell newCell = m_cellPool.allocCell(grid, m_subCellCount, createVisualizations);
				this.m_cellList.add(newCell);
				
				newCell.getCoordinate().set(m, n);
				
//				s_logger.severe(m_subCellCount+"");
				m_codeMngr.populateCell(newCell, localCodeSource, m_subCellCount, communicateWithServer, E_CodeType.SPLASH);
				
				s_utilMapping.getCoordinate().copy(newCell.getCoordinate());
				
				if( m_subCellCount == 1 )
				{
					m_cellSizeMngr.populateCellSize(s_utilMapping, this.m_parent, newCell);
				}
			}
		}
		
		if( flushPopulator )
		{
			m_codeMngr.flush();
		}
		
		for( i = 0; i < otherBufferCellCount; i++ )
		{
			BufferCell ithCell = otherBuffer.m_cellList.get(i);

			if( ithCell != null )
			{
				otherBuffer.m_cellList.set(i, null);
				m_cellPool.deallocCell(ithCell);
			}
		}

		otherBuffer.m_cellList.clear();
	}
	
	void drain()
	{		
		for ( int i = 0; i < this.m_cellList.size(); i++ )
		{
			BufferCell ithCell = this.m_cellList.get(i);
			
			if( ithCell != null )
			{
				m_cellPool.deallocCell(ithCell);
			}
		}
		
		this.m_cellList.clear();
		
		this.setExtents(0, 0, 0, 0);
	}
}