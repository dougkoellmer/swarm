package swarm.client.managers;

import java.util.ArrayList;
import java.util.logging.Logger;

import swarm.client.entities.smCamera;
import swarm.client.entities.smBufferCell;
import swarm.client.entities.smE_CodeStatus;
import swarm.client.structs.smCellPool;
import swarm.client.structs.smI_LocalCodeRepository;
import swarm.shared.app.smS_App;
import swarm.shared.utils.smU_BitTricks;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smA_Cell;
import swarm.shared.entities.smA_Grid;
import swarm.shared.entities.smE_CodeSafetyLevel;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smPoint;
import swarm.shared.structs.smGridCoordinate;

/**
 * ...
 * @author
 */
public class smCellBufferManager implements smI_LocalCodeRepository
{
	public static class Iterator
	{
		private int m_currentIndex = 0;
		
		void reset()
		{
			m_currentIndex = s_registeredInstances.size()-1; 
		}
		
		public smCellBufferManager next()
		{
			if( m_currentIndex >= 0 )
			{
				m_currentIndex--;
				
				return s_registeredInstances.get(m_currentIndex+1);
			}
			else
			{
				return null;
			}
		}
	}
	
	private static final Logger s_logger = Logger.getLogger(smCellBufferManager.class.getName());
	
	private smCellBuffer m_displayBuffer;
	private smCellBuffer m_backBuffer;
	
	private final smGridCoordinate m_utilCoord1 = new smGridCoordinate();
	private final smGridCoordinate m_utilCoord2 = new smGridCoordinate();
	private final smPoint m_utilPoint1 = new smPoint();
	private final smPoint m_utilPoint2 = new smPoint();
	
	private int m_updateCount = 0;
	
	private static final ArrayList<smCellBufferManager> s_registeredInstances = new ArrayList<smCellBufferManager>();
	
	private static final Iterator s_iterator = new Iterator();
	
	public smCellBufferManager(smCellCodeManager codeMngr) 
	{
		smCellPool cellPool = new smCellPool();
		
		m_displayBuffer = new smCellBuffer(codeMngr, cellPool);
		m_backBuffer = new smCellBuffer(codeMngr, cellPool);
	}
	
	public int getUpdateCount()
	{
		return m_updateCount;
	}
	
	private void swapBuffers()
	{
		smCellBuffer temp = m_displayBuffer;
		m_displayBuffer = m_backBuffer;
		m_backBuffer = temp;
	}
	
	public smCellBuffer getDisplayBuffer()
	{
		return m_displayBuffer;
	}
	
	/*public static void registerInstance(smCellBufferManager instance)
	{
		s_registeredInstances.add(instance);
	}
	
	public static Iterator getRegisteredInstances()
	{
		s_iterator.reset();
		
		return s_iterator;
	}
	
	public static void unregisterInstance(smCellBufferManager instance)
	{
		for( int i = s_registeredInstances.size()-1; i >= 0; i-- )
		{
			if( s_registeredInstances.get(i) == instance )
			{
				s_registeredInstances.remove(i);
				
				return;
			}
		}
	}*/
	
	public void drain()
	{
		m_displayBuffer.drain();
		m_backBuffer.drain();
	}

