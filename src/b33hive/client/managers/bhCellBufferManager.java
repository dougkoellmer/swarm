package b33hive.client.managers;

import java.util.ArrayList;
import java.util.logging.Logger;

import b33hive.client.entities.bhCamera;
import b33hive.client.entities.bhBufferCell;
import b33hive.client.entities.bhE_CodeStatus;
import b33hive.client.structs.bhI_LocalCodeRepository;
import b33hive.shared.app.bhS_App;
import b33hive.shared.utils.bhU_BitTricks;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.entities.bhA_Cell;
import b33hive.shared.entities.bhA_Grid;
import b33hive.shared.entities.bhE_CodeSafetyLevel;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhPoint;
import b33hive.shared.structs.bhGridCoordinate;

/**
 * ...
 * @author
 */
public class bhCellBufferManager implements bhI_LocalCodeRepository
{
	public static class Iterator
	{
		private int m_currentIndex = 0;
		
		void reset()
		{
			m_currentIndex = s_registeredInstances.size()-1; 
		}
		
		public bhCellBufferManager next()
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
	
	private static final Logger s_logger = Logger.getLogger(bhCellBufferManager.class.getName());
	
	private static bhCellBufferManager s_instance = null;
	
	private bhCellBuffer m_displayBuffer = new bhCellBuffer();
	private bhCellBuffer m_backBuffer = new bhCellBuffer();
	
	private final bhGridCoordinate m_utilCoord1 = new bhGridCoordinate();
	private final bhGridCoordinate m_utilCoord2 = new bhGridCoordinate();
	private final bhPoint m_utilPoint1 = new bhPoint();
	private final bhPoint m_utilPoint2 = new bhPoint();
	
	private int m_updateCount = 0;
	
	private static final ArrayList<bhCellBufferManager> s_registeredInstances = new ArrayList<bhCellBufferManager>();
	
	private static final Iterator s_iterator = new Iterator();
	
	public bhCellBufferManager() 
	{
	}
	
	public int getUpdateCount()
	{
		return m_updateCount;
	}
	
	private void swapBuffers()
	{
		bhCellBuffer tempBuffer = m_displayBuffer;
		m_displayBuffer = m_backBuffer;
		m_backBuffer = tempBuffer;
	}
	
	public bhCellBuffer getDisplayBuffer()
	{
		return m_displayBuffer;
	}
	
	public static bhCellBufferManager getInstance()
	{
		if( s_instance == null )
		{
			s_instance = new bhCellBufferManager();
			registerInstance(s_instance);
		}
		
		return s_instance;
	}
	
	public static void registerInstance(bhCellBufferManager instance)
	{
		s_registeredInstances.add(instance);
	}
	
	public static Iterator getRegisteredInstances()
	{
		s_iterator.reset();
		
		return s_iterator;
	}
	
	public static void unregisterInstance(bhCellBufferManager instance)
	{
		for( int i = s_registeredInstances.size()-1; i >= 0; i-- )
		{
			if( s_registeredInstances.get(i) == instance )
			{
				s_registeredInstances.remove(i);
				
				return;
			}
		}
	}
	
	public void drain()
	{
		m_displayBuffer.drain();
		m_backBuffer.drain();
	}

