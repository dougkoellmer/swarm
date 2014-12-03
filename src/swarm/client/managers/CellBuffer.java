package swarm.client.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;







import swarm.client.entities.BufferCell;
import swarm.client.entities.Camera;
import swarm.client.entities.ClientGrid;
import swarm.client.entities.I_BufferCellVisualization;
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
public class CellBuffer extends A_BufferCellList
{
	private static final Logger s_logger = Logger.getLogger(CellBuffer.class.getName());
	
	private static final CellAddressMapping s_utilMapping = new CellAddressMapping();
	
	private final GridCoordinate m_coord = new GridCoordinate();
//	private final ArrayList<BufferCell> m_cells = new ArrayList<BufferCell>();
	private int m_width = 0;
	private int m_height = 0;
	
	private final CellBufferManager m_parent;
	private final CellCodeManager m_codeMngr;
	private final CellSizeManager m_cellSizeMngr;
	
	private final ClientGrid.Obscured m_obscured = new ClientGrid.Obscured();
	
	private final CellKillQueue m_killQueue;
	
	CellBuffer(CellBufferManager parent, CellCodeManager codeMngr, BufferCellPool cellPool, CellSizeManager cellSizeMngr, int subCellCount, CellKillQueue killQueue)
	{
		super(parent, subCellCount, cellPool);
		
		m_codeMngr = codeMngr;
		m_cellSizeMngr = cellSizeMngr;
		m_parent = parent;
		m_killQueue = killQueue;
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
		return m_cellList.size() + m_killQueue.m_cellList.size();
	}
	
	void setExtents(int m, int n, int width, int height)
	{
		m_coord.set(m, n);
		
		m_width = width;
		m_height = height;
	}
	
