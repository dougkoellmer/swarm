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
import swarm.client.states.camera.Action_Camera_SetViewSize;
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
public class smVisualCellManager implements smI_UIElement
{
	private static final double NO_SCALING = .99999;
	private static final Logger s_logger = Logger.getLogger(smVisualCellManager.class.getName());
	
	private Panel m_container = null;
	
	private final smPoint m_utilPoint1 = new smPoint();
	private final smPoint m_utilPoint2 = new smPoint();
	
	private final smPoint m_lastBasePoint = new smPoint();
	
	private boolean m_needsUpdateDueToResizingOfCameraOrGrid = false;
	
	private StateMachine_Camera m_cameraController = null;
	
	private double m_lastScaling = 0;
	
	private final smDialog m_alertDialog;
	
	private final smViewContext m_viewContext;
	
	private final smVisualCellPool m_cellPool;
	
	private final int m_cellDestroyLimitWhileMoving = 1;
	
	public smVisualCellManager(smViewContext viewContext, Panel container) 
	{
		m_container = container;
		m_viewContext = viewContext;
		m_cellPool = new smVisualCellPool(viewContext.appContext.cellSandbox, m_container);
		
		m_viewContext.appContext.cellBufferMngr.getCellPool().setDelegate(m_cellPool);

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
	
	private boolean updateCellTransforms(double timeStep, boolean flushDestroyQueueIfMoving)
	{
		if( m_cameraController == null )
		{
			smU_Debug.ASSERT(false);
			
			return false;
		}
		
		if( m_viewContext.appContext.cameraMngr.isCameraAtRest() )
		{
			m_cellPool.cleanPool();
			
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
		int scrollX = m_container.getParent().getElement().getScrollLeft();
		int scrollY = m_container.getParent().getElement().getScrollTop();
		
		
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
			
			if( flushDestroyQueueIfMoving )
			{
				//m_cellPool.flushDestroyQueue(m_cellDestroyLimitWhileMoving);
			}
			
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
				
				
				double translateX = basePoint.getX() + offsetX + scrollX;
				double translateY = basePoint.getY() + offsetY + scrollY;
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
					boolean flushDestroyQueueIfMoving = (event.getState().getUpdateCount() % 8) == 0;
					
					if( !this.updateCellTransforms(event.getState().getLastTimeStep(), flushDestroyQueueIfMoving) )
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
				if( event.getAction() == Action_Camera_SetViewSize.class ||
					event.getAction() == StateMachine_Base.OnGridUpdate.class )
				{
					m_needsUpdateDueToResizingOfCameraOrGrid = true;
					
					//--- DRK > In a strict sense this is a waste of an update since in just a few milliseconds
					//---		we'll be doing another one anyway. If we don't do it here though, there's some 
					//---		ghosting of the cell visualizations with certain rapid resizes of the UI console.
					//---		Also, if the camera is still and the grid resizes, visual cells won't appear until
					//---		we move.
					boolean flushDestroyQueueIfMoving = true;
					this.updateCellTransforms(0, flushDestroyQueueIfMoving);
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