	public void update(smA_Grid grid, smCamera camera, smI_LocalCodeRepository alternativeCodeSource, int options__extends__smF_BufferUpdateOption)
	{
		m_updateCount++;
		
		//--- DRK > Figure out how big each cell is relative to a fully zoomed in cell.
		/*int gridSize = grid.getSize();
		int gridSizeUpperOf2 = gridSize == 0 ? 0 : smU_BitTricks.calcUpperPowerOfTwo(gridSize);
		double distanceRatio = camera.calcDistanceRatio();
		int cellSize = 1;
		double pixelCount = smS_App.CELL_PLUS_SPACING_PIXEL_COUNT;
		double cellNMultiplier = (pixelCount * distanceRatio);
		double n = smS_App.CELL_PIXEL_COUNT / cellNMultiplier;
		n = n < 1 ? 1 : n;
		cellSize = (int) Math.floor(n);
		cellSize = smU_BitTricks.calcLowerPowerOfTwo(cellSize);
		cellSize = cellSize <= 4 ? 1 : cellSize; // COMMENT OUT TO get correct cell sizes.
		cellSize = cellSize > gridSizeUpperOf2 ? gridSizeUpperOf2 : cellSize;*/
		
		int subCellDim = 1;
		
		//--- DRK > Calculate maximum "raw" buffer position and size, not caring about constraints.
		this.calcRawBufferDimensions(camera, grid, subCellDim, m_utilCoord1, m_utilCoord2);
		int newBufferWidth = m_utilCoord2.getM();
		int newBufferHeight = m_utilCoord2.getN();
		
		//s_logger.severe("Raw: " + m_utilCoord1.toString() + " " + newBufferWidth + " " + newBufferHeight);		
		
		//--- DRK > Constrain both the position and size of the buffer if necessary so it maps onto the grid in a minimal fashion.
		//---		The following is capable of creating a buffer with zero cells, which is perfectly acceptable.
		int gridWidthRemainder = subCellDim > 0 ? grid.getWidth() % subCellDim : 0;
		int relativeGridWidth = grid.getWidth() == 0 ? 0 : (grid.getWidth() - gridWidthRemainder) / subCellDim;
		relativeGridWidth += gridWidthRemainder > 0 ? 1 : 0;
		
		int gridHeightRemainder = subCellDim > 0 ? grid.getHeight() % subCellDim : 0;
		int relativeGridHeight = grid.getHeight() == 0 ? 0 : (grid.getHeight() - gridHeightRemainder) / subCellDim;
		relativeGridHeight += gridHeightRemainder > 0 ? 1 : 0;
		
		
		if( m_utilCoord1.getM() < 0 )
		{
			newBufferWidth += m_utilCoord1.getM(); // lower the width
			m_utilCoord1.setM(0);
			newBufferWidth = newBufferWidth > 0 ? newBufferWidth : 0;
		}
		if( m_utilCoord1.getM() + newBufferWidth >= relativeGridWidth )
		{
			newBufferWidth -= (m_utilCoord1.getM() + newBufferWidth) - relativeGridWidth;
			newBufferWidth = newBufferWidth > 0 ? newBufferWidth : 0;
		}
		if( m_utilCoord1.getN() < 0 )
		{
			newBufferHeight += m_utilCoord1.getN();
			m_utilCoord1.setN(0);
			newBufferHeight = newBufferHeight > 0 ? newBufferHeight : 0;
		}
		if( m_utilCoord1.getN() + newBufferHeight >= relativeGridHeight )
		{
			newBufferHeight -= (m_utilCoord1.getN() + newBufferHeight) - relativeGridHeight;
			newBufferHeight = newBufferHeight > 0 ? newBufferHeight : 0;
		}
		
		//s_logger.severe("Constrained: " + m_utilCoord1.toString() + " " + newBufferWidth + " " + newBufferHeight);
		//s_logger.severe("");
		
		m_backBuffer.setExtents(m_utilCoord1.getM(), m_utilCoord1.getN(), newBufferWidth, newBufferHeight);
		m_backBuffer.setCellSize(subCellDim);
		m_backBuffer.imposeBuffer(grid, m_displayBuffer, alternativeCodeSource, options__extends__smF_BufferUpdateOption);
		
		//s_logger.info(smCellPool.getInstance().getAllocCount() + "");
		
		this.swapBuffers();
	}
	
