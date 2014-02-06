package swarm.client.view.cell;

import java.util.ArrayList;
import java.util.logging.Logger;

import swarm.client.app.S_Client;
import swarm.client.app.AppContext;
import swarm.client.entities.Camera;
import swarm.client.entities.BufferCell;
import swarm.client.managers.CellBuffer;
import swarm.client.managers.CellBufferManager;
import swarm.client.entities.I_BufferCellListener;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.camera.Action_Camera_SetViewSize;
import swarm.client.states.camera.Action_Camera_SnapToPoint;
import swarm.client.states.camera.Action_ViewingCell_Refresh;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.structs.CellPool;
import swarm.client.structs.I_CellPoolDelegate;
import swarm.client.view.I_UIElement;
import swarm.client.view.U_Css;
import swarm.client.view.ViewContext;
import swarm.client.view.cell.AlertManager.I_Delegate;
import swarm.client.view.dialog.Dialog;
import swarm.shared.app.S_CommonApp;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Grid;
import swarm.shared.entities.U_Grid;
import swarm.shared.memory.ObjectPool;
import swarm.shared.reflection.I_Class;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.StateEvent;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Panel;

/**
 * ...
 * @author 
 */
public class VisualCellManager implements I_UIElement
{
	private static final double NO_SCALING = .99999;
	private static final Logger s_logger = Logger.getLogger(VisualCellManager.class.getName());
	
	private Panel m_container = null;
	
	private final Point m_utilPoint1 = new Point();
	private final Point m_utilPoint2 = new Point();
	
	private final Point m_lastBasePoint = new Point();
	
	private StateMachine_Camera m_cameraController = null;
	
	private double m_lastScaling = 0;
	
	private final Dialog m_alertDialog;
	
	private final ViewContext m_viewContext;
	
	private final VisualCellPool m_cellPool;
	
	private final int m_cellDestroyLimitWhileMoving = 1;
	
	public VisualCellManager(ViewContext viewContext, Panel container) 
	{
		m_container = container;
		m_viewContext = viewContext;
		m_cellPool = new VisualCellPool(viewContext.appContext.cellSandbox, m_container, m_viewContext.spinnerFactory);
		
		m_viewContext.appContext.cellBufferMngr.getCellPool().setDelegate(m_cellPool);

		m_alertDialog = new Dialog(m_viewContext.clickMngr, 256, 164, new Dialog.I_Delegate()
		{
			@Override
			public void onOkPressed()
			{
				BufferCell bufferCell = getCurrentBufferCell();
				VisualCell visualCell = (VisualCell) bufferCell.getVisualization();
				visualCell.getBlocker().setContent(null);
				
				VisualCellManager.this.m_viewContext.alertMngr.onHandled();
			}
		});
		
		m_viewContext.alertMngr.setDelegate(new AlertManager.I_Delegate()
		{
			@Override
			public void showAlert(String message)
			{
				BufferCell bufferCell = getCurrentBufferCell();
				
				if( bufferCell != null )
				{
					CellAddress address = bufferCell.getAddress();
					String title = "Cell says...";
					
					if( message.length() > S_Client.MAX_ALERT_CHARACTERS )
					{
						message = message.substring(0, S_Client.MAX_ALERT_CHARACTERS);
						message += "...";
					}
					
					SafeHtml safeHtml = SafeHtmlUtils.fromString(message);
					m_alertDialog.setTitle(title);
					m_alertDialog.setBodySafeHtml(safeHtml);
					VisualCell visualCell = (VisualCell) bufferCell.getVisualization();
					visualCell.getBlocker().setContent(m_alertDialog);
				}
				else
				{
					U_Debug.ASSERT(false, "Expected current cell to be set.");
				}
			}
		});
	}
	
