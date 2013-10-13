package swarm.client.navigation;

import java.util.logging.Logger;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import swarm.client.entities.smCamera;
import swarm.client.managers.smCameraManager;
import swarm.client.states.camera.Action_Camera_SetViewSize;
import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
import swarm.client.states.camera.Action_Camera_SnapToPoint;
import swarm.client.states.camera.Event_GettingMapping_OnResponse;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.view.smViewContext;
import swarm.client.view.cell.smVisualCellContainer;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smA_Grid;
import swarm.shared.statemachine.smI_StateEventListener;
import swarm.shared.statemachine.smStateEvent;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;
import swarm.shared.utils.smU_Math;

public class smScrollNavigator implements smI_StateEventListener
{
	private static final Logger s_logger = Logger.getLogger(smScrollNavigator.class.getName());
	
	private final Action_Camera_SnapToPoint.Args m_args_SnapToPoint = new Action_Camera_SnapToPoint.Args();
	private final Action_Camera_SnapToCoordinate.Args m_args_SnapToCoord = new Action_Camera_SnapToCoordinate.Args();
	private final Action_Camera_SetViewSize.Args m_args_SetCameraViewSize = new Action_Camera_SetViewSize.Args();
	
	private final smViewContext m_viewContext;
	private final int m_scrollBarWidthDiv2;
	private final double m_cellHudHeight;
	
	private final Panel m_scrollContainer;
	private final Widget m_scrollContainerParent;
	private final Panel m_scrollContainerInner;
	
	private final smPoint m_utilPoint1 = new smPoint();
	
	private smA_Grid m_currentGrid = null;
	
	public smScrollNavigator(smViewContext viewContext, Panel scrollContainer, Panel scrollee)
	{
		m_viewContext = viewContext;
		m_scrollContainer = scrollContainer;
		m_scrollContainerParent = scrollContainer.getParent();
		m_scrollContainerInner = scrollee;
		m_scrollBarWidthDiv2 = (int) Math.floor(((double)this.getScrollBarWidth())/2);
		m_cellHudHeight = viewContext.appConfig.cellHudHeight;
		
		m_args_SnapToCoord.userData = smScrollNavigator.class;
		
		m_scrollContainer.addDomHandler(new ScrollHandler()
		{
			@Override
			public void onScroll(ScrollEvent event)
			{
				updateCameraFromScrollBars();
			}
			
		}, ScrollEvent.getType());
		
		this.toggleScrollBars(null);
	}
	
	public void onResize()
	{
		State_ViewingCell viewingState =  m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
		State_CameraSnapping snappingState = m_viewContext.stateContext.getEnteredState(State_CameraSnapping.class);
		
		this.toggleScrollBars(viewingState);
		
		if( snappingState != null )
		{
			m_utilPoint1.copy(m_viewContext.appContext.cameraMngr.getTargetPosition());
			this.updateCameraViewRect(false);
			
			m_args_SnapToCoord.init(snappingState.getTargetCoordinate(), m_utilPoint1);
			snappingState.getParent().performAction(Action_Camera_SnapToCoordinate.class, m_args_SnapToCoord);
		}
		else if( viewingState != null )
		{
			this.updateCameraViewRect(false);
			updateCameraFromScrollBars();
		}
		else
		{
			this.updateCameraViewRect(true);
		}
	}
	
	private void updateCameraViewRect(boolean updateBuffer)
	{
		m_args_SetCameraViewSize.init(m_scrollContainer.getElement().getClientWidth(), m_scrollContainer.getElement().getClientHeight(), updateBuffer);
		m_viewContext.stateContext.performAction(Action_Camera_SetViewSize.class, m_args_SetCameraViewSize);
	}
	
