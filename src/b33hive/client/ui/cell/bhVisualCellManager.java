package b33hive.client.ui.cell;

import java.util.ArrayList;
import java.util.logging.Logger;

import b33hive.client.app.bhS_Client;
import b33hive.client.app.bh_c;
import b33hive.client.entities.bhCamera;
import b33hive.client.entities.bhBufferCell;
import b33hive.client.managers.bhCellBuffer;
import b33hive.client.managers.bhCellBufferManager;
import b33hive.client.entities.bhI_BufferCellListener;
import b33hive.client.states.StateMachine_Base;
import b33hive.client.states.camera.StateMachine_Camera;
import b33hive.client.states.camera.State_ViewingCell;
import b33hive.client.structs.bhCellPool;
import b33hive.client.structs.bhI_CellPoolDelegate;
import b33hive.client.ui.bhI_UIElement;
import b33hive.client.ui.bhU_UI;
import b33hive.client.ui.cell.bhAlertManager.I_Delegate;
import b33hive.client.ui.dialog.bhDialog;
import b33hive.shared.app.bhS_App;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.entities.bhA_Grid;
import b33hive.shared.entities.bhU_Grid;
import b33hive.shared.memory.bhObjectPool;
import b33hive.shared.reflection.bhI_Class;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhStateEvent;
import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.structs.bhPoint;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Panel;

/**
 * ...
 * @author 
 */
public class bhVisualCellManager implements bhI_UIElement, bhI_CellPoolDelegate
{
	private static final double NO_SCALING = .99999;
	private static final Logger s_logger = Logger.getLogger(bhVisualCellManager.class.getName());
	
	private final bhI_Class<bhVisualCell> m_visualCellClass = new bhI_Class<bhVisualCell>()
	{
		@Override
		public bhVisualCell newInstance()
		{
			return new bhVisualCell();
		}
	};
	
	private Panel m_container = null;
	
	private final bhPoint m_utilPoint1 = new bhPoint();
	private final bhPoint m_utilPoint2 = new bhPoint();
	private final bhGridCoordinate m_utilCoord = new bhGridCoordinate();
	
	private final bhPoint m_lastBasePoint = new bhPoint();
	
	private final bhObjectPool<bhVisualCell> m_pool = new bhObjectPool<bhVisualCell>(m_visualCellClass);
	
	private final ArrayList<bhVisualCell> m_queuedRemovals = new ArrayList<bhVisualCell>();
	
	private boolean m_needsUpdateDueToResizingOfCameraOrGrid = false;
	
	private StateMachine_Camera m_cameraController = null;
	
	private double m_lastScaling = 0;
	
	private final bhDialog m_alertDialog;
	
	public bhVisualCellManager(Panel container) 
	{
		m_container = container;
		
		bhCellPool.getInstance().setDelegate(this);

		m_alertDialog = new bhDialog(256, 164, new bhDialog.I_Delegate()
		{
			@Override
			public void onOkPressed()
			{
				bhBufferCell bufferCell = getCurrentBufferCell();
				bhVisualCell visualCell = (bhVisualCell) bufferCell.getVisualization();
				visualCell.getBlocker().setContent(null);
				
				bhAlertManager.getInstance().onHandled();
			}
		});
		
		bhAlertManager.getInstance().setDelegate(new bhAlertManager.I_Delegate()
		{
			@Override
			public void showAlert(String message)
			{
				bhBufferCell bufferCell = getCurrentBufferCell();
				
				if( bufferCell != null )
				{
					bhCellAddress address = bufferCell.getCellAddress();
					String title = "Cell says...";
					
					if( message.length() > bhS_Client.MAX_ALERT_CHARACTERS )
					{
						message = message.substring(0, bhS_Client.MAX_ALERT_CHARACTERS);
						message += "...";
					}
					
					SafeHtml safeHtml = SafeHtmlUtils.fromString(message);
					m_alertDialog.setTitle(title);
					m_alertDialog.setBodySafeHtml(safeHtml);
					bhVisualCell visualCell = (bhVisualCell) bufferCell.getVisualization();
					visualCell.getBlocker().setContent(m_alertDialog);
				}
				else
				{
					bhU_Debug.ASSERT(false, "Expected current cell to be set.");
				}
			}
		});
	}
	
	private void processRemovals()
	{
		for( int i = m_queuedRemovals.size()-1; i >= 0; i-- )
		{
			bhVisualCell ithCell = m_queuedRemovals.get(i);
			
			if( ithCell.getParent() != null )
			{
				bhU_Debug.ASSERT(ithCell.getParent() == m_container, "processRemovals1");
				
				ithCell.removeFromParent();
			}
			ithCell.onDestroy();
			m_pool.deallocate(ithCell);
		}
		
		m_queuedRemovals.clear();
	}
	
