package swarm.client.navigation;

import java.util.logging.Logger;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
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
	private final Panel m_mouseLayer;
	
	private final smPoint m_utilPoint1 = new smPoint();
	private final smPoint m_originalTargetSnapPoint = new smPoint();
	
	private smA_Grid m_currentGrid = null;
	
	public smScrollNavigator(smViewContext viewContext, Panel scrollContainer, Panel scrollee, Panel mouseLayer)
	{
		m_viewContext = viewContext;
		m_scrollContainer = scrollContainer;
		m_scrollContainerParent = scrollContainer.getParent();
		m_scrollContainerInner = scrollee;
		m_mouseLayer = mouseLayer;
		m_scrollBarWidthDiv2 = (int) Math.floor(((double)this.getScrollBarWidth())/2);
		m_cellHudHeight = viewContext.appConfig.cellHudHeight;
		
		m_args_SnapToCoord.userData = this.getClass();
		
		m_scrollContainer.addDomHandler(new ScrollHandler()
		{
			@Override
			public void onScroll(ScrollEvent event)
			{
				s_logger.severe(m_scrollContainer.getElement().getScrollTop() + " " + m_scrollContainer.getElement().getScrollHeight());
				updateCameraFromScrollBars();
			}
			
		}, ScrollEvent.getType());
		
		this.toggleScrollBars(null);
	}
	
	public int getScrollX()
	{
		return m_scrollContainer.getElement().getScrollLeft();
	}
	
	public int getScrollY()
	{
		return m_scrollContainer.getElement().getScrollTop();
	}
	
	public void onResize()
	{
		State_ViewingCell viewingState =  m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
		State_CameraSnapping snappingState = m_viewContext.stateContext.getEnteredState(State_CameraSnapping.class);
		
		this.toggleScrollBars(viewingState);
		
		if( snappingState != null )
		{
			m_utilPoint1.copy(m_originalTargetSnapPoint);
			this.updateCameraViewRect(false, false);
			
			this.adjustSnapTargetPoint_private(snappingState.getTargetCoordinate(), m_utilPoint1);
			
			m_args_SnapToCoord.init(snappingState.getTargetCoordinate(), m_utilPoint1);
			snappingState.getParent().performAction(Action_Camera_SnapToCoordinate.class, m_args_SnapToCoord);
		}
		else if( viewingState != null )
		{
			this.updateCameraViewRect(false, false);
			updateCameraFromScrollBars();
		}
		else
		{
			this.updateCameraViewRect(true, false);
		}
	}
	
	private void updateCameraViewRect(boolean updateBuffer, boolean maintainApparentPosition)
	{
		m_args_SetCameraViewSize.init(this.getWindowWidth(), this.getWindowHeight(), updateBuffer, maintainApparentPosition);
		m_viewContext.stateContext.performAction(Action_Camera_SetViewSize.class, m_args_SetCameraViewSize);
	}
	
	private void updateCameraFromScrollBars()
	{
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
		double windowWidth = this.getWindowWidth();
		double minViewHeight = smU_CameraViewport.calcViewWindowHeight(grid, m_cellHudHeight);
		double windowHeight = this.getWindowHeight();
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
	
	private void clearScrollbarX()
	{
		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
		Style mouseLayerStyle = this.m_mouseLayer.getElement().getStyle();
		
		scrollerStyle.setOverflowX(Overflow.HIDDEN);
		innerStyle.clearProperty("minWidth");
		mouseLayerStyle.clearProperty("minWidth");
		m_scrollContainer.getElement().setScrollLeft(0);
	}
	
	private void clearScrollbarY()
	{
		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
		Style mouseLayerStyle = this.m_mouseLayer.getElement().getStyle();
		
		scrollerStyle.setOverflowY(Overflow.HIDDEN);
		innerStyle.clearProperty("minHeight");
		mouseLayerStyle.clearProperty("minHeight");
		m_scrollContainer.getElement().setScrollTop(0);
	}
	
	private void setInitialScrollbarPosX(State_ViewingCell viewingState)
	{
		smA_Grid grid = viewingState.getCell().getGrid();
		smPoint cameraPoint = m_viewContext.appContext.cameraMngr.getCamera().getPosition();
		smPoint centerPoint = m_utilPoint1;
		smU_CameraViewport.calcViewWindowCenter(grid, viewingState.getCell().getCoordinate(), m_cellHudHeight, centerPoint);
		double minViewWidth = smU_CameraViewport.calcViewWindowWidth(grid);
		double windowWidth = this.getWindowWidth();
		
		double delta = (minViewWidth - windowWidth)/2;
		double cameraPos = smU_Math.clamp(cameraPoint.getX(), centerPoint.getX()-delta, centerPoint.getX()+delta);
		double scroll = (cameraPos - (centerPoint.getX()-delta));
		m_scrollContainer.getElement().setScrollLeft((int) Math.round(scroll));
	}
	
	private void setInitialScrollbarPosY(State_ViewingCell viewingState)
	{
		smA_Grid grid = viewingState.getCell().getGrid();
		smPoint cameraPoint = m_viewContext.appContext.cameraMngr.getCamera().getPosition();
		smPoint centerPoint = m_utilPoint1;
		smU_CameraViewport.calcViewWindowCenter(grid, viewingState.getCell().getCoordinate(), m_cellHudHeight, centerPoint);
		double minViewHeight = smU_CameraViewport.calcViewWindowHeight(grid, m_cellHudHeight);
		double windowHeight = this.getWindowHeight();
		
		double delta = (minViewHeight - windowHeight)/2;
		double cameraPos = smU_Math.clamp(cameraPoint.getY(), centerPoint.getY()-delta, centerPoint.getY()+delta);
		double scroll = (cameraPos - (centerPoint.getY()-delta));
		int scrollRounded = (int) Math.round(scroll);
		m_scrollContainer.getElement().setScrollTop(scrollRounded);
	}
	
	private void toggleScrollBarX(State_ViewingCell viewingState, double minViewWidth, smPoint cameraPoint, smPoint centerPoint)
	{
		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
		Style mouseLayerStyle = this.m_mouseLayer.getElement().getStyle();
		
		double windowWidth = this.getWindowWidth();
		
		if( windowWidth < minViewWidth )
		{
			scrollerStyle.setOverflowX(Overflow.SCROLL);
			String widthProperty = minViewWidth+"px";
			innerStyle.setProperty("minWidth", widthProperty);
			mouseLayerStyle.setProperty("minWidth", widthProperty);
			//innerStyle.setProperty("maxWidth", widthProperty);
			//mouseLayerStyle.setProperty("maxWidth", widthProperty);
		}
		else
		{
			clearScrollbarX();
		}
	}
	
	private void toggleScrollBarY(State_ViewingCell viewingState, double minViewHeight, smPoint cameraPoint, smPoint centerPoint)
	{
		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
		Style mouseLayerStyle = this.m_mouseLayer.getElement().getStyle();
		
		double windowHeight = this.getWindowHeight();
		
		if( windowHeight < minViewHeight )
		{
			scrollerStyle.setOverflowY(Overflow.SCROLL);
			String heightProperty = minViewHeight+"px";
			innerStyle.setProperty("minHeight", heightProperty);
			mouseLayerStyle.setProperty("minHeight", heightProperty);
			//innerStyle.setProperty("maxHeight", heightProperty);
			//mouseLayerStyle.setProperty("maxHeight", heightProperty);
		}
		else
		{
			clearScrollbarY();
		}
	}
	
	private void toggleScrollBars(State_ViewingCell viewingState_nullable)
	{
		if( viewingState_nullable != null && viewingState_nullable.isEntered() )
		{			
			smA_Grid grid = viewingState_nullable.getCell().getGrid();
			smPoint cameraPoint = m_viewContext.appContext.cameraMngr.getCamera().getPosition();
			smPoint centerPoint = m_utilPoint1;
			smU_CameraViewport.calcViewWindowCenter(grid, viewingState_nullable.getCell().getCoordinate(), m_cellHudHeight, centerPoint);
			
			double minViewWidth = smU_CameraViewport.calcViewWindowWidth(grid);
			double minViewHeight = smU_CameraViewport.calcViewWindowHeight(grid, m_cellHudHeight);
			
			toggleScrollBarX(viewingState_nullable, minViewWidth, cameraPoint, centerPoint);
			toggleScrollBarY(viewingState_nullable, minViewHeight, cameraPoint, centerPoint);
			
			//--- DRK > Pretty sure that we have to recheck the x scroll bar to cover fringe
			//---		cases where the appearance of the y scroll bar diminishes the view width
			//---		to the point where the X scroll bar is in fact needed after all.
			toggleScrollBarX(viewingState_nullable, minViewWidth, cameraPoint, centerPoint);
		}
		else
		{
			clearScrollbarX();
			clearScrollbarY();
		}
	}
	
	private double getWindowWidth()
	{
		double value = this.m_scrollContainer.getElement().getClientWidth();
		
		if( value == 0 )
		{
			if( m_scrollContainer.getElement().getOffsetWidth() != 0 )
			{
				return 0;
			}
			else
			{
				return m_viewContext.splitPanel.getCellPanelWidth();
			}
		}
		else
		{
			return value;
		}
	}
	
	private double getWindowHeight()
	{
		double value = this.m_scrollContainer.getElement().getClientHeight();
		
		if( value == 0 )
		{
			if( m_scrollContainer.getElement().getOffsetHeight() != 0 )
			{
				return 0;
			}
			else
			{
				return RootPanel.get().getOffsetHeight();
			}
		}
		else
		{
			return value;
		}
	}
	
	public boolean isScrollingX()
	{
		if( m_currentGrid == null )  return false;
		
		double minViewWidth = smU_CameraViewport.calcViewWindowWidth(m_currentGrid);
		double windowWidth = this.getWindowWidth();
		
		return windowWidth < minViewWidth;
	}
	
	public boolean isScrollingY()
	{
		if( m_currentGrid == null )  return false;
		
		double minViewHeight = smU_CameraViewport.calcViewWindowHeight(m_currentGrid, m_cellHudHeight);
		double windowHeight = this.getWindowHeight();
		
		return windowHeight < minViewHeight;
	}
	
	public void adjustSnapTargetPoint(Action_Camera_SnapToCoordinate.Args args)
	{
		if( args.userData == this.getClass() )   return;
		
		m_originalTargetSnapPoint.copy(args.getTargetPoint());
		
		this.adjustSnapTargetPoint_private(args.getTargetCoordinate(), args.getTargetPoint());
	}
	
	public void adjustSnapTargetPoint_private(smGridCoordinate targetCoord, smPoint point_out)
	{
		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
		smA_Grid grid = this.m_viewContext.appContext.gridMngr.getGrid();
		double minViewWidth = smU_CameraViewport.calcViewWindowWidth(grid);
		double minViewHeight = smU_CameraViewport.calcViewWindowHeight(grid, m_cellHudHeight);
		double windowWidth = this.getWindowWidth();
		double windowHeight = this.getWindowHeight();
		double originalWindowWidth = windowWidth;
		double originalWindowHeight = windowHeight;
		
		point_out.copy(m_originalTargetSnapPoint);
		boolean widthSmaller = false;
		boolean heightSmaller = false;
		
		if( windowWidth < minViewWidth )
		{
			if( viewingState == null )
			{
				windowHeight -= m_scrollBarWidthDiv2*2;
			}
			
			widthSmaller = true;
		}

		if( windowHeight < minViewHeight )
		{
			if( viewingState == null )
			{
				windowWidth -= m_scrollBarWidthDiv2*2;
			}

			heightSmaller = true;
		}
		
		if( !widthSmaller && windowWidth < minViewWidth )
		{
			if( viewingState == null )
			{
				windowHeight -= m_scrollBarWidthDiv2*2;
			}
			
			widthSmaller = true;
		}
		
		double newWindowWidth = windowWidth;
		double newWindowHeight = windowHeight;

		if( widthSmaller && heightSmaller )
		{
			/*if( viewingState == null )
			{
				point_out.incX(m_scrollBarWidthDiv2*.5);
				point_out.incY(m_scrollBarWidthDiv2*.5);
			}
			else
			{
				point_out.incX(m_scrollBarWidthDiv2*.5);
				point_out.incY(m_scrollBarWidthDiv2*.5);
			}*/
			double deltaX = (minViewWidth - originalWindowWidth)/2;
			double deltaY = (minViewHeight - originalWindowHeight)/2;
			smU_CameraViewport.calcViewWindowCenter(grid, targetCoord, m_cellHudHeight, m_utilPoint1);
			boolean maxX = point_out.getX() >= m_utilPoint1.getX() +deltaX;
			boolean maxY = point_out.getY() >= m_utilPoint1.getY() +deltaY;
			//boolean maxY = m_utilPoint1.getY() + windowHeight/2;
			
			if( maxX )
			{
				point_out.incX(m_scrollBarWidthDiv2*1.5+1);
			}
			else
			{
				point_out.incX(m_scrollBarWidthDiv2*.5);
			}

			if( maxY )
			{
				point_out.incY(m_scrollBarWidthDiv2*1.5+1);
			}
			else
			{
				point_out.incY(m_scrollBarWidthDiv2*.5);
			}
			
			//newWindowWidth = windowWidth + m_scrollBarWidthDiv2;
			//newWindowHeight = windowHeight + m_scrollBarWidthDiv2;
		}
		else if( widthSmaller )
		{
			if( viewingState == null )
			{
				point_out.incY(m_scrollBarWidthDiv2);
			}

			point_out.incX(-m_scrollBarWidthDiv2*.5);
			newWindowWidth = windowWidth + m_scrollBarWidthDiv2;
			
			newWindowHeight = 0;
			
			smU_CameraViewport.calcConstrainedCameraPoint(grid, targetCoord, point_out, newWindowWidth, newWindowHeight, m_cellHudHeight, point_out);
		}
		else if( heightSmaller )
		{
			if( viewingState == null )
			{
				point_out.incX(m_scrollBarWidthDiv2);
			}
			
			point_out.incY(-m_scrollBarWidthDiv2*.5);
			newWindowHeight = windowHeight + m_scrollBarWidthDiv2;
			
			newWindowWidth = 0;
			
			smU_CameraViewport.calcConstrainedCameraPoint(grid, targetCoord, point_out, newWindowWidth, newWindowHeight, m_cellHudHeight, point_out);
		}
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
					
					m_currentGrid = state.getCell().getGrid();
					
					toggleScrollBars(state);
					
					boolean isScrollingX = isScrollingX();
					boolean isScrollingY = isScrollingY();

					if( isScrollingX || isScrollingY )
					{
						this.updateCameraViewRect(true,  true);
						
						if( isScrollingX )
						{
							this.setInitialScrollbarPosX(state);
						}
						
						if( isScrollingY )
						{
							this.setInitialScrollbarPosY(state);
						}
					}
				}
				
				break;
			}

			case DID_EXIT:
			{
				if ( event.getState() instanceof State_ViewingCell )
				{
					boolean isScrollingX = isScrollingX();
					boolean isScrollingY = isScrollingY();
					
					toggleScrollBars((State_ViewingCell) event.getState());

					if( isScrollingX || isScrollingY )
					{
						this.updateCameraViewRect(true, true);
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
	
	private static native double getScrollBarWidth()
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