	public BufferCell getCellAtIndex(int index)
	{
		if( index < m_cellList.size() )
		{
			return m_cellList.get(index);
		}
		else
		{
			if( m_killQueue.m_cellList.size() > 0 )
			{
				return m_killQueue.m_cellList.get(index-m_cellList.size());
			}
		}
		
		return null;
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
	
	private void maybeSentenceToDeath(BufferCell cell)
	{
		if( !cell.getVisualization().isLoaded() )
		{
//			if( m_subCellCount == 1 )  s_logger.severe("DESTROYED");
			m_cellPool.deallocCell(cell);
		}
		else
		{
//			if( m_subCellCount == 1 )  s_logger.severe("SENTENCED");
			m_killQueue.sentenceToDeath(cell);
		}
	}
	
	void imposeBuffer(ClientGrid grid, Camera camera, CellBuffer otherBuffer, GridCoordinate snapCoord_nullable, I_LocalCodeRepository localCodeSource, int highestSubCellCount, int highestSubCellCount_notOverridden, int options__extends__smF_BufferUpdateOption)
	{
		boolean createVisualizations = (options__extends__smF_BufferUpdateOption & F_BufferUpdateOption.CREATE_VISUALIZATIONS) != 0;
		boolean communicateWithServer = (options__extends__smF_BufferUpdateOption & F_BufferUpdateOption.COMMUNICATE_WITH_SERVER) != 0;
		boolean flushPopulator = (options__extends__smF_BufferUpdateOption & F_BufferUpdateOption.FLUSH_CELL_POPULATOR) != 0;
		boolean justRemovedOverride = (options__extends__smF_BufferUpdateOption & F_BufferUpdateOption.JUST_REMOVED_OVERRIDE) != 0;
		boolean madeOrSwappedSnapTargetCell = false;

		int i, m, n;
		
		int otherBufferCellCount = otherBuffer.m_cellList.size();

		if( m_subCellCount > highestSubCellCount )
		{
			for( i = otherBufferCellCount-1; i >= 0 ; i-- )
			{
				BufferCell ithCellFromOtherBuffer = otherBuffer.m_cellList.get(i);
				
				if( !this.isInBoundsAbsolute(ithCellFromOtherBuffer.getCoordinate()) )
				{
					m_cellPool.deallocCell(ithCellFromOtherBuffer);
				}
				else
				{
					maybeSentenceToDeath(ithCellFromOtherBuffer);
				}
			}
			
			otherBuffer.m_cellList.clear();
			
			return;
		}
		else
		{
			for( i = 0; i < otherBuffer.m_cellList.size(); i++ )
			{
				BufferCell ithCellFromOtherBuffer = otherBuffer.m_cellList.get(i);
				
				if( !this.isInBoundsAbsolute(ithCellFromOtherBuffer.getCoordinate()) )
				{
//					if( m_subCellCount == 1 )  s_logger.severe("DESTROYED");
					
					m_cellPool.deallocCell(ithCellFromOtherBuffer);
					otherBuffer.m_cellList.set(i, null);
				}
			}
		}

		int limitN = m_coord.getN() + m_height;
		int limitM = m_coord.getM() + m_width;
		final boolean aboveCurrentSubCellCount = highestSubCellCount > m_subCellCount;
		for ( n = m_coord.getN(); n < limitN; n++ )
		{
			for ( m = m_coord.getM(); m < limitM; m++ )
			{
//				boolean isDebugCell = m_subCellCount == 1 && m == 10 && n == 16;
				boolean isBeingSnappedTo = snapCoord_nullable != null && m_subCellCount == 1 && m == snapCoord_nullable.getM() && n == snapCoord_nullable.getN();
	
				boolean obscured = false;
				
				if( aboveCurrentSubCellCount )
				{
					if( grid.isObscured(m, n, m_subCellCount, highestSubCellCount, m_obscured) )
					{
						obscured = true;
						
						if( !isBeingSnappedTo )
						{
							CellBuffer higherBuffer = m_parent.getDisplayBuffer(U_Bits.calcBitPosition(m_obscured.subCellCount));
							BufferCell obscuringCell = higherBuffer.getCellAtAbsoluteCoord(m_obscured.m, m_obscured.n);
							
							if( obscuringCell != null )
							{
								I_BufferCellVisualization obscuringCellVisualization = obscuringCell.getVisualization();
								
								if( obscuringCellVisualization.isLoaded() )
								{
									if( m_obscured.offset == 1 )
									{
										continue;
									}
									else if( m_obscured.offset > 1 )
									{
										m += m_obscured.offset;
										m -= 1; // cancel out next increment in for loop
										
										continue;
									}
								}
							}
						}
					}
				}
				
				if( !grid.isTaken(m, n, m_subCellCount) )  continue;
				
				//--- DRK > For a brief time the swap method's success was determined by us being aboveCurrentSubCellCount
				//---		but I believe I meant aboveCurrentSubCellCount && obscured. This still might be faulty.
				//---		Maybe we want to be greedy and never check if cell is loaded. !isBeingSnappedTo added now later in the day too.
				boolean onlySwapIfLoaded = !isBeingSnappedTo && aboveCurrentSubCellCount && obscured;
//				boolean checkIsLoaded = false;
				
				if( swap(m, n, otherBuffer, this, onlySwapIfLoaded) != null )
				{
//					if( m_subCellCount == 1 )  s_logger.severe("SWAPPED");

					madeOrSwappedSnapTargetCell = isBeingSnappedTo ? true : madeOrSwappedSnapTargetCell;
					
					continue;
				}
				
				if( !obscured || isBeingSnappedTo )
				{
					final BufferCell cellFromKillQueue = swap(m, n, m_killQueue, this, /*onlySwapIfLoaded=*/false);
					if( cellFromKillQueue != null )
					{
//						if( m_subCellCount == 1 )  s_logger.severe("SAVED");
						
						madeOrSwappedSnapTargetCell = isBeingSnappedTo ? true : madeOrSwappedSnapTargetCell;
						cellFromKillQueue.saveFromDeathSentence();
						
						continue;
					}
				}
				
				if( !isBeingSnappedTo )
				{
					//--- DRK > If we're obscured then although we attempt to swap an existing cell
					//---		from the other buffer or the kill queue above, we don't make new cells.
					if ( obscured )  continue;
				}
				else
				{
					madeOrSwappedSnapTargetCell = true;
				}
				
				makeNewCell(m, n, grid, localCodeSource, createVisualizations, communicateWithServer, justRemovedOverride);
			}
		}
		
		//--- DRK > If this is the snap target then we try to preserve the cell from the other buffer or from the kill queue.
		//---		If we can't do that then we make it from scratch. We do this here cause it may have been skipped over in
		//---		the metacell skip over logic at the top of the loop above.
		if( !madeOrSwappedSnapTargetCell && m_subCellCount == 1 && snapCoord_nullable != null )
		{
			if( swap(snapCoord_nullable.getM(), snapCoord_nullable.getN(), otherBuffer, this, /*onlySwapIfLoaded=*/false) == null )
			{
				BufferCell cellSavedFromDeathSentence = swap(snapCoord_nullable.getM(), snapCoord_nullable.getN(), m_killQueue, this, /*onlySwapIfLoaded=*/false);
				if( cellSavedFromDeathSentence != null )
				{
					cellSavedFromDeathSentence.saveFromDeathSentence();
				}
				
				else if( this.isInBoundsAbsolute(snapCoord_nullable) )
				{
					makeNewCell(snapCoord_nullable.getM(), snapCoord_nullable.getN(), grid, localCodeSource, createVisualizations, communicateWithServer, justRemovedOverride);
				}
			}
		}
		
		//--- DRK > For cases where we start at cell_1, zoom out to a higher meta level, then come back faster than the kill queue
		//---		time, this block preserves cell_1s that are still on the cell_1 kill queue or the other buffer while meta count
		//---		is overridden...we're not making fresh cells at this point, until meta count override is removed.
		if( highestSubCellCount_notOverridden < highestSubCellCount && highestSubCellCount_notOverridden == m_subCellCount )
		{
			preserveCellsForOverrideCase(highestSubCellCount, highestSubCellCount_notOverridden, otherBuffer);
			preserveCellsForOverrideCase(highestSubCellCount, highestSubCellCount_notOverridden, m_killQueue);
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
				maybeSentenceToDeath(ithCell);
			}
		}

		otherBuffer.m_cellList.clear();
	}
	