	public bhI_BufferCellListener createVisualization(int width, int height, int padding, int subCellDim)
	{
		bhVisualCell newVisualCell = m_pool.allocate();
		
		//s_logger.severe("Creating: " + newVisualCell.getId() + " at " + bhCellBufferManager.getInstance().getUpdateCount());
		
		if( newVisualCell.getParent() != null )
		{
			bhU_Debug.ASSERT(false, "createVisualization1");
		}
		
		newVisualCell.onCreate(width, height, padding, subCellDim);
		
		return newVisualCell;
	}
	
	public void destroyVisualization(bhI_BufferCellListener visualization)
	{
		bhVisualCell visualCell = (bhVisualCell) visualization;
		
		//s_logger.severe("Destroying: " + visualCell.getId() + " at " + bhCellBufferManager.getInstance().getUpdateCount());
		
		if( visualCell.getParent() != m_container )
		{
			//--- DRK > Fringe case can hit this assert, so it's removed...
			//---		You flick the camera so it comes to rest right at the edge of a new cell, creating the visualization.
			//---		Then inside the same update loop, you do a mouse press which, probably due to numerical error, 
			//---		takes the new cell out of view. Currently, in the state machine implementation, that mouse press
			//---		forces a refresh of the cell buffer in the same update loop, right after the refresh that caused the 
			//---		visualization to get created. Because this manager lazily adds/removes cells, the cell didn't have a
			//---		chance to get a parent.
			//bhU_Debug.ASSERT(false, "destroyVisualization1....bad parent: " + visualCell.getParent());
		}
		
		m_queuedRemovals.add(visualCell);
	}
	