	public void update(bhA_Grid grid, bhCamera camera, bhI_LocalCodeRepository alternativeCodeSource, int options__extends__bhF_BufferUpdateOption)
	{
		m_updateCount++;
		
		//--- DRK > Figure out how big each cell is relative to a fully zoomed in cell.
		/*int gridSize = grid.getSize();
		int gridSizeUpperOf2 = gridSize == 0 ? 0 : bhU_BitTricks.calcUpperPowerOfTwo(gridSize);
		double distanceRatio = camera.calcDistanceRatio();
		int cellSize = 1;
		double pixelCount = bhS_App.CELL_PLUS_SPACING_PIXEL_COUNT;
		double cellNMultiplier = (pixelCount * distanceRatio);
		double n = bhS_App.CELL_PIXEL_COUNT / cellNMultiplier;
		n = n < 1 ? 1 : n;
		cellSize = (int) Math.floor(n);
		cellSize = bhU_BitTricks.calcLowerPowerOfTwo(cellSize);
		cellSize = cellSize <= 4 ? 1 : cellSize; // COMMENT OUT TO get correct cell sizes.
		cellSize = cellSize > gridSizeUpperOf2 ? gridSizeUpperOf2 : cellSize;*/
		
		int subCellCountAcross = 1;
		
		//--- DRK > Calculate maximum "raw" buffer position and size, not caring about constraints.
		this.calcRawBufferDimensions(camera, grid, subCellCountAcross, m_utilCoord1, m_utilCoord2);
		int newBufferWidth = m_utilCoord2.getM();
		int newBufferHeight = m_utilCoord2.getN();
		
		//s_logger.severe("Raw: " + m_utilCoord1.toString() + " " + newBufferWidth + " " + newBufferHeight);		
		
		//--- DRK > Constrain both the position and size of the buffer if necessary so it maps onto the grid in a minimal fashion.
		//---		The following is capable of creating a buffer with zero cells, which is perfectly acceptable.
		int gridWidthRemainder = subCellCountAcross > 0 ? grid.getWidth() % subCellCountAcross : 0;
		int relativeGridWidth = grid.getWidth() == 0 ? 0 : (grid.getWidth() - gridWidthRemainder) / subCellCountAcross;
		relativeGridWidth += gridWidthRemainder > 0 ? 1 : 0;
		
		int gridHeightRemainder = subCellCountAcross > 0 ? grid.getHeight() % subCellCountAcross : 0;
		int relativeGridHeight = grid.getHeight() == 0 ? 0 : (grid.getHeight() - gridHeightRemainder) / subCellCountAcross;
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
		m_backBuffer.setCellSize(subCellCountAcross);
		m_backBuffer.imposeBuffer(grid, m_displayBuffer, alternativeCodeSource, options__extends__bhF_BufferUpdateOption);
		
		//s_logger.info(bhCellPool.getInstance().getAllocCount() + "");
		
		this.swapBuffers();
	}
	
	private void calcRawBufferDimensions(bhCamera camera, bhA_Grid grid, int subCellCountDim, bhGridCoordinate topLeft_out, bhGridCoordinate widthHeight_out)
	{
		if( subCellCountDim == 0 || grid.getWidth() == 0 || grid.getHeight() == 0 )
		{
			topLeft_out.set(0, 0);
			widthHeight_out.set(0,  0);
			
			return;
		}
		
		bhGridCoordinate coordOfCenterCell = topLeft_out;
		bhPoint worldPointOfCenterCell = m_utilPoint1;
		bhPoint screenPointOfCenterCell = m_utilPoint2;
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
	public boolean tryPopulatingCell(bhGridCoordinate coordinate, bhE_CodeType eType, bhA_Cell outCell)
	{
		bhCode toReturn = null;
		bhBufferCell thisCell = null;
		
		if( m_displayBuffer.isInBoundsAbsolute(coordinate) )
		{
			thisCell = m_displayBuffer.getCellAtAbsoluteCoord(coordinate);
			
			if( thisCell.getStatus(eType) == bhE_CodeStatus.HAS_CODE )
			{
				toReturn = thisCell.getCode(eType);
			}
			else
			{
				for( int i = 0; i < bhE_CodeType.values().length; i++ )
				{
					bhE_CodeType ithType = bhE_CodeType.values()[i];
					
					if( ithType == eType )
					{
						continue;
					}
					
					if( thisCell.getStatus(ithType) == bhE_CodeStatus.HAS_CODE )
					{
						bhCode potentialStandIn = thisCell.getCode(ithType);
						
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
							bhU_Debug.ASSERT(false, "Code shouldn't be null here.");
						}
					}
				}
				
				if( toReturn /*still*/ == null )
				{
					//--- DRK > TODO: A minor hack here doing an instanceof...will let it pass for now,
					//---			  but a better implementation is welcome.
					if( outCell instanceof bhBufferCell)
					{
						bhBufferCell outCellAsBufferCell = (bhBufferCell) outCell;
						
						if( thisCell.getStatus(eType) == bhE_CodeStatus.WAITING_ON_CODE )
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
						/*else if( thisCell.getStatus(eType) == bhE_CodeStatus.GET_ERROR )
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
			
			if( eType == bhE_CodeType.SOURCE || toReturn.getSafetyLevel() == bhE_CodeSafetyLevel.REQUIRES_DYNAMIC_SANDBOX )
			{
				outCell.getCodePrivileges().copy(thisCell.getCodePrivileges());
			}
			
			return true;
		}
		
		return false;
	}
}