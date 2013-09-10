package swarm.client.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;


import swarm.client.entities.smBufferCell;
import swarm.client.structs.smCellPool;
import swarm.client.structs.smI_LocalCodeRepository;
import swarm.shared.utils.smU_Bits;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smA_Grid;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.smGridCoordinate;


/**
 * ...
 * @author 
 */
public class smCellBuffer
{
	private static final Logger s_logger = Logger.getLogger(smCellBuffer.class.getName());
	
	private static final smGridCoordinate s_utilCoord1 = new smGridCoordinate();
	private static final smGridCoordinate s_utilCoord2 = new smGridCoordinate();
	private static final smGridCoordinate s_utilCoord3 = new smGridCoordinate();
	
	private final smGridCoordinate m_coordinate = new smGridCoordinate();
	private final ArrayList<smBufferCell> m_cells = new ArrayList<smBufferCell>();
	private int m_width = 0;
	private int m_height = 0;
	
	private int m_subCellDimension = 1;
	
	private final smCellCodeManager m_codeMngr;
	private final smCellPool m_cellPool;
	
	smCellBuffer(smCellCodeManager codeMngr, smCellPool cellPool)
	{
		m_codeMngr = codeMngr;
		m_cellPool = cellPool;
	}
	
	public smGridCoordinate getCoordinate()
	{
		return m_coordinate;
	}
	
	void setCellSize(int size)
	{
		if ( !smU_Bits.isPowerOfTwo(size) )
		{
			smU_Debug.ASSERT(false, "setCellSize1");
			return;
		}
		
		m_subCellDimension = size;
	}
	
