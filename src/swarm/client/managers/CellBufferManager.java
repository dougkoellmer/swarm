package swarm.client.managers;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.dougkoellmer.shared.app.S_App;

import swarm.client.entities.Camera;
import swarm.client.entities.BufferCell;
import swarm.client.entities.ClientGrid;
import swarm.client.entities.E_CodeStatus;
import swarm.client.structs.BufferCellPool;
import swarm.client.structs.I_LocalCodeRepository;
import swarm.shared.app.S_CommonApp;
import swarm.shared.utils.U_Bits;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Cell;
import swarm.shared.entities.A_Grid;
import swarm.shared.entities.E_CodeSafetyLevel;
import swarm.shared.entities.E_CodeType;
import swarm.shared.structs.Code;
import swarm.shared.structs.Point;
import swarm.shared.structs.GridCoordinate;

/**
 * ...
 * @author
 */
public class CellBufferManager implements I_LocalCodeRepository
{
	public static class Iterator
	{
		private int m_currentIndex = 0;
		
		private final ArrayList<CellBufferManager> m_bufferMngrs;
		
		public Iterator(ArrayList<CellBufferManager> bufferMngrs)
		{
			m_bufferMngrs = bufferMngrs;
		}
		
		public void reset()
		{
			m_currentIndex = m_bufferMngrs.size()-1; 
		}
		
		public CellBufferManager next()
		{
			if( m_currentIndex >= 0 )
			{
				m_currentIndex--;
				
				return m_bufferMngrs.get(m_currentIndex+1);
			}
			else
			{
				return null;
			}
		}
	}
	
	private static final Logger s_logger = Logger.getLogger(CellBufferManager.class.getName());
	
	private int m_updateCount = 0;
	
	private final BufferCellPool m_cellPool;
	private int m_currentSubCellCount = 1;
	private Integer m_overrideSubCellCount = null;
	
	private CellBufferPair[] m_bufferPairs = null;
	private final CellCodeManager m_codeMngr;
	private final CellSizeManager m_sizeMngr;
	private final int m_levelCount;
	
	private boolean m_dirty = false;
	
	
	public CellBufferManager(ClientGrid grid, CellCodeManager codeMngr, CellSizeManager cellSizeMngr, int metaLevelCount) 
	{
		m_cellPool = new BufferCellPool();
		m_codeMngr = codeMngr;
		m_sizeMngr = cellSizeMngr;
		m_levelCount = metaLevelCount + 1;
		
		createBufferPairs(grid);
	}
	
	public void overrideSubCellCount()
	{
		m_overrideSubCellCount = m_currentSubCellCount;
	}
	
	public void removeOverrideSubCellCount()
	{
		m_overrideSubCellCount = null;
	}
	
	private void createBufferPairs(ClientGrid grid)
	{
		CellBufferPair[] oldPairs = m_bufferPairs;
		
		m_bufferPairs = new CellBufferPair[m_levelCount];
		
		for( int i = 0; i < m_bufferPairs.length; i++ )
		{
			int subCellCount = 0x1 << i;
			
			if( subCellCount != grid.modifySubCellCount(subCellCount) )  continue;
			
			if( oldPairs != null && i < oldPairs.length )
			{
				m_bufferPairs[i] = oldPairs[i];
			}
			else
			{
				m_bufferPairs[i] = new CellBufferPair(this, m_codeMngr, m_sizeMngr, m_cellPool, subCellCount);
			}
		}
	}
	
	public BufferCellPool getCellPool()
	{
		return m_cellPool;
	}
	
	public int getUpdateCount()
	{
		return m_updateCount;
	}
	
//	public CellBuffer getDisplayBuffer()
//	{
//		if( m_bufferPairs == null )  return null;
//		if( m_bufferPairs.length == 0 )  return null;
//		
//		return m_bufferPairs[0].getDisplayBuffer();
//	}
	
	public void drain()
	{
		if( m_bufferPairs == null )  return;
		
		for( int i = 0; i < m_bufferPairs.length; i++ )
		{
			CellBufferPair ithPair = m_bufferPairs[i];
			
			if( ithPair == null )  continue;
			
			ithPair.drain();
		}
	}
	
	public void update_cameraStill(double timestep)
	{
		if( m_bufferPairs == null )  return;
		
		for( int i = 0; i < m_bufferPairs.length; i++ )
		{
			CellBufferPair ithPair = m_bufferPairs[i];
			
			if( ithPair == null )  continue;
			
			ithPair.update_cameraStill(timestep);
		}
	}

