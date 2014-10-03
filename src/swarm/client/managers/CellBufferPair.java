package swarm.client.managers;

import java.util.logging.Logger;

import swarm.client.entities.Camera;
import swarm.client.entities.ClientGrid;
import swarm.client.structs.BufferCellPool;
import swarm.client.structs.I_LocalCodeRepository;
import swarm.shared.entities.A_Grid;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;

public class CellBufferPair
{
	private static final Logger s_logger = Logger.getLogger(CellBufferPair.class.getName());
	
	private final GridCoordinate m_utilCoord1 = new GridCoordinate();
	private final GridCoordinate m_utilCoord2 = new GridCoordinate();
	
	private final Point m_utilPoint1 = new Point();
	private final Point m_utilPoint2 = new Point();
	
	private CellBuffer m_displayBuffer;
	private CellBuffer m_backBuffer;
	
	private final CellKillQueue m_killQueue;
	
	private final int m_subCellCount;
	
	CellBufferPair(CellBufferManager parent, CellCodeManager codeMngr, CellSizeManager cellSizeMngr, BufferCellPool pool, int subCellCount)
	{
		m_killQueue = new CellKillQueue(pool);
		m_displayBuffer = new CellBuffer(parent, codeMngr, pool, cellSizeMngr, subCellCount, m_killQueue);
		m_backBuffer = new CellBuffer(parent, codeMngr, pool, cellSizeMngr, subCellCount, m_killQueue);
		
		m_subCellCount = subCellCount;
	}
	
	void update_cameraStill()
	{
		m_killQueue.update();
	}
	
	void update_cameraMoving(ClientGrid grid, Camera camera, I_LocalCodeRepository alternativeCodeSource, int options__extends__smF_BufferUpdateOption, int subCellCount)
	{
		m_killQueue.update();
		
		//--- DRK > Calculate maximum "raw" buffer position and size based on camera viewport, not caring about grid constraints.
		this.calcRawBufferDimensions(camera, grid, m_subCellCount, m_utilCoord1, m_utilCoord2);
		int newBufferWidth = m_utilCoord2.getM();
		int newBufferHeight = m_utilCoord2.getN();
		
//		s_logger.severe("Raw: " + m_subCellCount + " " + m_utilCoord1.toString() + " " + newBufferWidth + " " + newBufferHeight);		
		
		//--- DRK > Constrain both the position and size of the buffer if necessary so it maps onto the grid in a minimal fashion.
		//---		The following is capable of creating a buffer of zero size, which is perfectly acceptable.
		int gridWidthRemainder = m_subCellCount > 0 ? grid.getWidth() % m_subCellCount : 0;
		int relativeGridWidth = grid.getWidth() == 0 ? 0 : (grid.getWidth() - gridWidthRemainder) / m_subCellCount;
		relativeGridWidth += gridWidthRemainder > 0 ? 1 : 0;
		int gridHeightRemainder = m_subCellCount > 0 ? grid.getHeight() % m_subCellCount : 0;
		int relativeGridHeight = grid.getHeight() == 0 ? 0 : (grid.getHeight() - gridHeightRemainder) / m_subCellCount;
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
		
//		s_logger.severe("Constrained: " + m_utilCoord1.toString() + " " + newBufferWidth + " " + newBufferHeight);
//		s_logger.severe("");
		
		m_backBuffer.setExtents(m_utilCoord1.getM(), m_utilCoord1.getN(), newBufferWidth, newBufferHeight);
		m_backBuffer.imposeBuffer(grid, m_displayBuffer, alternativeCodeSource, subCellCount, options__extends__smF_BufferUpdateOption);
		
		//s_logger.info(smCellPool.getInstance().getAllocCount() + "");
		
		this.swapBuffers();
	}
	
	private void calcRawBufferDimensions(Camera camera, A_Grid grid, int subCellCountDim, GridCoordinate topLeft_out, GridCoordinate widthHeight_out)
	{
		if( subCellCountDim == 0 || grid.getWidth() == 0 || grid.getHeight() == 0 )
		{
			topLeft_out.set(0, 0);
			widthHeight_out.set(0,  0);
			
			return;
		}
		
		GridCoordinate coordOfCenterCell = topLeft_out;
		Point worldPointOfCenterCell = m_utilPoint1;
		Point screenPointOfCenterCell = m_utilPoint2;
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
	
	public CellBuffer getDisplayBuffer()
	{
		return m_displayBuffer;
	}
	
	void swapBuffers()
	{
		CellBuffer temp = m_displayBuffer;
		m_displayBuffer = m_backBuffer;
		m_backBuffer = temp;
	}
	
	void drain()
	{
		m_displayBuffer.drain();
		m_backBuffer.drain();
		
		//--- DRK > Don't think this is really ever necessary as of this
		//---		iteration of swarm, but here's some future proofing for ya.
		m_killQueue.drain();
	}
}