	public int getSubCellCount()
	{
		return m_subCellDimension;
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
			smBufferCell cellRemoved = m_cells.remove(m_cells.size()-1);
			if( cellRemoved != null )
			{
				smU_Debug.ASSERT(false, "setExtents1");
			}
		}
	}
	
	public smBufferCell getCellAtIndex(int index)
	{
		return m_cells.get(index);
	}
	
	private void setCell(int m, int n, smBufferCell cell)
	{
		if ( !isInBoundsRelative(m, n) )
		{
			smU_Debug.ASSERT(false, "smCellBuffer::setCell1");
			return;
		}
		
		m_cells.set(m + n * m_width, cell);
	}
	
	public boolean isInBoundsAbsolute(smGridCoordinate absolute)
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
	
	public smBufferCell getCellAtAbsoluteCoord(smGridCoordinate absoluteCoord)
	{
		absoluteToRelative(absoluteCoord, s_utilCoord1);
		return getCellAtRelativeCoord(s_utilCoord1);
	}
	
	public smBufferCell getCellAtRelativeCoord(smGridCoordinate relativeCoord)
	{
		if ( !isInBoundsRelative(relativeCoord.getM(), relativeCoord.getN()) )
		{
			return null;
		}
		
		return m_cells.get(relativeCoord.getM() + relativeCoord.getN() * m_width);
	}
	
	public void absoluteToRelative(smGridCoordinate absCoord, smGridCoordinate coord_out)
	{
		int relativeM = absCoord.getM() - this.m_coordinate.getM();
		int relativeN = absCoord.getN() - this.m_coordinate.getN();
		coord_out.set(relativeM, relativeN);
	}
	
	public void relativeToAbsolute(smGridCoordinate relCoord, smGridCoordinate coord_out)
	{
		int absoluteM = relCoord.getM() + this.m_coordinate.getM();
		int absoluteN = relCoord.getN() + this.m_coordinate.getN();
		coord_out.set(absoluteM, absoluteN);
	}
	
	public boolean isTouching(smGridCoordinate absCoord)
	{
		int relativeM = absCoord.getM() - this.m_coordinate.getM();
		int relativeN = absCoord.getN() - this.m_coordinate.getN();
		return isInBoundsRelative(relativeM, relativeN);
	}
	
	void imposeBuffer(smA_Grid grid, smCellBuffer otherBuffer, smI_LocalCodeRepository localCodeSource, int options__extends__smF_BufferUpdateOption)
	{		
		boolean createVisualizations = (options__extends__smF_BufferUpdateOption & smF_BufferUpdateOption.CREATE_VISUALIZATIONS) != 0;
		boolean communicateWithServer = (options__extends__smF_BufferUpdateOption & smF_BufferUpdateOption.COMMUNICATE_WITH_SERVER) != 0;
		boolean flushPopulator = (options__extends__smF_BufferUpdateOption & smF_BufferUpdateOption.FLUSH_CELL_POPULATOR) != 0;
		
		smBufferCell ithCell = null;
		int i;
		boolean cellRecycled;
		int m, n;
		
		int otherSubCellCountDim = otherBuffer.getSubCellCount();
		int thisSubCellCountDim = this.getSubCellCount();
		int thisCellCount = this.getCellCount();
		int otherBufferCellCount = otherBuffer.getCellCount();
		
		smGridCoordinate absCoord = s_utilCoord1;
		smGridCoordinate relThisCoord = s_utilCoord2;
		
		//--- DRK > Easy case is when we have a size change...then everything is recycled.
		if ( otherSubCellCountDim != thisSubCellCountDim )
		{
			smU_Debug.ASSERT(false); // asserting here until zoomed-out meta cells are implemented.
			
			for ( i = 0; i < thisCellCount; i++ )
			{
				m = i % m_width;
				n = i / m_width;
			
				ithCell = null;
				
				relThisCoord.set(m, n);
				this.relativeToAbsolute(relThisCoord, absCoord);
				
				cellRecycled = false;
				
				if ( i < otherBuffer.m_cells.size() )
				{
					ithCell = otherBuffer.m_cells.get(i);
					otherBuffer.m_cells.set(i, null);
					cellRecycled = true;
				}
				else
				{
					ithCell = m_cellPool.allocCell(grid, this.m_subCellDimension, createVisualizations);
				}
				
				this.m_cells.set(i, ithCell);
				
				ithCell.getCoordinate().copy(absCoord);
				
				m_codeMngr.populateCell(ithCell, localCodeSource, m_subCellDimension, cellRecycled, communicateWithServer, smE_CodeType.SPLASH);
			}
		}
		
		//--- DRK > Hard case is when cell size is the same...then we have to do all kinds of teh crazy algorithms to
		//---		be as efficient in our cell-reuse as possible.
		else
		{
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
			
			smGridCoordinate relOtherCoord = s_utilCoord3;
			
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
					
					if( !grid.isTaken(absCoord) )
					{
						continue;
					}
					
					otherBuffer.absoluteToRelative(absCoord, relOtherCoord);
					
					cellRecycled = false;
					
					smBufferCell otherCell = null;
					
					if ( otherBuffer.isTouching(absCoord) )
					{						
						//--- DRK > Splice the still-visible cell from the other buffer into this buffer.
						otherCell = otherBuffer.getCellAtRelativeCoord(relOtherCoord);
						otherBuffer.setCell(relOtherCoord.getM(), relOtherCoord.getN(), null);
						this.setCell(m, n, otherCell);
						
						if ( otherCell == null )
						{
							smU_Debug.ASSERT(false, "imposeBuffer1");
						}
						
						//--- DRK > If possible, fill in the gap that is left in the other buffer with the cell
						//---		from the other buffer at the same m, n location that was skipped.
						smBufferCell cellToTransfer = otherBuffer.getCellAtRelativeCoord(relThisCoord);
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
							otherCell = m_cellPool.allocCell(grid, this.m_subCellDimension, createVisualizations);
						}
						
						this.setCell(m, n, otherCell);
					}
					
					smBufferCell imposedCell = getCellAtRelativeCoord(relThisCoord);
					imposedCell.getCoordinate().copy(absCoord);
					
					m_codeMngr.populateCell(otherCell, localCodeSource, m_subCellDimension, cellRecycled, communicateWithServer, smE_CodeType.SPLASH);
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
		}
		
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
			smBufferCell ithCell = this.m_cells.get(i);
			
			if( ithCell != null )
			{
				m_cellPool.deallocCell(ithCell);
			}
		}
		
		this.m_cells.clear();
		
		this.setExtents(0, 0, 0, 0);
	}
}