	private void updateCameraFromScrollBars()
	{
		StateMachine_Camera machine = m_viewContext.stateContext.getEnteredState(StateMachine_Camera.class);
		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
		
		if( viewingState == null )
		{
			//--- DRK > I guess when we leave viewing state and reset scroll left/top to zero,
			//---		that fires a scroll event, so valid case here...ASSERT removed for now.
			//smU_Debug.ASSERT(false, "Expected viewing state to be entered.");
			
			return;
		}
		
		smA_Grid grid = viewingState.getCell().getGrid();
		
		double minViewWidth = smU_CameraViewport.calcViewWindowWidth(grid);
		double windowWidth = m_scrollContainer.getElement().getClientWidth();
		double minViewHeight = smU_CameraViewport.calcViewWindowHeight(grid, m_cellHudHeight);
		double windowHeight = m_scrollContainer.getElement().getClientHeight();
		smGridCoordinate coord = viewingState.getCell().getCoordinate();
		smPoint centerPoint = m_utilPoint1;
		smU_CameraViewport.calcViewWindowCenter(grid, viewingState.getCell().getCoordinate(), m_cellHudHeight, centerPoint);
		
		if( windowWidth < minViewWidth )
		{
			int scroll = m_scrollContainer.getElement().getScrollLeft();
			double newPos = centerPoint.getX() - (minViewWidth - windowWidth)/2 + scroll;
			centerPoint.setX(newPos);
		}
		
		if( windowHeight < minViewHeight )
		{
			int scroll = m_scrollContainer.getElement().getScrollTop();
			double newPos = centerPoint.getY() - (minViewHeight - windowHeight)/2 + scroll;
			centerPoint.setY(newPos);
		}
		
		smU_CameraViewport.calcConstrainedCameraPoint(grid, coord, centerPoint, windowWidth, windowHeight, m_cellHudHeight, centerPoint);
		
		m_args_SnapToPoint.init(centerPoint, true, false);
		m_viewContext.stateContext.performAction(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
	}
	
	private void toggleScrollBars(State_ViewingCell viewingState_nullable)
	{
		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
		
		if( viewingState_nullable != null && viewingState_nullable.isEntered() )
		{
			StateMachine_Camera machine = viewingState_nullable.getParent();
			
			smA_Grid grid = viewingState_nullable.getCell().getGrid();
			
			double minViewWidth = smU_CameraViewport.calcViewWindowWidth(grid);
			double windowWidth = m_scrollContainer.getElement().getClientWidth();
			
			smPoint cameraPoint = m_viewContext.appContext.cameraMngr.getCamera().getPosition();
			smPoint centerPoint = m_utilPoint1;
			smU_CameraViewport.calcViewWindowCenter(grid, viewingState_nullable.getCell().getCoordinate(), m_cellHudHeight, centerPoint);
			
			if( windowWidth < minViewWidth )
			{			
				scrollerStyle.setOverflowX(Overflow.SCROLL);
				innerStyle.setProperty("minWidth", minViewWidth+"px");
				
				if( viewingState_nullable.getUpdateCount() == 0 )
				{
					double delta = (minViewWidth - windowWidth)/2;
					double cameraPos = smU_Math.clamp(cameraPoint.getX(), centerPoint.getX()-delta, centerPoint.getX()+delta);
					double scroll = (cameraPos - (centerPoint.getX()-delta));
					m_scrollContainer.getElement().setScrollLeft((int) Math.round(scroll));
				}
			}
			else
			{
				scrollerStyle.setOverflowX(Overflow.HIDDEN);
				innerStyle.clearProperty("minWidth");
				m_scrollContainer.getElement().setScrollLeft(0);
			}
			
			double minViewHeight = smU_CameraViewport.calcViewWindowHeight(grid, m_cellHudHeight);
			double windowHeight = m_scrollContainer.getElement().getClientHeight();
			
			if( windowHeight < minViewHeight )
			{
				scrollerStyle.setOverflowY(Overflow.SCROLL);
				innerStyle.setProperty("minHeight", minViewHeight+"px");
				
				if( viewingState_nullable.getUpdateCount() == 0 )
				{
					double delta = (minViewHeight - windowHeight)/2;
					double cameraPos = smU_Math.clamp(cameraPoint.getY(), centerPoint.getY()-delta, centerPoint.getY()+delta);
					double scroll = (cameraPos - (centerPoint.getY()-delta));
					m_scrollContainer.getElement().setScrollTop((int) Math.round(scroll));
				}
			}
			else
			{
				scrollerStyle.setOverflowY(Overflow.HIDDEN);
				innerStyle.clearProperty("minHeight");
				m_scrollContainer.getElement().setScrollTop(0);
			}
		}
		else
		{
			scrollerStyle.setOverflowX(Overflow.HIDDEN);
			scrollerStyle.setOverflowY(Overflow.HIDDEN);
			innerStyle.clearProperty("minWidth");
			innerStyle.clearProperty("minHeight");
			m_scrollContainer.getElement().setScrollLeft(0);
			m_scrollContainer.getElement().setScrollTop(0);
		}
	}
	
	private void adjustViewportOnArrival()
	{
		State_ViewingCell viewingState =  m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
		StateMachine_Camera machine = viewingState.getParent();
		smA_Grid grid = viewingState.getCell().getGrid();
		smGridCoordinate coord = viewingState.getCell().getCoordinate();
		smCamera camera = m_viewContext.appContext.cameraMngr.getCamera();
		smPoint cameraPoint = camera.getPosition();
		m_utilPoint1.copy(cameraPoint);
		
		double minViewWidth = smU_CameraViewport.calcViewWindowWidth(grid);
		double windowWidth = m_scrollContainer.getElement().getClientWidth();
		double minViewHeight = smU_CameraViewport.calcViewWindowHeight(grid, m_cellHudHeight);
		double windowHeight = m_scrollContainer.getElement().getClientHeight();
		
		boolean needToAdjust = false;
		
		if( windowWidth < minViewWidth )
		{
			m_utilPoint1.incY(-m_scrollBarWidthDiv2);
			needToAdjust = true;
		}
		
		if( windowHeight < minViewHeight )
		{
			m_utilPoint1.incX(-m_scrollBarWidthDiv2);
			needToAdjust = true;
		}
		
		if( needToAdjust )
		{
			this.updateCameraViewRect(false);
			
			smU_CameraViewport.calcConstrainedCameraPoint(grid, coord, m_utilPoint1, windowWidth, windowHeight, m_cellHudHeight, m_utilPoint1);
			
			m_args_SnapToPoint.init(m_utilPoint1, true, false);
			m_viewContext.stateContext.performAction(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
		}
	}
	
	public void adjustSnapTargetPoint(smGridCoordinate targetCoord, smPoint point_out)
	{
		smA_Grid grid = this.m_viewContext.appContext.gridMngr.getGrid();
		smCameraManager cameraMngr = this.m_viewContext.appContext.cameraMngr;
		double minViewWidth = smU_CameraViewport.calcViewWindowWidth(grid);
		double windowWidth = m_scrollContainerParent.getElement().getClientWidth();
		double minViewHeight = smU_CameraViewport.calcViewWindowHeight(grid, m_cellHudHeight);
		double windowHeight = m_scrollContainerParent.getElement().getClientHeight();
		smPoint targetPoint = point_out;

		if( windowWidth < minViewWidth )
		{
			targetPoint.incY(m_scrollBarWidthDiv2);
			windowHeight = 0;
		}
		
		if( windowHeight < minViewHeight )
		{
			targetPoint.incX(m_scrollBarWidthDiv2);
			windowWidth = 0;
		}
		
		smU_CameraViewport.calcConstrainedCameraPoint(grid, targetCoord, targetPoint, windowWidth, windowHeight, m_cellHudHeight, targetPoint);
	}
	
	@Override
	public void onStateEvent(smStateEvent event)
	{
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if ( event.getState() instanceof State_ViewingCell )
				{
					State_ViewingCell state = event.getState();
					StateMachine_Camera machine = state.getParent();
					
					m_currentGrid = state.getCell().getGrid();
					
					toggleScrollBars(state);
					
					adjustViewportOnArrival();
				}
				
				break;
			}
	
			case DID_EXIT:
			{
				if ( event.getState() instanceof State_ViewingCell )
				{
					double minViewWidth = smU_CameraViewport.calcViewWindowWidth(m_currentGrid);
					double windowWidth = m_scrollContainer.getElement().getClientWidth();
					double minViewHeight = smU_CameraViewport.calcViewWindowHeight(m_currentGrid, m_cellHudHeight);
					double windowHeight = m_scrollContainer.getElement().getClientHeight();
					boolean updateCameraViewRect = windowWidth < minViewWidth || windowHeight < minViewHeight;
					
					toggleScrollBars((State_ViewingCell) event.getState());
					
					if( updateCameraViewRect )
					{
						this.updateCameraViewRect(true);
					}
					
					m_currentGrid = null;
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				
				break;
			}
		}
	}
	
	private native double getScrollBarWidth()
	/*-{
			var scrollDiv = $doc.createElement("div");
			scrollDiv.className = "sm_scrollbar_query";
			$doc.body.appendChild(scrollDiv);
			
			// Get the scrollbar width
			var scrollBarWidth = scrollDiv.offsetWidth - scrollDiv.clientWidth;
			
			// Delete the DIV 
			$doc.body.removeChild(scrollDiv);
			
			return scrollBarWidth;
	}-*/;
}