	public void update_cameraMoving(double timestep, ClientGrid grid, Camera camera, GridCoordinate snappingCoordinate_nullable, I_LocalCodeRepository alternativeCodeSource, int options__extends__smF_BufferUpdateOption)
	{
		m_updateCount++;
		
		if( m_bufferPairs == null )  return;
		
		//--- DRK > Figure out how big each cell is relative to a fully zoomed in cell.
		int gridSize = grid.getWidth();
		int gridSizeUpperOf2 = gridSize == 0 ? 0 : U_Bits.calcUpperPowerOfTwo(gridSize);
		double distanceRatio = camera.calcDistanceRatio();
		int subCellCount = 1;
		double pixelCount = grid.getCellWidth() + grid.getCellPadding();
		double cellNMultiplier = (pixelCount * distanceRatio);
		double n = grid.getCellWidth() / cellNMultiplier;
		n = n < 1 ? 1 : n;
		subCellCount = (int) Math.floor(n);
		subCellCount = U_Bits.calcLowerPowerOfTwo(subCellCount);
//		cellSize = cellSize <= 4 ? 1 : cellSize; // COMMENT OUT TO get correct cell sizes.
		subCellCount = subCellCount > gridSizeUpperOf2 ? gridSizeUpperOf2 : subCellCount;
		
		subCellCount = grid.modifySubCellCount(subCellCount);
		
		m_currentSubCellCount = subCellCount == 1 || m_overrideSubCellCount == null ? subCellCount : m_overrideSubCellCount;
		
		int index = U_Bits.calcBitPosition(m_currentSubCellCount);
		index = index >= m_bufferPairs.length ? m_bufferPairs.length-1 : index;
		m_currentSubCellCount = 0x1 << index;
		
		for( int i = m_bufferPairs.length-1; i >= 0; i-- )
		{
			if( m_bufferPairs[i] == null )  continue;
			
			m_bufferPairs[i].update_cameraMoving(timestep, grid, camera, snappingCoordinate_nullable, alternativeCodeSource, options__extends__smF_BufferUpdateOption, m_currentSubCellCount);
		}
		
//		s_logger.severe(m_cellPool.getCheckOutCount()+"/" + m_cellPool.getAllocCount());
	}
	
	public CellBuffer getDisplayBuffer(int index)
	{
		return m_bufferPairs[index] != null ? m_bufferPairs[index].getDisplayBuffer() : null;
	}
	
	public int getBufferCount()
	{
		return m_bufferPairs != null ? m_bufferPairs.length : 0;
	}
	
	public int getSubCellCount()
	{
		return m_currentSubCellCount;
	}
	
	public CellBuffer getHighestDisplayBuffer()
	{
		int index = U_Bits.calcBitPosition(m_currentSubCellCount);
		index = index >= m_bufferPairs.length ? m_bufferPairs.length-1 : index;
		return m_bufferPairs != null ? m_bufferPairs[index].getDisplayBuffer() : null;
	}
	
	public CellBuffer getLowestDisplayBuffer()
	{
		if( m_bufferPairs != null )
		{
			return m_bufferPairs[0].getDisplayBuffer();
		}
		
		return null;
	}
	
	@Override
	public boolean tryPopulatingCell(GridCoordinate coordinate, E_CodeType eType, A_Cell outCell)
	{
		Code toReturn = null;
		BufferCell thisCell = null;
		
		CellBuffer displayBuffer = getLowestDisplayBuffer();
		
		if( displayBuffer.isInBoundsAbsolute(coordinate) )
		{
			thisCell = displayBuffer.getCellAtAbsoluteCoord(coordinate);
			
			if( thisCell == null )  return false;
			
			if( thisCell.getStatus(eType) == E_CodeStatus.HAS_CODE )
			{
				toReturn = thisCell.getCode(eType);
			}
			else
			{
				for( int i = 0; i < E_CodeType.values().length; i++ )
				{
					E_CodeType ithType = E_CodeType.values()[i];
					
					if( ithType == eType )
					{
						continue;
					}
					
					if( thisCell.getStatus(ithType) == E_CodeStatus.HAS_CODE )
					{
						Code potentialStandIn = thisCell.getCode(ithType);
						
						if( potentialStandIn != null )
						{
							if( potentialStandIn.isStandInFor(eType) )
							{
								toReturn = potentialStandIn;
								
								break;
							}
						}
						else
						{
							U_Debug.ASSERT(false, "Code shouldn't be null here.");
						}
					}
				}
				
				if( toReturn /*still*/ == null )
				{
					//--- DRK > TODO: A minor hack here doing an instanceof...will let it pass for now,
					//---			  but a better implementation is welcome.
					if( outCell instanceof BufferCell)
					{
						BufferCell outCellAsBufferCell = (BufferCell) outCell;
						
						if( thisCell.getStatus(eType) == E_CodeStatus.WAITING_ON_CODE )
						{
							//--- DRK > NOTE: Not COMPLETELY sure this is good to do...pretty sure though.
							outCellAsBufferCell.onServerRequest(eType);
							
							return true;
						}
						
						//--- DRK > Had this if block executing for a while, but took it out, because it's not desirable for
						//---		at least one case...user snaps to a cell that's off-screen, snap buffer makes call to server,
						//---		transient error occurs (let's say some random http error), then view buffer comes into view of
						//---		the target cell. In this case, it makes more sense for the view buffer to attempt to contact
						//---		the server again. Note that the snap buffer *will not* automatically attempt to contact the server again
						//---		after an error, because it's only updated once at the beginning of a snap (or when view/grid changes size).
						//---
						//---		There may still be a case where this is the desired action...we shall see.
						/*else if( thisCell.getStatus(eType) == smE_CodeStatus.GET_ERROR )
						{
							outCellAsBufferCell.onGetResponseError(eType);
							
							return true;
						}*/
					}
				}
			}			
		}
		
		if( toReturn != null )
		{
			outCell.setCode(eType, toReturn);
			
			if( eType == E_CodeType.SOURCE || toReturn.getSafetyLevel() == E_CodeSafetyLevel.VIRTUAL_DYNAMIC_SANDBOX )
			{
				outCell.getCodePrivileges().copy(thisCell.getCodePrivileges());
			}
			
			return true;
		}
		
		return false;
	}
}