package swarm.client.view.cell;

import java.util.ArrayList;
import java.util.logging.Logger;

import swarm.client.app.smS_Client;
import swarm.client.app.smAppContext;
import swarm.client.entities.smCamera;
import swarm.client.entities.smBufferCell;
import swarm.client.managers.smCellBuffer;
import swarm.client.managers.smCellBufferManager;
import swarm.client.entities.smI_BufferCellListener;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.camera.Action_Camera_SetCameraViewSize;
import swarm.client.states.camera.Action_ViewingCell_Refresh;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.structs.smCellPool;
import swarm.client.structs.smI_CellPoolDelegate;
import swarm.client.view.smI_UIElement;
import swarm.client.view.smU_Css;
import swarm.client.view.smViewContext;
import swarm.client.view.cell.smAlertManager.I_Delegate;
import swarm.client.view.dialog.smDialog;
import swarm.shared.app.smS_App;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smA_Grid;
import swarm.shared.entities.smU_Grid;
import swarm.shared.memory.smObjectPool;
import swarm.shared.reflection.smI_Class;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smStateEvent;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Panel;

/**
 * ...
 * @author 
 */
public class smVisualCellManager implements smI_UIElement, smI_CellPoolDelegate
{
	private static final double NO_SCALING = .99999;
	private static final Logger s_logger = Logger.getLogger(smVisualCellManager.class.getName());
	
	private final smI_Class<smVisualCell> m_visualCellClass = new smI_Class<smVisualCell>()
	{
		@Override
		public smVisualCell newInstance()
		{
			return new smVisualCell(smVisualCellManager.this.m_viewContext.appContext.cellSandbox);
		}
	};
	
	private Panel m_container = null;
	
	private final smPoint m_utilPoint1 = new smPoint();
	private final smPoint m_utilPoint2 = new smPoint();
	private final smGridCoordinate m_utilCoord = new smGridCoordinate();
	
	private final smPoint m_lastBasePoint = new smPoint();
	
	private final smObjectPool<smVisualCell> m_pool = new smObjectPool<smVisualCell>(m_visualCellClass);
	
	private final ArrayList<smVisualCell> m_queuedRemovals = new ArrayList<smVisualCell>();
	
	private boolean m_needsUpdateDueToResizingOfCameraOrGrid = false;
	
	private StateMachine_Camera m_cameraController = null;
	
	private double m_lastScaling = 0;
	
	private final smDialog m_alertDialog;
	
	private final smViewContext m_viewContext;
	
	public smVisualCellManager(smViewContext viewContext, Panel container) 
	{
		m_container = container;
		m_viewContext = viewContext;
		
		m_viewContext.appContext.cellBufferMngr.getCellPool().setDelegate(this);

		m_alertDialog = new smDialog(m_viewContext.clickMngr, 256, 164, new smDialog.I_Delegate()
		{
			@Override
			public void onOkPressed()
			{
				smBufferCell bufferCell = getCurrentBufferCell();
				smVisualCell visualCell = (smVisualCell) bufferCell.getVisualization();
				visualCell.getBlocker().setContent(null);
				
				smVisualCellManager.this.m_viewContext.alertMngr.onHandled();
			}
		});
		
		m_viewContext.alertMngr.setDelegate(new smAlertManager.I_Delegate()
		{
			@Override
			public void showAlert(String message)
			{
				smBufferCell bufferCell = getCurrentBufferCell();
				
				if( bufferCell != null )
				{
					smCellAddress address = bufferCell.getCellAddress();
					String title = "Cell says...";
					
					if( message.length() > smS_Client.MAX_ALERT_CHARACTERS )
					{
						message = message.substring(0, smS_Client.MAX_ALERT_CHARACTERS);
						message += "...";
					}
					
					SafeHtml safeHtml = SafeHtmlUtils.fromString(message);
					m_alertDialog.setTitle(title);
					m_alertDialog.setBodySafeHtml(safeHtml);
					smVisualCell visualCell = (smVisualCell) bufferCell.getVisualization();
					visualCell.getBlocker().setContent(m_alertDialog);
				}
				else
				{
					smU_Debug.ASSERT(false, "Expected current cell to be set.");
				}
			}
		});
	}
	