	private boolean updateCellTransforms(double timeStep)
	{
		if( m_cameraController == null )
		{
			bhU_Debug.ASSERT(false);
			
			return false;
		}
		
		if( m_cameraController.getCameraManager().isCameraAtRest() )
		{
			if( !m_needsUpdateDueToResizingOfCameraOrGrid )
			{
				return false;
			}
		}

		bhCellBufferManager cellManager = bhCellBufferManager.getInstance();
		bhCellBuffer cellBuffer = cellManager.getDisplayBuffer();
		
		bhA_Grid grid = bh_c.gridMngr.getGrid(); // TODO: Get grid from somewhere else.
		
		int bufferSize = cellBuffer.getCellCount();
		int bufferWidth = cellBuffer.getWidth();
		int bufferHeight = cellBuffer.getHeight();
		
		bhCamera camera = bh_c.camera;
		
		double distanceRatio = camera.calcDistanceRatio();
		
		int subCellCount = cellBuffer.getSubCellCount();
		
		bhPoint basePoint = m_utilPoint1;
		cellBuffer.getCoordinate().calcPoint(basePoint, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
		camera.calcScreenPoint(basePoint, m_utilPoint2);
		basePoint = m_utilPoint2;
		basePoint.round();
		
		m_lastBasePoint.copy(basePoint);
		
		double scaling = bhU_Grid.calcCellScaling(distanceRatio, subCellCount, grid.getCellPadding(), grid.getCellWidth());
		/*double factor = 1e5; // = 1 * 10^5 = 100000.
		scaling = Math.round(scaling * factor) / factor;*/
		
		double cellWidthPlusPadding = -1;
		double cellHeightPlusPadding = -1;
		if( subCellCount == 1 )
		{
			cellWidthPlusPadding = (grid.getCellWidth() + grid.getCellPadding()) * scaling;
			cellHeightPlusPadding = (grid.getCellHeight() + grid.getCellPadding()) * scaling;
		}
		else
		{
			//--- DRK > We have to fudge the scaling here so that thick artifact lines don't appear between the meta-cell images at some zoom levels.
			//---		Basically just rounding things to the nearest pixel so that the browser renderer doesn't just decide on its own how it should look at sub-pixels.
			cellWidthPlusPadding = (grid.getCellWidth()) * scaling;
			cellWidthPlusPadding = Math.round(cellWidthPlusPadding);
			scaling = cellWidthPlusPadding / grid.getCellWidth();
			
			cellHeightPlusPadding = grid.getCellHeight() * scaling;
		}
	
		String scaleProperty = scaling < NO_SCALING ? bhU_UI.createScaleTransform(scaling) : null;
		
		
		
		//--- DRK > NOTE: ALL DOM-manipulation related to cells should occur within this block.
		//---		This is why removal of visual cells is queued until this point.  This might be an
		//---		extraneous optimization in some browsers if they themselves have intelligent DOM-change batching,
		//---		but we must assume that most browsers are retarded.
		//--	NOTE: Well, not ALL manipulation is in here, there are a few odds and ends done outside this...but most should be here.
		m_container.getElement().getStyle().setDisplay(Display.NONE);
		{
			processRemovals();
			
			//--- DRK > Serious malfunction here if hit.
			//--- NOTE: Now cell buffer can have null cells (i.e. if they aren't owned).
			//---		So this assert is now invalid...keeping for historical reference.
			//bhU_Debug.ASSERT(cellBuffer.getCellCount() == m_pool.getAllocCount(), "bhVisualCellManager::update1");
			
			for ( int i = 0; i < bufferSize; i++ )
			{
				int ix = i % bufferWidth;
				int iy = i / bufferWidth;
				
				bhBufferCell ithBufferCell = cellBuffer.getCellAtIndex(i);
				
				if( ithBufferCell == null )  continue;
				
				double offsetX = (ix * (cellWidthPlusPadding));
				double offsetY = (iy * (cellHeightPlusPadding));
				
				bhVisualCell ithVisualCell = (bhVisualCell) ithBufferCell.getVisualization();
				
				ithVisualCell.validate();
				
				double translateX = basePoint.getX() + offsetX;
				double translateY = basePoint.getY() + offsetY;
				String translateProperty = bhU_UI.createTranslateTransform(translateX, translateY);
				String transform = scaleProperty != null ? translateProperty + " " + scaleProperty : translateProperty;
				bhU_UI.setTransform(ithVisualCell.getElement(), transform);
				
				ithVisualCell.update(timeStep);
				
				if( ithVisualCell.getParent() == null )
				{
					m_container.add(ithVisualCell);
				}
			}
		}
		m_container.getElement().getStyle().setDisplay(Display.BLOCK);
		
		m_lastScaling = scaling;
		
		m_needsUpdateDueToResizingOfCameraOrGrid = false;
		
		return true;
	}
	
	private void updateCellsIndividually(double timeStep)
	{
		bhCellBufferManager cellManager = bhCellBufferManager.getInstance();
		bhCellBuffer cellBuffer = cellManager.getDisplayBuffer();
		int bufferSize = cellBuffer.getCellCount();
		
		for ( int i = 0; i < bufferSize; i++ )
		{
			bhBufferCell ithBufferCell = cellBuffer.getCellAtIndex(i);
			
			if( ithBufferCell == null ) continue;
			
			bhVisualCell ithVisualCell = (bhVisualCell) ithBufferCell.getVisualization();
			ithVisualCell.update(timeStep);
		}
	}
	
	public double getLastScaling()
	{
		return m_lastScaling;
	}
	
	public bhPoint getLastBasePoint()
	{
		return m_lastBasePoint;
	}
	
	@Override
	public void onStateEvent(bhStateEvent event)
	{
		switch(event.getType())
		{
			case DID_UPDATE:
			{
				if( event.getState().getParent() instanceof StateMachine_Camera )
				{
					if( !this.updateCellTransforms(event.getState().getLastTimeStep()) )
					{
						this.updateCellsIndividually(event.getState().getLastTimeStep());
					}
				}
				
				break;
			}
			
			case DID_ENTER:
			{
				if( event.getState() instanceof StateMachine_Camera )
				{
					m_cameraController = (StateMachine_Camera) event.getState();
				}
				
				break;
			}
			
			case DID_EXIT:
			{
				if( event.getState() instanceof StateMachine_Camera )
				{
					m_cameraController = null;
				}
				else if( event.getState() instanceof State_ViewingCell )
				{
					clearAlerts();
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == StateMachine_Camera.SetCameraViewSize.class ||
					event.getAction() == StateMachine_Base.OnGridResize.class )
				{
					m_needsUpdateDueToResizingOfCameraOrGrid = true;
					
					//--- DRK > In a strict sense this is a waste of an update since in just a few milliseconds
					//---		we'll be doing another one anyway. If we don't do it here though, there's some 
					//---		ghosting of the cell visualizations with certain rapid resizes of the UI console.
					//---		Also, if the camera is still and the grid resizes, visual cells won't appear until
					//---		we move.
					this.updateCellTransforms(0);
				}
				else if( event.getAction() == State_ViewingCell.Refresh.class )
				{
					//--- DRK > Used to clear alerts here, but moved it to the actual refresh button handler.
					//--- 		Probably safe here, but refresh might instantly update the cell's code, before
					//---		we get a chance to remove the alerts...might be order-dependent strangeness there.
					//clearAlerts();
				}
				
				break;
			}
		}
	}
	
	private bhBufferCell getCurrentBufferCell()
	{
		State_ViewingCell state = bhA_State.getEnteredInstance(State_ViewingCell.class);
		if( state != null )
		{
			return state.getCell();
		}
		
		return null;
	}
	
	public void clearAlerts()
	{
		bhAlertManager.getInstance().clear();
		
		bhBufferCell bufferCell = getCurrentBufferCell();
		
		if( bufferCell != null )
		{
			bhVisualCell cell = (bhVisualCell) bufferCell.getVisualization();
			cell.getBlocker().setContent(null);
		}
		else
		{
			//--- DRK > Below assert trips badly when exiting view state...state is already
			//---		exited when we get to here.
			//bhU_Debug.ASSERT(false, "Expected current cell to be set.");
		}
	}
}