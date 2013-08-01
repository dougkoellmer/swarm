package com.b33hive.client.ui.cell;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.b33hive.client.app.bhS_ClientApp;
import com.b33hive.client.entities.bhCamera;
import com.b33hive.client.entities.bhBufferCell;
import com.b33hive.client.managers.bhCellBuffer;
import com.b33hive.client.managers.bhCellBufferManager;
import com.b33hive.client.entities.bhClientGrid;
import com.b33hive.client.entities.bhI_BufferCellListener;
import com.b33hive.client.states.StateMachine_Base;
import com.b33hive.client.states.camera.StateMachine_Camera;
import com.b33hive.client.states.camera.State_ViewingCell;
import com.b33hive.client.structs.bhCellPool;
import com.b33hive.client.structs.bhI_CellPoolDelegate;
import com.b33hive.client.ui.bhI_UIElement;
import com.b33hive.client.ui.bhU_UI;
import com.b33hive.client.ui.cell.bhAlertManager.I_Delegate;
import com.b33hive.client.ui.dialog.bhDialog;
import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.debugging.bhU_Debug;
import com.b33hive.shared.memory.bhObjectPool;
import com.b33hive.shared.reflection.bhI_Class;
import com.b33hive.shared.statemachine.bhA_Action;
import com.b33hive.shared.statemachine.bhA_State;
import com.b33hive.shared.statemachine.bhStateEvent;
import com.b33hive.shared.structs.bhCellAddress;
import com.b33hive.shared.structs.bhPoint;
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
	
	private static bhVisualCellManager s_instance = null;
	
	private Panel m_container = null;
	
	private final bhPoint m_utilPoint1 = new bhPoint();
	private final bhPoint m_utilPoint2 = new bhPoint();
	
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
		
		s_instance = this;

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
					
					if( message.length() > bhS_ClientApp.MAX_ALERT_CHARACTERS )
					{
						message = message.substring(0, bhS_ClientApp.MAX_ALERT_CHARACTERS);
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
	
	public static bhVisualCellManager getInstance()
	{
		return s_instance;
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
	
	public bhI_BufferCellListener createVisualization(int cellSize)
	{
		bhVisualCell newVisualCell = m_pool.allocate();
		
		//s_logger.severe("Creating: " + newVisualCell.getId() + " at " + bhCellBufferManager.getInstance().getUpdateCount());
		
		if( newVisualCell.getParent() != null )
		{
			bhU_Debug.ASSERT(false, "createVisualization1");
		}
		
		newVisualCell.onCreate(cellSize);
		
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
		bhBufferCell firstCell = cellBuffer.getCellCount() > 0 ? cellBuffer.getCellAtIndex(0) : null;
		
		if ( firstCell == null )
		{
			m_container.getElement().getStyle().setDisplay(Display.NONE);
			{
				processRemovals();
			}
			m_container.getElement().getStyle().setDisplay(Display.BLOCK);
			
			return false;
		}
		
		int bufferSize = cellBuffer.getCellCount();
		int bufferWidth = cellBuffer.getWidth();
		int bufferHeight = cellBuffer.getHeight();
		
		bhCamera camera = bhCamera.getInstance();
		
		double distanceRatio = camera.calcDistanceRatio();
		
		int cellSize = cellBuffer.getCellSize();
		
		double scaling = bhVisualCell.calcCellScaling(distanceRatio, cellSize);
		/*double factor = 1e5; // = 1 * 10^5 = 100000.
		scaling = Math.round(scaling * factor) / factor;*/
		
		double cellPlusSpacingPixels = -1;
		if( cellSize == 1 )
		{
			cellPlusSpacingPixels = (bhS_App.CELL_PLUS_SPACING_PIXEL_COUNT) * scaling;
		}
		else
		{
			//--- DRK > We have to fudge the scaling here so that thick artifact lines don't appear between the meta-cell images at some zoom levels.
			//---		Basically just rounding things to the nearest pixel so that the browser renderer doesn't just decide on its own how it should look at sub-pixels.
			cellPlusSpacingPixels = (bhS_App.CELL_PIXEL_COUNT) * scaling;
			cellPlusSpacingPixels = Math.round(cellPlusSpacingPixels);
			scaling = cellPlusSpacingPixels / bhS_App.CELL_PIXEL_COUNT;
		}
		
		bhPoint basePoint = m_utilPoint1;
		firstCell.getCoordinate().calcPoint(basePoint, cellSize);
		camera.calcScreenPoint(basePoint, m_utilPoint2);
		basePoint = m_utilPoint2;
		basePoint.round();
		
		m_lastBasePoint.copy(basePoint);
	
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
			bhU_Debug.ASSERT(cellBuffer.getCellCount() == m_pool.getAllocCount(), "bhVisualCellManager::update1");
			
			for ( int i = 0; i < bufferSize; i++ )
			{
				int ix = i % bufferWidth;
				int iy = i / bufferWidth;
				
				double offsetX = (ix * (cellPlusSpacingPixels));
				double offsetY = (iy * (cellPlusSpacingPixels));
				
				bhVisualCell ithVisualCell = (bhVisualCell) cellBuffer.getCellAtIndex(i).getVisualization();
				
				ithVisualCell.validate();
				
				double translateX = basePoint.getX() + offsetX;
				double translateY = basePoint.getY() + offsetY;
				String translateProperty = bhU_UI.createTranslateTransform(translateX, translateY);
				String transform = scaleProperty != null ? translateProperty + " " + scaleProperty : translateProperty;
				bhU_UI.setTransform(ithVisualCell.getElement(), transform);
	
				//--- DRK > Have to include this optimization here because setting scaling every frame on every cell
				//---		 rocks performance, at least in GWT hosted mode.
				//---		NOTE: I'm pretty sure that CPU-based repainting of elements is the problem.
				//---			  GPU is now used where supported, but still have to figure that out for other browsers.
				/*if( cellsNeedRescaling || ithVisualCell.getParent() == null)
				{
					if( scaleProperty == null )
					{
						bhU_UI.removeScaleTransform(ithVisualCell.getElement());
					}
					else
					{
						bhU_UI.setScaling(ithVisualCell.getElement(), scaleProperty);
					}
					
					if( ithVisualCell.getParent() == null )
					{
						m_container.add(ithVisualCell);
					}
				}*/
				
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
			bhVisualCell ithVisualCell = (bhVisualCell) cellBuffer.getCellAtIndex(i).getVisualization();
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