	private boolean updateCellTransforms(double timeStep)
	{
		CellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
		CellBuffer cellBuffer = cellManager.getDisplayBuffer();
		
		A_Grid grid = m_viewContext.appContext.gridMngr.getGrid(); // TODO: Get grid from somewhere else.
		
		int bufferSize = cellBuffer.getCellCount();
		int bufferWidth = cellBuffer.getWidth();
		
		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
		
		double distanceRatio = camera.calcDistanceRatio();
		
		int subCellCount = cellBuffer.getSubCellCount();
		
		Point basePoint = m_utilPoint1;
		cellBuffer.getCoordinate().calcPoint(basePoint, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
		camera.calcScreenPoint(basePoint, m_utilPoint2);
		basePoint = m_utilPoint2;
		basePoint.round();
		
		//s_logger.severe("" + basePoint + " " + camera.getPosition() + " " + camera.getViewWidth() + " " + camera.getViewHeight());
		
		m_lastBasePoint.copy(basePoint);
		
		double scaling = U_Grid.calcCellScaling(distanceRatio, subCellCount, grid.getCellPadding(), grid.getCellWidth());
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
	
		String scaleProperty = scaling < NO_SCALING ? U_Css.createScaleTransform(scaling, use3dTransforms) : null;
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
			
			for ( int i = 0; i < bufferSize; i++ )
			{
				BufferCell ithBufferCell = cellBuffer.getCellAtIndex(i);
				
				if( ithBufferCell == null )  continue;
				
				int ix = i % bufferWidth;
				int iy = i / bufferWidth;
				
				double offsetX = (ix * (cellWidthPlusPadding));
				double offsetY = (iy * (cellHeightPlusPadding));
				
				VisualCell ithVisualCell = (VisualCell) ithBufferCell.getVisualization();
				
				ithVisualCell.validate();
				
				
				double translateX = basePoint.getX() + offsetX + scrollX;
				double translateY = basePoint.getY() + offsetY + scrollY;
				String translateProperty = U_Css.createTranslateTransform(translateX, translateY, use3dTransforms);
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
		
		return true;
	}
	
	private void updateCellsIndividually(double timeStep)
	{
		CellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
		CellBuffer cellBuffer = cellManager.getDisplayBuffer();
		int bufferSize = cellBuffer.getCellCount();
		
		for ( int i = 0; i < bufferSize; i++ )
		{
			BufferCell ithBufferCell = cellBuffer.getCellAtIndex(i);
			
			if( ithBufferCell == null ) continue;
			
			VisualCell ithVisualCell = (VisualCell) ithBufferCell.getVisualization();
			ithVisualCell.update(timeStep);
		}
	}
	
	public double getLastScaling()
	{
		return m_lastScaling;
	}
	
	public Point getLastBasePoint()
	{
		return m_lastBasePoint;
	}
	
	@Override
	public void onStateEvent(StateEvent event)
	{
		switch( event.getType() )
		{
			case DID_UPDATE:
			{
				if( event.getState().getParent() instanceof StateMachine_Camera )
				{
					//boolean flushDestroyQueueIfMoving = (event.getState().getUpdateCount() % 8) == 0;
					
					if( m_viewContext.appContext.cameraMngr.getAtRestFrameCount() >=2 )
					{
						m_cellPool.cleanPool();
						
						this.updateCellsIndividually(event.getState().getLastTimeStep());
					}
					else
					{
						this.updateCellTransforms(event.getState().getLastTimeStep());
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
				if( event.getAction() == Action_Camera_SetViewSize.class )
				{
					Action_Camera_SetViewSize.Args args = event.getActionArgs();
					
					if( args.updateBuffer() )
					{
						this.updateCellTransforms(0.0);
					}
				}
				else if( event.getAction() == StateMachine_Base.OnGridUpdate.class )
				{					
					//--- DRK > In some sense this is a waste of an update since in just a few milliseconds
					//---		we'll be doing another one anyway. If we don't do it here though, there's some 
					//---		lagging of the cell visualizations with very rapid resizes of the window.
					//---		Also, if the camera is still and the itself gets grid updated, visual cells won't appear until
					//---		we move the camera again.
					this.updateCellTransforms(0.0);
				}
				else if( event.getAction() == Action_Camera_SnapToPoint.class )
				{
					Action_Camera_SnapToPoint.Args args = event.getActionArgs();
					
					//--- DRK(TODO): Don't really like this null check here...necessary because of legacy
					//---			 code using null snap args to simply change to floating state.
					if( args == null || args.isInstant() )
					{
						this.updateCellTransforms(0.0);
					}
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
	
	private BufferCell getCurrentBufferCell()
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
		
		BufferCell bufferCell = getCurrentBufferCell();
		
		if( bufferCell != null )
		{
			VisualCell cell = (VisualCell) bufferCell.getVisualization();
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