	private void calcRawBufferDimensions(smCamera camera, smA_Grid grid, int subCellCountDim, smGridCoordinate topLeft_out, smGridCoordinate widthHeight_out)
	{
		if( subCellCountDim == 0 || grid.getWidth() == 0 || grid.getHeight() == 0 )
		{
			topLeft_out.set(0, 0);
			widthHeight_out.set(0,  0);
			
			return;
		}
		
		smGridCoordinate coordOfCenterCell = topLeft_out;
		smPoint worldPointOfCenterCell = m_utilPoint1;
		smPoint screenPointOfCenterCell = m_utilPoint2;
		double distanceRatio = camera.calcDistanceRatio();
		
		double cellWidthPlusPadding = (grid.getCellWidth() + grid.getCellPadding()) * subCellCountDim;
		double scaledCellWidth = (cellWidthPlusPadding) * distanceRatio;
		double cellHeightPlusPadding = (grid.getCellHeight() + grid.getCellPadding()) * subCellCountDim;
		double scaledCellHeight = (cellHeightPlusPadding) * distanceRatio;
		
		coordOfCenterCell.setWithPoint(camera.getPosition(), cellWidthPlusPadding, cellHeightPlusPadding);
		worldPointOfCenterCell.set(coordOfCenterCell.getM()*cellWidthPlusPadding, coordOfCenterCell.getN()*cellHeightPlusPadding, 0.0);
		worldPointOfCenterCell.floor();
		camera.calcScreenPoint(worldPointOfCenterCell, screenPointOfCenterCell);
		
		double screenX = screenPointOfCenterCell.getX();
		double screenY = screenPointOfCenterCell.getY();
		double screenWidth = camera.getViewWidth();
		double screenHeight = camera.getViewHeight();
		
		int cellsToTheLeft = (int) (screenX < 0 ? 0 : Math.ceil(screenX / scaledCellWidth));
		int cellsToTheTop = (int) (screenY < 0 ? 0 : Math.ceil(screenY / scaledCellHeight));
		
		screenX += scaledCellWidth;
		screenY += scaledCellHeight;
		
		int cellsToTheRight = (int) (screenX > screenWidth ? 0 : Math.ceil( (screenWidth-screenX) / scaledCellWidth));
		int cellsToTheBottom = (int) (screenY > screenHeight ? 0 : Math.ceil( (screenHeight-screenY) / scaledCellHeight));

		topLeft_out.incM(-cellsToTheLeft);
		topLeft_out.incN(-cellsToTheTop);
		widthHeight_out.setM(cellsToTheLeft + 1 + cellsToTheRight);
		widthHeight_out.setN(cellsToTheTop + 1 + cellsToTheBottom);
	}
	
	@Override
	public boolean tryPopulatingCell(smGridCoordinate coordinate, smE_CodeType eType, smA_Cell outCell)
	{
		smCode toReturn = null;
		smBufferCell thisCell = null;
		
		if( m_displayBuffer.isInBoundsAbsolute(coordinate) )
		{
			thisCell = m_displayBuffer.getCellAtAbsoluteCoord(coordinate);
			
			if( thisCell.getStatus(eType) == smE_CodeStatus.HAS_CODE )
			{
				toReturn = thisCell.getCode(eType);
			}
			else
			{
				for( int i = 0; i < smE_CodeType.values().length; i++ )
				{
					smE_CodeType ithType = smE_CodeType.values()[i];
					
					if( ithType == eType )
					{
						continue;
					}
					
					if( thisCell.getStatus(ithType) == smE_CodeStatus.HAS_CODE )
					{
						smCode potentialStandIn = thisCell.getCode(ithType);
						
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
							smU_Debug.ASSERT(false, "Code shouldn't be null here.");
						}
					}
				}
				
				if( toReturn /*still*/ == null )
				{
					//--- DRK > TODO: A minor hack here doing an instanceof...will let it pass for now,
					//---			  but a better implementation is welcome.
					if( outCell instanceof smBufferCell)
					{
						smBufferCell outCellAsBufferCell = (smBufferCell) outCell;
						
						if( thisCell.getStatus(eType) == smE_CodeStatus.WAITING_ON_CODE )
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
			
			if( eType == smE_CodeType.SOURCE || toReturn.getSafetyLevel() == smE_CodeSafetyLevel.REQUIRES_DYNAMIC_SANDBOX )
			{
				outCell.getCodePrivileges().copy(thisCell.getCodePrivileges());
			}
			
			return true;
		}
		
		return false;
	}
}