	private void processRemovals()
	{
		for( int i = m_queuedRemovals.size()-1; i >= 0; i-- )
		{
			smVisualCell ithCell = m_queuedRemovals.get(i);
			
			if( ithCell.getParent() != null )
			{
				smU_Debug.ASSERT(ithCell.getParent() == m_container, "processRemovals1");
				
				ithCell.removeFromParent();
			}
			ithCell.onDestroy();
			m_pool.deallocate(ithCell);
		}
		
		m_queuedRemovals.clear();
	}
	
	public smI_BufferCellListener createVisualization(int width, int height, int padding, int subCellDim)
	{
		smVisualCell newVisualCell = m_pool.allocate();
		newVisualCell.setVisible(true);
		
		//s_logger.severe("Creating: " + newVisualCell.getId() + " at " + smCellBufferManager.getInstance().getUpdateCount());
		
		if( newVisualCell.getParent() != null )
		{
			smU_Debug.ASSERT(false, "createVisualization1");
		}
		
		newVisualCell.onCreate(width, height, padding, subCellDim);
		
		return newVisualCell;
	}
	
	public void destroyVisualization(smI_BufferCellListener visualization)
	{
		smVisualCell visualCell = (smVisualCell) visualization;
		
		//s_logger.severe("Destroying: " + visualCell.getId() + " at " + smCellBufferManager.getInstance().getUpdateCount());
		
		if( visualCell.getParent() != m_container )
		{
			//--- DRK > Fringe case can hit this assert, so it's removed...
			//---		You flick the camera so it comes to rest right at the edge of a new cell, creating the visualization.
			//---		Then inside the same update loop, you do a mouse press which, probably due to numerical error, 
			//---		takes the new cell out of view. Currently, in the state machine implementation, that mouse press
			//---		forces a refresh of the cell buffer in the same update loop, right after the refresh that caused the 
			//---		visualization to get created. Because this manager lazily adds/removes cells, the cell didn't have a
			//---		chance to get a parent.
			//smU_Debug.ASSERT(false, "destroyVisualization1....bad parent: " + visualCell.getParent());
		}
		
		visualCell.setVisible(false);
		m_queuedRemovals.add(visualCell);
	}
	
