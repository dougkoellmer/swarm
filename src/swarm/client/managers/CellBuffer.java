package swarm.client.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;



import swarm.client.entities.BufferCell;
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
	
	private static final GridCoordinate s_utilCoord1 = new GridCoordinate();
	private static final GridCoordinate s_utilCoord2 = new GridCoordinate();
	private static final GridCoordinate s_utilCoord3 = new GridCoordinate();
	private static final CellAddressMapping s_utilMapping = new CellAddressMapping();
	
	private final GridCoordinate m_coordinate = new GridCoordinate();
	private final ArrayList<BufferCell> m_cells = new ArrayList<BufferCell>();
	private int m_width = 0;
	private int m_height = 0;
	
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
		return m_coordinate;
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
		return m_cells.size();
	}
	
	void setExtents(int m, int n, int width, int height)
	{
		int size = width * height;
		
		m_coordinate.set(m, n);
		
		m_width = width;
		m_height = height;
		
		while ( m_cells.size() < size )
		{
			m_cells.add(null);
		}
		
		while ( m_cells.size() > size )
		{
			BufferCell cellRemoved = m_cells.remove(m_cells.size()-1);
			if( cellRemoved != null )
			{
				U_Debug.ASSERT(false, "setExtents1");
			}
		}
	}
	
	public BufferCell getCellAtIndex(int index)
	{
		return m_cells.get(index);
	}
	
	private void setCell(int m, int n, BufferCell cell)
	{
		if ( !isInBoundsRelative(m, n) )
		{
			U_Debug.ASSERT(false, "smCellBuffer::setCell1");
			return;
		}
		
		m_cells.set(m + n * m_width, cell);
	}
	
	public boolean isInBoundsAbsolute(GridCoordinate absolute)
	{
		this.absoluteToRelative(absolute, s_utilCoord1);

		return isInBoundsRelative(s_utilCoord1.getM(), s_utilCoord1.getN());
	}
	
	public boolean isInBoundsRelative(int m, int n)
	{
		return m >= 0 && m < m_width && n >= 0 && n < m_height;
	}
	
	private int coordsToIndex(int m, int n)
	{
		return m + n * m_width;
	}
	
	public BufferCell getCellAtAbsoluteCoord(GridCoordinate absoluteCoord)
	{
		absoluteToRelative(absoluteCoord, s_utilCoord1);
		return getCellAtRelativeCoord(s_utilCoord1);
	}
	
	public BufferCell getCellAtRelativeCoord(GridCoordinate relativeCoord)
	{
		if ( !isInBoundsRelative(relativeCoord.getM(), relativeCoord.getN()) )
		{
			return null;
		}
		
		return m_cells.get(relativeCoord.getM() + relativeCoord.getN() * m_width);
	}
	
	public void absoluteToRelative(GridCoordinate absCoord, GridCoordinate coord_out)
	{
		int relativeM = absCoord.getM() - this.m_coordinate.getM();
		int relativeN = absCoord.getN() - this.m_coordinate.getN();
		coord_out.set(relativeM, relativeN);
	}
	
	public void relativeToAbsolute(GridCoordinate relCoord, GridCoordinate coord_out)
	{
		int absoluteM = relCoord.getM() + this.m_coordinate.getM();
		int absoluteN = relCoord.getN() + this.m_coordinate.getN();
		coord_out.set(absoluteM, absoluteN);
	}
	
	public boolean isTouching(GridCoordinate absCoord)
	{
		int relativeM = absCoord.getM() - this.m_coordinate.getM();
		int relativeN = absCoord.getN() - this.m_coordinate.getN();
		return isInBoundsRelative(relativeM, relativeN);
	}
	
	void imposeBuffer(A_Grid grid, CellBuffer otherBuffer, I_LocalCodeRepository localCodeSource, int currentSubCellCount, int options__extends__smF_BufferUpdateOption)
	{		
		boolean createVisualizations = (options__extends__smF_BufferUpdateOption & F_BufferUpdateOption.CREATE_VISUALIZATIONS) != 0;
		boolean communicateWithServer = (options__extends__smF_BufferUpdateOption & F_BufferUpdateOption.COMMUNICATE_WITH_SERVER) != 0;
		boolean flushPopulator = (options__extends__smF_BufferUpdateOption & F_BufferUpdateOption.FLUSH_CELL_POPULATOR) != 0;
		
		BufferCell ithCell = null;
		int i;
		boolean cellRecycled;
		int m, n;
		
		int otherSubCellCountDim = otherBuffer.getSubCellCount();
		int thisSubCellCountDim = this.getSubCellCount();
		int thisCellCount = this.getCellCount();
		int otherBufferCellCount = otherBuffer.getCellCount();
		
		GridCoordinate absCoord = s_utilCoord1;
		GridCoordinate relThisCoord = s_utilCoord2;
		
		
		/*HashMap<String, smBufferCell> debug_dict = new HashMap<String, smBufferCell>();
		int debug_otherBufferHitCount = 0;
		for ( i = 0; i < otherBuffer.getCellCount(); i++ )
		{
			smBufferCell debug_cell = otherBuffer.getCellAtIndex(i);
			if ( this.isTouching(debug_cell.getCoordinate()) )
			{
				debug_dict.put(debug_cell.getCoordinate().writeString(), debug_cell);
				debug_otherBufferHitCount++;
			}
		}*/
		
		GridCoordinate relOtherCoord = s_utilCoord3;
		
		int startM = 0, finishM = m_width, deltaM = 1;
		int startN = 0, finishN = m_height, deltaN = 1;
		
		if ( this.m_coordinate.getM() < otherBuffer.m_coordinate.getM() )
		{
			startM = m_width - 1;
			finishM = -1;
			deltaM = -1;
		}
		
		if ( this.m_coordinate.getN() < otherBuffer.m_coordinate.getN() )
		{
			startN = m_height - 1;
			finishN = -1;
			deltaN = -1;
		}
		
		for ( n = startN; n != finishN; n+=deltaN )
		{
			for ( m = startM; m != finishM; m+=deltaM )
			{
				relThisCoord.set(m, n);
				
				this.relativeToAbsolute(relThisCoord, absCoord);
				otherBuffer.absoluteToRelative(absCoord, relOtherCoord);
				
				if( !grid.isTaken(absCoord) )
				{
					continue;
				}
				
				cellRecycled = false;
				
				BufferCell otherCell = null;
				
				if ( otherBuffer.isTouching(absCoord) )
				{
					otherCell = otherBuffer.getCellAtRelativeCoord(relOtherCoord);
					
					//--- DRK > Can be null when a cell is created between buffer imposings (e.g. a new account is created
					//---		in b33hive). In this case, the grid coordinate is marked as taken, but there's no cell there
					//---		yet, so, we make one.
					//---		There used to be an assert inside here, to catch algorithmic problems...so those problems could
					//---		still theoretically still exist, and we're just patching over them by creating a new cell...oh well.
					if ( otherCell == null )
					{
						otherCell = m_cellPool.allocCell(grid, m_subCellCount, createVisualizations);
					}
					
					//--- DRK > Splice the still-visible cell from the other buffer into this buffer.
					otherBuffer.setCell(relOtherCoord.getM(), relOtherCoord.getN(), null);
					this.setCell(m, n, otherCell);
					
					
					//--- DRK > If possible, fill in the gap that is left in the other buffer with the cell
					//---		from the other buffer at the same m, n location that was skipped.
					BufferCell cellToTransfer = otherBuffer.getCellAtRelativeCoord(relThisCoord);
					if ( cellToTransfer != null )
					{
						if ( this.isTouching(cellToTransfer.getCoordinate()) )
						{
							// nothing to do...i think
						}
						else
						{
							otherBuffer.setCell(m, n, null);
							otherBuffer.setCell(relOtherCoord.getM(), relOtherCoord.getN(), cellToTransfer);
						}
					}
					else
					{
						// nothing to do...i think
					}
				}
				else
				{
					otherCell = otherBuffer.getCellAtRelativeCoord(relThisCoord);
					
					if ( otherCell != null )
					{							
						/*if ( this.isTouching(otherCell.getCoordinate()) ) // collides with reusable cell
						{
							this.absoluteToRelative(otherCell.getCoordinate(), relThisCoord);
							
							var indexOfCollision = coordsToIndex(relThisCoord.getM(), relThisCoord.getN());
							
							if ( currentIndex > indexOfCollision )
							{
								//--- DRK > This cell was transferred into the gap left by a splice, so we can safely
								//---		recycle it despite there being a collision.
								otherBuffer.setCell(m, n, null);
							}
							else
							{
								//--- DRK > This cell represents a pending splice that has yet to take place, so we must make a freshy.
								otherCell = pool.allocCell();
							}
						}
						else
						{
							otherBuffer.setCell(m, n, null);
							otherCell.m_visualization.cellRecycled();
						}*/
						
						otherBuffer.setCell(m, n, null);
						cellRecycled = true;
					}
					else
					{
						otherCell = m_cellPool.allocCell(grid, m_subCellCount, createVisualizations);
					}
					
					this.setCell(m, n, otherCell);
				}
				
				BufferCell imposedCell = getCellAtRelativeCoord(relThisCoord);
				imposedCell.getCoordinate().copy(absCoord);
				
				m_codeMngr.populateCell(imposedCell, localCodeSource, m_subCellCount, cellRecycled, communicateWithServer, E_CodeType.SPLASH);
				
				s_utilMapping.getCoordinate().copy(imposedCell.getCoordinate());
				
				m_cellSizeMngr.populateCellSize(s_utilMapping, this.m_parent, imposedCell);
			}
		}
		
		/*int debug_nonNullCount = 0;
		int debug_thisBufferHitCount = 0;
		for ( i = 0; i < this.getCellCount(); i++ )
		{
			smBufferCell debug_cell = this.m_cells.get(i);
			
			String debug_stringRep = debug_cell.getCoordinate().writeString();
			
			if ( debug_dict.containsKey(debug_stringRep) )
			{
				smU_Debug.ASSERT(debug_cell == debug_dict.get(debug_stringRep), "imposeBuffer2");
				debug_thisBufferHitCount++;
			}
		}
		
		smU_Debug.ASSERT(debug_otherBufferHitCount == debug_thisBufferHitCount, "imposeBuffer3");*/
		
		if( flushPopulator )
		{
			m_codeMngr.flush();
		}
		
		//int debug_nonNullCount = 0;
		for ( i = 0; i < otherBufferCellCount; i++ )
		{
			ithCell = otherBuffer.m_cells.get(i);
			
			//--- DRK > The two known cases where you have orphaned cells is (a) when otherBuffer is larger,
			//---		or (b) when otherBuffer has "weird" dimensions relative to thisBuffer, e.g. otherBuffer=4x2
			//---		and thisBuffer=3x3 (there will be two orphaned cells, 3x0 and 3x1 of otherBuffer).
			//---
			//---		TODO: Case (b) is probably mitigatable in a fancier way, but I don't have time to figure it out right now.
			//---		Anyway, all it means for now is n extra cell allocations in the pool.  Extreme screen resizes would
			//---		kinda thrash the pool, but that in turn could be mitigated by occasionally garbage collecting
			//---		the pool, if no good solution to case (b) could be found.
			if ( ithCell != null )
			{
				m_cellPool.deallocCell(ithCell);
				otherBuffer.m_cells.set(i, null);
				
				//debug_nonNullCount++;
			}
		}
		
		//--- DRK > The assert below was real old school, but finally tripped, and further review deemed that it wasn't valid,
		//---		so the comment below in the if block was somewhat prophetic.  A valid case where the assert can trip is if otherBuffer is 5x2
		//---		and thisBuffer is 3x3....otherBuffer will have 4 orphaned cells (the 2 rightmost columns), so debug_nonNullCount
		//---		will equal 4....4 != 10 - 9, so the assert can trip even though it shouldn't.
		/*if( otherBufferCellCount > thisCellCount )
		{
			//--- DRK > This might not be a valid smU_Debug.ASSERT case...if it trips, let me know.
			String message = "nonNullCount, otherBufferCount, thisCellCount, " + debug_nonNullCount + " " + otherBufferCellCount + " " + thisCellCount;
			smU_Debug.ASSERT(debug_nonNullCount == otherBufferCellCount - thisCellCount, message);
		}*/
	}
	
	void drain()
	{		
		for ( int i = 0; i < this.m_cells.size(); i++ )
		{
			BufferCell ithCell = this.m_cells.get(i);
			
			if( ithCell != null )
			{
				m_cellPool.deallocCell(ithCell);
			}
		}
		
		this.m_cells.clear();
		
		this.setExtents(0, 0, 0, 0);
	}
}