package swarm.client.navigation;

import java.util.logging.Logger;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

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
	
	private final Panel m_scrollContainer;
	private final Widget m_scrollContainerParent;
	private final Panel m_scrollContainerInner;
	
	private final smPoint m_utilPoint1 = new smPoint();
	
	public smScrollNavigator(smViewContext viewContext, Panel scrollContainer, Panel scrollee)
	{
		m_viewContext = viewContext;
		m_scrollContainer = scrollContainer;
		m_scrollContainerParent = scrollContainer.getParent();
		m_scrollContainerInner = scrollee;
		m_scrollBarWidthDiv2 = (int) Math.floor(((double)this.getScrollBarWidth())/2);
		
		m_args_SnapToCoord.setUserData(smScrollNavigator.class);
		
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
		boolean isSnapping = m_viewContext.stateContext.isEntered(State_CameraSnapping.class);
		
		this.toggleScrollBars(viewingState);
		
		if( isSnapping )
		{
			this.updateCameraViewRect(true);
			adjustSnapTargetPoint();
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
			smU_Debug.ASSERT(false, "Expected viewing state to be entered.");
			
			return;
		}
		
		smA_Grid grid = viewingState.getCell().getGrid();
		
		double minViewWidth = machine.calcViewWindowWidth(grid);
		double windowWidth = m_scrollContainer.getElement().getClientWidth();
		double minViewHeight = machine.calcViewWindowHeight(grid);
		double windowHeight = m_scrollContainer.getElement().getClientHeight();
		
		smPoint centerPoint = m_utilPoint1;
		machine.calcViewWindowCenter(grid, viewingState.getCell().getCoordinate(), centerPoint);
		
		if( windowWidth < minViewWidth )
		{
			int scroll = m_scrollContainer.getElement().getScrollLeft();
			double newPos = centerPoint.getX() - (minViewWidth - windowWidth)/2 + scroll;
			centerPoint.setX(newPos);
			
			s_logger.severe("1: " + centerPoint.toString());
		}
		
		if( windowHeight < minViewHeight )
		{
			int scroll = m_scrollContainer.getElement().getScrollTop();
			double newPos = centerPoint.getY() - (minViewHeight - windowHeight)/2 + scroll;
			centerPoint.setY(newPos);
		}
		
		machine.calcConstrainedCameraPoint(grid, viewingState.getCell().getCoordinate(), centerPoint, centerPoint);
		
		s_logger.severe("2: " + centerPoint.toString());
		
		m_args_SnapToPoint.init(centerPoint, true, false);
		m_viewContext.stateContext.performAction(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
		
		s_logger.severe("3: " + m_viewContext.appContext.cameraMngr.getCamera().getPosition());
	}
	
	private void toggleScrollBars(State_ViewingCell viewingState_nullable)
	{
		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
		
		if( viewingState_nullable != null && viewingState_nullable.isEntered() )
		{
			StateMachine_Camera machine = viewingState_nullable.getParent();
			
			smA_Grid grid = viewingState_nullable.getCell().getGrid();
			
			double minViewWidth = machine.calcViewWindowWidth(grid);
			double windowWidth = m_scrollContainer.getElement().getClientWidth();
			
			smPoint cameraPoint = m_viewContext.appContext.cameraMngr.getCamera().getPosition();
			smPoint centerPoint = m_utilPoint1;
			machine.calcViewWindowCenter(grid, viewingState_nullable.getCell().getCoordinate(), centerPoint);
			
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
			
			double minViewHeight = machine.calcViewWindowHeight(grid);
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
		
		smPoint cameraPoint = m_viewContext.appContext.cameraMngr.getCamera().getPosition();
		m_utilPoint1.copy(cameraPoint);
		
		double minViewWidth = machine.calcViewWindowWidth(grid);
		double windowWidth = m_scrollContainerParent.getElement().getClientWidth();
		double minViewHeight = machine.calcViewWindowHeight(grid);
		double windowHeight = m_scrollContainerParent.getElement().getClientHeight();
		
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
			
			machine.calcConstrainedCameraPoint(grid, viewingState.getCell().getCoordinate(), m_utilPoint1, m_utilPoint1);
			
			m_args_SnapToPoint.init(m_utilPoint1, true, false);
			m_viewContext.stateContext.performAction(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
		}
	}
	
	private void adjustSnapTargetPoint()
	{
		State_CameraSnapping state = m_viewContext.stateContext.getEnteredState(State_CameraSnapping.class);
		
		if( state == null )
		{
			smU_Debug.ASSERT(false, "Expected snapping state to be entered.");
			
			return;
		}
		
		StateMachine_Camera machine = state.getParent();
		smA_Grid grid = this.m_viewContext.appContext.gridMngr.getGrid();
		smCameraManager cameraMngr = this.m_viewContext.appContext.cameraMngr;
		double minViewWidth = machine.calcViewWindowWidth(grid);
		double windowWidth = m_scrollContainerParent.getElement().getClientWidth();
		double minViewHeight = machine.calcViewWindowHeight(grid);
		double windowHeight = m_scrollContainerParent.getElement().getClientHeight();
		smPoint targetPoint = m_utilPoint1;
		targetPoint.copy(cameraMngr.getTargetPosition());

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
		
		machine.calcConstrainedCameraPoint(grid, state.getTargetCoordinate(), targetPoint, windowWidth, windowHeight, targetPoint);
		m_args_SnapToCoord.init(state.getTargetCoordinate(), targetPoint);
		machine.performAction(Action_Camera_SnapToCoordinate.class, m_args_SnapToCoord);
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
					
					smA_Grid grid = state.getCell().getGrid();
					
					toggleScrollBars(state);
					
					adjustViewportOnArrival();
				}
				
				break;
			}
	
			case DID_EXIT:
			{
				if ( event.getState() instanceof State_ViewingCell )
				{
					toggleScrollBars((State_ViewingCell) event.getState());
					this.updateCameraViewRect(true);
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == Action_Camera_SnapToCoordinate.class )
				{
					if( event.getActionArgs().getUserData() == smScrollNavigator.class )  return;
					
					adjustSnapTargetPoint();
				}
				
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