	private boolean updateCellTransforms(double timeStep)
	{
		if( m_cameraController == null )
		{
			smU_Debug.ASSERT(false);
			
			return false;
		}
		
		if( m_viewContext.appContext.cameraMngr.isCameraAtRest() )
		{
			processRemovals();
			
			if( !m_needsUpdateDueToResizingOfCameraOrGrid )
			{
				return false;
			}
		}

		smCellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
		smCellBuffer cellBuffer = cellManager.getDisplayBuffer();
		
		smA_Grid grid = m_viewContext.appContext.gridMngr.getGrid(); // TODO: Get grid from somewhere else.
		
		int bufferSize = cellBuffer.getCellCount();
		int bufferWidth = cellBuffer.getWidth();
		int bufferHeight = cellBuffer.getHeight();
		
		smCamera camera = m_viewContext.appContext.cameraMngr.getCamera();
		
		double distanceRatio = camera.calcDistanceRatio();
		
		int subCellCount = cellBuffer.getSubCellCount();
		
		smPoint basePoint = m_utilPoint1;
		cellBuffer.getCoordinate().calcPoint(basePoint, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
		camera.calcScreenPoint(basePoint, m_utilPoint2);
		basePoint = m_utilPoint2;
		basePoint.round();
		
		//s_logger.severe("" + basePoint + " " + camera.getPosition() + " " + camera.getViewWidth() + " " + camera.getViewHeight());
		
		m_lastBasePoint.copy(basePoint);
		
		double scaling = smU_Grid.calcCellScaling(distanceRatio, subCellCount, grid.getCellPadding(), grid.getCellWidth());
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
		
		boolean use3dTransforms = m_viewContext.appContext.platformInfo.has3dTransforms();
	
		String scaleProperty = scaling < NO_SCALING ? smU_Css.createScaleTransform(scaling, use3dTransforms) : null;
		
		
		
		//--- DRK > NOTE: ALL DOM-manipulation related to cells should occur within this block.
		//---		This might be an extraneous optimization in some browsers if they themselves have
		//---		intelligent DOM-change batching, but we must assume that most browsers are retarded.
		//--	NOTE: Well, not ALL manipulation is in here, there are a few odds and ends done outside this...but most should be here.
		m_container.getElement().getStyle().setDisplay(Display.NONE);
		{
			//--- DRK > Serious malfunction here if hit.
			//--- NOTE: Now cell buffer can have null cells (i.e. if they aren't owned).
			//---		So this assert is now invalid...keeping for historical reference.
			//smU_Debug.ASSERT(cellBuffer.getCellCount() == m_pool.getAllocCount(), "smVisualCellManager::update1");
			
			//processRemovals();
			
			for ( int i = 0; i < bufferSize; i++ )
			{
				smBufferCell ithBufferCell = cellBuffer.getCellAtIndex(i);
				
				if( ithBufferCell == null )  continue;
				
				int ix = i % bufferWidth;
				int iy = i / bufferWidth;
				
				double offsetX = (ix * (cellWidthPlusPadding));
				double offsetY = (iy * (cellHeightPlusPadding));
				
				smVisualCell ithVisualCell = (smVisualCell) ithBufferCell.getVisualization();
				
				ithVisualCell.validate();
				
				double translateX = basePoint.getX() + offsetX;
				double translateY = basePoint.getY() + offsetY;
				String translateProperty = smU_Css.createTranslateTransform(translateX, translateY, use3dTransforms);
				String transform = scaleProperty != null ? translateProperty + " " + scaleProperty : translateProperty;
				String transformProperty = m_viewContext.appContext.platformInfo.getTransformProperty();
				ithVisualCell.getElement().getStyle().setProperty(transformProperty, transform);
				
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
		smCellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
		smCellBuffer cellBuffer = cellManager.getDisplayBuffer();
		int bufferSize = cellBuffer.getCellCount();
		
		for ( int i = 0; i < bufferSize; i++ )
		{
			smBufferCell ithBufferCell = cellBuffer.getCellAtIndex(i);
			
			if( ithBufferCell == null ) continue;
			
			smVisualCell ithVisualCell = (smVisualCell) ithBufferCell.getVisualization();
			ithVisualCell.update(timeStep);
		}
	}
	
	public double getLastScaling()
	{
		return m_lastScaling;
	}
	
	public smPoint getLastBasePoint()
	{
		return m_lastBasePoint;
	}
	
	@Override
	public void onStateEvent(smStateEvent event)
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
				if( event.getAction() == Action_Camera_SetCameraViewSize.class ||
					event.getAction() == StateMachine_Base.OnGridUpdate.class )
				{
					m_needsUpdateDueToResizingOfCameraOrGrid = true;
					
					//--- DRK > In a strict sense this is a waste of an update since in just a few milliseconds
					//---		we'll be doing another one anyway. If we don't do it here though, there's some 
					//---		ghosting of the cell visualizations with certain rapid resizes of the UI console.
					//---		Also, if the camera is still and the grid resizes, visual cells won't appear until
					//---		we move.
					this.updateCellTransforms(0);
				}
				else if( event.getAction() == Action_ViewingCell_Refresh.class )
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
	
	private smBufferCell getCurrentBufferCell()
	{
		State_ViewingCell state = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
		if( state != null )
		{
			return state.getCell();
		}
		
		return null;
	}
	
	public void clearAlerts()
	{
		m_viewContext.alertMngr.clear();
		
		smBufferCell bufferCell = getCurrentBufferCell();
		
		if( bufferCell != null )
		{
			smVisualCell cell = (smVisualCell) bufferCell.getVisualization();
			cell.getBlocker().setContent(null);
		}
		else
		{
			//--- DRK > Below assert trips badly when exiting view state...state is already
			//---		exited when we get to here.
			//smU_Debug.ASSERT(false, "Expected current cell to be set.");
		}
	}
}