	private void preserveCellsForOverrideCase(int highestSubCellCount, int highestSubCellCount_notOverridden, A_BufferCellList cellList)
	{
		for( int i = 0; i < cellList.m_cellList.size(); i++ )
		{
			BufferCell ithCell = cellList.m_cellList.get(i);

			if( ithCell != null && this.isInBoundsAbsolute(ithCell.getCoordinate()) )
			{
				BufferCell preservedCell = swap(i, cellList, this, /*onlySwapIfLoaded=*/false);
				
				if( preservedCell != null )
				{
					if( cellList == m_killQueue )
					{
						preservedCell.saveFromDeathSentence();
					}
				}
			}
		}
	}
	
	private void makeNewCell(int m, int n, ClientGrid grid, I_LocalCodeRepository localCodeSource, boolean createVisualizations, boolean communicateWithServer, boolean justRemovedOverride)
	{
		int highestPossibleSubCellCount = 0x1 << m_parent.getBufferCount();
		BufferCell newCell = m_cellPool.allocCell(grid, m_subCellCount, highestPossibleSubCellCount, createVisualizations, m, n, justRemovedOverride);
		this.m_cellList.add(newCell);
		
//		if( m_subCellCount == 1 )  s_logger.severe("CREATED");
		
//		if( m_subCellCount == 2 )
//		{
//			s_logger.severe("ERER");;
//		}
		
//		s_logger.severe(m_subCellCount+"");
		m_codeMngr.populateCell(newCell, localCodeSource, m_subCellCount, communicateWithServer, E_CodeType.SPLASH);
		
		s_utilMapping.getCoordinate().copy(newCell.getCoordinate());
		
		if( m_subCellCount == 1 )
		{
			m_cellSizeMngr.populateCellSize(s_utilMapping, this.m_parent, newCell);
		}
	}
	
	@Override public BufferCell getCellAtAbsoluteCoord(int m, int n)
	{
		BufferCell fromSuper = super.getCellAtAbsoluteCoord(m, n);
		
		if( fromSuper != null )  return fromSuper;
		
		return m_killQueue.getCellAtAbsoluteCoord(m, n);
	}
	
	@Override void drain()
	{
		super.drain();
		
		this.setExtents(0, 0, 0, 0);
	}
	
	@Override public String toString()
	{
		return m_coord.getM() + " " + m_coord.getN() + " " +  m_width + " " +  m_height;
	}
}