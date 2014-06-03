package swarm.client.navigation;

import java.util.logging.Logger;

import javax.persistence.criteria.Root;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import swarm.client.entities.BufferCell;
import swarm.client.entities.Camera;
import swarm.client.managers.CameraManager;
import swarm.client.managers.CellAddressManager;
import swarm.client.managers.CellBuffer;
import swarm.client.managers.CellBufferManager;
import swarm.client.managers.CellSizeManager;
import swarm.client.states.camera.Action_Camera_SetViewSize;
import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
import swarm.client.states.camera.Action_Camera_SnapToPoint;
import swarm.client.states.camera.Event_CameraSnapping_OnTargetCellAppeared;
import swarm.client.states.camera.Event_Camera_OnCellSizeFound;
import swarm.client.states.camera.Event_GettingMapping_OnResponse;
import swarm.client.states.camera.I_State_SnappingOrViewing;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.view.U_Cell;
import swarm.client.view.U_Css;
import swarm.client.view.ViewContext;
import swarm.client.view.cell.VisualCell;
import swarm.client.view.cell.VisualCellContainer;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Grid;
import swarm.shared.lang.Boolean;
import swarm.shared.statemachine.A_StateContextForwarder;
import swarm.shared.statemachine.I_StateEventListener;
import swarm.shared.statemachine.StateEvent;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.CellSize;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;
import swarm.shared.structs.Rect;
import swarm.shared.structs.Vector;
import swarm.shared.utils.U_Math;

public class ScrollNavigator implements I_StateEventListener
{
	public interface I_ScrollListener
	{
		void onScroll();
	}
	
	private static final Logger s_logger = Logger.getLogger(ScrollNavigator.class.getName());
	
	private final Action_Camera_SnapToPoint.Args m_args_SnapToPoint = new Action_Camera_SnapToPoint.Args();
	private final Action_Camera_SnapToCoordinate.Args m_args_SnapToCoord = new Action_Camera_SnapToCoordinate.Args();
	private final Action_Camera_SetViewSize.Args m_args_SetCameraViewSize = new Action_Camera_SetViewSize.Args();
	
	private final ViewContext m_viewContext;
	private final int m_scrollBarWidthDiv2;
	private final double m_cellHudHeight;
	
	private final Panel m_scrollContainer;
	private final Panel m_scrollContainerInner;
	private final Panel m_mouseLayer;
	
	private final Point m_utilPoint1 = new Point();
	private final Point m_utilPoint2 = new Point();
	
	private final CellAddressMapping m_utilMapping = new CellAddressMapping();
	
	private final FocusedLayout m_layout = new FocusedLayout();
	
	private I_ScrollListener m_scrollListener = null;
	
	public ScrollNavigator(ViewContext viewContext, Panel scrollContainer, Panel scrollee, Panel mouseLayer)
	{
		m_viewContext = viewContext;
		m_scrollContainer = scrollContainer;
		m_scrollContainerInner = scrollee;
		m_mouseLayer = mouseLayer;
		m_scrollBarWidthDiv2 = (int) Math.floor(((double)U_Css.getScrollBarWidth())/2);
		m_cellHudHeight = viewContext.appConfig.cellHudHeight;
		
		m_scrollContainer.getElement().getStyle().setZIndex(1);
		
		m_args_SnapToCoord.set(this.getClass());
		m_args_SnapToCoord.historyShouldIgnore = true;
		
		m_scrollContainer.addDomHandler(new ScrollHandler()
		{
			@Override
			public void onScroll(ScrollEvent event)
			{
				State_ViewingCell viewingState =  m_viewContext.stateContext.get(State_ViewingCell.class);
				if( viewingState != null )
				{
					VisualCell cell = (VisualCell)viewingState.getCell().getVisualization();
					setTargetLayout(cell);
					//m_viewContext.cellMngr.updateCellTransforms(0.0);
				}
				else
				{
					//--- DRK > I guess when we leave viewing state and reset scroll left/top to zero,
					//---		that fires a scroll event, so valid case here...ASSERT removed for now.
					//smU_Debug.ASSERT(false, "Expected viewing state to be entered.");
				}
				
				if( m_scrollListener != null )  m_scrollListener.onScroll();
			}
			
		}, ScrollEvent.getType());
		
		this.toggleScrollBars(null);
	}
	
	public void addScrollListener(I_ScrollListener listener)
	{
		if( m_scrollListener != null )  throw new Error(); // TODO: allow multiple listeners if necessary
		
		m_scrollListener = listener;
	}
	
	public Panel getScrollContainer()
	{
		return m_scrollContainer;
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
		onResize_private();
	}
	
	private void onResize_private()
	{
		s_logger.severe("RESIZE_START");
		
		State_ViewingCell viewingState =  m_viewContext.stateContext.getEntered(State_ViewingCell.class);
		State_CameraSnapping snappingState = m_viewContext.stateContext.getEntered(State_CameraSnapping.class);
		
		this.toggleScrollBars(viewingState);
		
		if( viewingState != null || snappingState != null )
		{
			//s_logger.severe(m_viewContext.appContext.cameraMngr.getCamera().getPosition()+"");
			
			GridCoordinate coord = null;
			
			if( viewingState != null )
			{
				coord = viewingState.getCell().getCoordinate();
			}
			else
			{
				coord = snappingState.getTargetCoord();
			}
			
			this.updateCameraViewRect(false, false);
			
			A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
			grid.calcCoordTopLeftPoint(coord, 1, m_utilPoint1);
			s_logger.severe("1: " + m_utilPoint1);
			
			double cellHudHeight = m_viewContext.appConfig.cellHudHeight;
			double viewWidth = m_viewContext.appContext.cameraMngr.getCamera().getViewWidth();
			double viewHeight = m_viewContext.appContext.cameraMngr.getCamera().getViewHeight();
			
			U_CameraViewport.calcConstrainedCameraPoint(grid, coord, m_utilPoint1, viewWidth, viewHeight, cellHudHeight, m_utilPoint1);
			s_logger.severe("2: " + m_utilPoint1);
			
			//TODO: This series calls calcLayout twice.
			this.adjustTargetSnapPoint_private(coord, m_utilPoint1);
			s_logger.severe("3: " + m_utilPoint1);
			this.setTargetLayout(coord);
			
			if( viewingState == null )
			{
				m_args_SnapToCoord.init(coord, m_utilPoint1);
				snappingState.getContext().perform(Action_Camera_SnapToCoordinate.class, m_args_SnapToCoord);
			}
			else
			{
				m_args_SnapToPoint.init(m_utilPoint1, true, false);
				viewingState.getContext().perform(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
			}
			
			//s_logger.severe(m_viewContext.appContext.cameraMngr.getCamera().getPosition()+"");
		}
		/*else if( viewingState != null )
		{
			this.updateCameraViewRect(true, false);
			//updateCameraFromScrollBars();
			this.setTargetLayout((VisualCell)viewingState.getCell().getVisualization());
		}*/
		else
		{
			this.updateCameraViewRect(true, false);
		}
		
		s_logger.severe("RESIZE_END\n\n");
	}
	
	private void updateCameraViewRect(boolean updateBuffer, boolean maintainApparentPosition)
	{
		m_args_SetCameraViewSize.init(this.getWindowWidth(), this.getWindowHeight(), updateBuffer, maintainApparentPosition);
		m_viewContext.stateContext.perform(Action_Camera_SetViewSize.class, m_args_SetCameraViewSize);
	}
	
	/*private void updateCameraFromScrollBars()
	{
		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
		
		if( viewingState == null )
		{
			//--- DRK > I guess when we leave viewing state and reset scroll left/top to zero,
			//---		that fires a scroll event, so valid case here...ASSERT removed for now.
			//smU_Debug.ASSERT(false, "Expected viewing state to be entered.");
			
			return;
		}
		
		A_Grid grid = viewingState.getCell().getGrid();
		
		double minViewWidth = U_CameraViewport.calcCellWidthRequirement(grid);
		double windowWidth = this.getWindowWidth();
		double minViewHeight = U_CameraViewport.calcCellHeightRequirement(grid, m_cellHudHeight);
		double windowHeight = this.getWindowHeight();
		GridCoordinate coord = viewingState.getCell().getCoordinate();
		Point centerPoint = m_utilPoint1;
		U_CameraViewport.calcViewWindowCenter(grid, viewingState.getCell().getCoordinate(), m_cellHudHeight, centerPoint);
		
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
		
		U_CameraViewport.calcConstrainedCameraPoint(grid, coord, centerPoint, windowWidth, windowHeight, m_cellHudHeight, centerPoint);
		
		m_args_SnapToPoint.init(centerPoint, true, false);
		m_viewContext.stateContext.performAction(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
	}*/
	
	private void clearScrollbarX()
	{
		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
		Style mouseLayerStyle = this.m_mouseLayer.getElement().getStyle();
		
		scrollerStyle.setOverflowX(Overflow.HIDDEN);
		innerStyle.clearProperty("width");
		mouseLayerStyle.clearProperty("width");
		m_scrollContainer.getElement().setScrollLeft(0);
	}
	
	private void clearScrollbarY()
	{
		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
		Style mouseLayerStyle = this.m_mouseLayer.getElement().getStyle();
		
		scrollerStyle.setOverflowY(Overflow.HIDDEN);
		innerStyle.clearProperty("height");
		mouseLayerStyle.clearProperty("height");
		m_scrollContainer.getElement().setScrollTop(0);
	}
	
	private void setInitialScrollbarPosX(State_ViewingCell viewingState)
	{
		A_Grid grid = viewingState.getCell().getGrid();
		Point cameraPoint = m_viewContext.appContext.cameraMngr.getCamera().getPosition();
		Point centerPoint = m_utilPoint1;
		U_CameraViewport.calcViewWindowCenter(grid, viewingState.getCell().getCoordinate(), m_cellHudHeight, centerPoint);
		double minViewWidth = U_CameraViewport.calcCellWidthRequirement(grid);
		double windowWidth = this.getWindowWidth();
		
		double delta = (minViewWidth - windowWidth)/2;
		double cameraPos = U_Math.clamp(cameraPoint.getX(), centerPoint.getX()-delta, centerPoint.getX()+delta);
		double scroll = (cameraPos - (centerPoint.getX()-delta));
		m_scrollContainer.getElement().setScrollLeft((int) Math.round(scroll));
	}
	
	private void setInitialScrollbarPosY(State_ViewingCell viewingState)
	{
		A_Grid grid = viewingState.getCell().getGrid();
		Point cameraPoint = m_viewContext.appContext.cameraMngr.getCamera().getPosition();
		Point centerPoint = m_utilPoint1;
		U_CameraViewport.calcViewWindowCenter(grid, viewingState.getCell().getCoordinate(), m_cellHudHeight, centerPoint);
		double minViewHeight = U_CameraViewport.calcCellHeightRequirement(grid, m_cellHudHeight);
		double windowHeight = this.getWindowHeight();
		
		double delta = (minViewHeight - windowHeight)/2;
		double cameraPos = U_Math.clamp(cameraPoint.getY(), centerPoint.getY()-delta, centerPoint.getY()+delta);
		double scroll = (cameraPos - (centerPoint.getY()-delta));
		int scrollRounded = (int) Math.round(scroll);
		m_scrollContainer.getElement().setScrollTop(scrollRounded);
	}
	
	private void putScrollBarX(double size)
	{
		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
		Style mouseLayerStyle = this.m_mouseLayer.getElement().getStyle();
		
		scrollerStyle.setOverflowX(Overflow.SCROLL);
		String widthProperty = size+"px";
		innerStyle.setProperty("width", widthProperty);
		mouseLayerStyle.setProperty("width", widthProperty);
	}
	
	private void putScrollBarY(double size)
	{
		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
		Style mouseLayerStyle = this.m_mouseLayer.getElement().getStyle();
		
		scrollerStyle.setOverflowY(Overflow.SCROLL);
		String heightProperty = size+"px";
		innerStyle.setProperty("height", heightProperty);
		mouseLayerStyle.setProperty("height", heightProperty);
	}
	
	private void toggleScrollBars(State_ViewingCell viewingState_nullable)
	{
		if( viewingState_nullable != null && viewingState_nullable.isEntered() )
		{			
			//A_Grid grid = viewingState_nullable.getCell().getGrid();
			GridCoordinate targetCoord = viewingState_nullable.getTargetCoord();
			//Point cameraPoint = m_viewContext.appContext.cameraMngr.getCamera().getPosition();
			//Point centerPoint = m_utilPoint1;
			//U_CameraViewport.calcViewWindowCenter(grid, viewingState_nullable.getCell().getCoordinate(), m_cellHudHeight, centerPoint);
			
//			this.calcFocusedLayout(targetCoord, m_utilPoint1, m_utilRect1, m_utilRect2, m_utilCellSize1, m_utilBool1, m_utilBool2);
			this.calcFocusedLayout(targetCoord, m_layout);
	
			if( m_layout.widthSmaller.value )
			{
				putScrollBarX(m_layout.cellSizePlusExtras.getWidth());
			}
			else
			{
				clearScrollbarX();
			}
			
			if( m_layout.heightSmaller.value )
			{
				putScrollBarY(m_layout.cellSizePlusExtras.getHeight());
			}
			else
			{
				clearScrollbarY();
			}
		}
		else
		{
			clearScrollbarX();
			clearScrollbarY();
		}
	}
	
	private double getWindowWidthSansScroll()
	{
		double value = m_scrollContainer.getElement().getOffsetWidth();
		
		if( value == 0 )
		{
			value = this.getWindowWidth();
		}
		
		return value;
	}
	
	private double getWindowHeightSansScroll()
	{
		double value = m_scrollContainer.getElement().getOffsetHeight();
		
		if( value == 0 )
		{
			value = this.getWindowHeight();
		}
		
		return value;
	}
	
	public double getWindowWidth()
	{
		double value = this.m_scrollContainer.getElement().getClientWidth();
		
		if( value == 0 )
		{
			if( m_scrollContainer.getElement().getOffsetWidth() != 0 )
			{
				return m_scrollContainer.getElement().getOffsetWidth();
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
	
	public double getWindowHeight()
	{
		double value = this.m_scrollContainer.getElement().getClientHeight();
		
		if( value == 0 )
		{
			if( m_scrollContainer.getElement().getOffsetHeight() != 0 )
			{
				return m_scrollContainer.getElement().getOffsetHeight();
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
		return this.m_scrollContainer.getElement().getScrollWidth() > this.m_scrollContainer.getElement().getClientWidth();
		/*if( m_currentGrid == null )  return false;
		
		double minViewWidth = U_CameraViewport.calcCellWidthRequirement(m_currentGrid);
		double windowWidth = this.getWindowWidth();
		
		return windowWidth < minViewWidth;*/
	}
	
	public boolean isScrollingY()
	{
		return this.m_scrollContainer.getElement().getScrollHeight() > this.m_scrollContainer.getElement().getClientHeight();
		
		/*if( m_currentGrid == null )  return false;
		
		double minViewHeight = U_CameraViewport.calcCellHeightRequirement(m_currentGrid, m_cellHudHeight);
		double windowHeight = this.getWindowHeight();
		
		return windowHeight < minViewHeight;*/
	}
	
	public void adjustTargetSnapPoint(Action_Camera_SnapToCoordinate.Args args)
	{
		if( args.get() == this.getClass() )   return;
		
		this.adjustTargetSnapPoint_private(args.getTargetCoordinate(), args.getTargetPoint());
	}
	
	public void adjustTargetSnapPoint_private(GridCoordinate targetCoord, Point point_out)
	{
		State_ViewingCell viewingState = m_viewContext.stateContext.getEntered(State_ViewingCell.class);
		if( viewingState != null )
		{
			if( !targetCoord.isEqualTo(viewingState.getCell().getCoordinate()) )
			{
				viewingState = null;
			}
		}
		
		A_Grid grid = this.m_viewContext.appContext.gridMngr.getGrid();
		
//		this.calcFocusedLayout(targetCoord, m_utilPoint1, m_utilRect1, m_utilRect2, m_utilCellSize1, m_utilBool1, m_utilBool2);
		this.calcFocusedLayout(targetCoord, m_layout);
		
		double newWindowWidth = m_layout.window.getWidth();
		double newWindowHeight = m_layout.window.getHeight();
		boolean widthSmaller = m_layout.widthSmaller.value;
		boolean heightSmaller = m_layout.heightSmaller.value;

		if( widthSmaller && heightSmaller )
		{
			if( viewingState == null )
			{
				double windowWidthSansScroll = this.getWindowWidthSansScroll();
				double windowHeightSansScroll = this.getWindowHeightSansScroll();
				double defaultCellWidthReq = U_CameraViewport.calcCellWidthRequirement(grid);
				double defaultCellHeightReq = U_CameraViewport.calcCellHeightRequirement(grid, m_cellHudHeight);
				
				if( windowHeightSansScroll > defaultCellHeightReq )
				{
					targetCoord.calcPoint(m_utilPoint2, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
					double paddingTop = U_CameraViewport.calcCellPaddingTop(grid, m_cellHudHeight);
					m_utilPoint2.incY(-paddingTop);
					m_utilPoint2.incY(newWindowHeight/2);
					m_utilPoint2.incY(m_scrollBarWidthDiv2);
					
					double offset = Math.min((m_layout.cellSizePlusExtras.getHeight() - defaultCellHeightReq)/2, (newWindowHeight - defaultCellHeightReq)/2);
					offset = Math.max(0, offset);
					m_utilPoint2.incY(-offset);
					point_out.setY(m_utilPoint2.getY());
				}
				
				if( windowWidthSansScroll > defaultCellWidthReq )
				{
					targetCoord.calcPoint(m_utilPoint2, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
					double paddingLeft = grid.getCellPadding();
					m_utilPoint2.incX(-paddingLeft);
					m_utilPoint2.incX(newWindowWidth/2);
					m_utilPoint2.incX(m_scrollBarWidthDiv2);

					double offset = Math.min((m_layout.cellSizePlusExtras.getWidth() - defaultCellWidthReq)/2, (newWindowWidth - defaultCellWidthReq)/2);
					offset = Math.max(0, offset);
					m_utilPoint2.incX(-offset);
					point_out.setX(m_utilPoint2.getX());
				}
			}
		}
		else if( widthSmaller )
		{
			if( viewingState == null )
			{
				point_out.incY(m_scrollBarWidthDiv2);
			}
			
			newWindowHeight = 0;
			
			U_CameraViewport.calcConstrainedCameraPoint(grid, targetCoord, point_out, newWindowWidth, newWindowHeight, m_cellHudHeight, point_out);
		}
		else if( heightSmaller )
		{
			if( viewingState == null )
			{
				point_out.incX(m_scrollBarWidthDiv2);
			}
			
			newWindowWidth = 0;
			
			U_CameraViewport.calcConstrainedCameraPoint(grid, targetCoord, point_out, newWindowWidth, newWindowHeight, m_cellHudHeight, point_out);
		}
	}
	
	@Override
	public void onStateEvent(StateEvent event)
	{
		switch(event.getType())
		{
			case DID_UPDATE:
			{
				/*if ( event.getState() instanceof State_ViewingCell )
				{
					if( event.getState().getUpdateCount() % 5 == 0 )
					{
						s_logger.severe(this.getScrollY() +"");
					}
				}*/
				
				break;
			}
			
			case DID_ENTER:
			{
				if ( event.getState() instanceof State_ViewingCell )
				{
					State_ViewingCell state = event.getState();
					
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
					
					m_scrollContainer.getElement().setScrollTop(0);
					m_scrollContainer.getElement().setScrollLeft(0);
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
						boolean maintainApparentPosition = true;
						this.updateCameraViewRect(true, maintainApparentPosition);
						
						if( event.getContext().isEntered(State_CameraSnapping.class) )
						{
							//--- DRK > Maintaining apparent position shifts (at least currently) the camera target,
							//---		so this makes sure we stay on track.
							this.onResize();
						}
					}
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if (event.isFor(Event_Camera_OnCellSizeFound.class) )
				{
					this.onResize();
				}
				
				//--- DRK > This case handled by the "visual cell pop" flow started by focuser.
				//---		Kinda sloppy.
//				else if( event.isFor(Event_CameraSnapping_OnTargetCellAppeared.class) )
//				{
//					BufferCell cell = U_Cell.getBufferCell(event);
//					
//					if( cell.getFocusedCellSize() != null && cell.getFocusedCellSize().hasNaturalDimension() )
//					{
//						this.onResize();
//					}
//				}
				
				break;
			}
		}
	}	
	
	private void setTargetLayout(GridCoordinate gridCoord)
	{
		CellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
		CellBuffer cellBuffer = cellManager.getDisplayBuffer();
		if( cellBuffer.isInBoundsAbsolute(gridCoord) )
		{
			BufferCell bufferCell = cellBuffer.getCellAtAbsoluteCoord(gridCoord);
			VisualCell visualCell = (VisualCell) bufferCell.getVisualization();
			this.setTargetLayout(visualCell);		
		}
	}
	
	public void setTargetLayout(VisualCell visualCell)
	{
		BufferCell bufferCell = visualCell.getBufferCell();
		
		this.calcFocusedLayout(bufferCell.getCoordinate(), m_layout);
		
		m_layout.topLeftOffset.incX(-this.getScrollX());
		m_layout.topLeftOffset.incY(-this.getScrollY());
		
		visualCell.setTargetLayout(m_layout.cellSize.getWidth(), m_layout.cellSize.getHeight(), (int)m_layout.topLeftOffset.getX(), (int)m_layout.topLeftOffset.getY());
	}
	
	
	
	private void calcCellSize(VisualCell visualCell, CellSize size_out)
	{
		BufferCell bufferCell = visualCell.getBufferCell();
		CellSize cellSize = bufferCell.getFocusedCellSize();
		
		size_out.copy(cellSize);
		
		int width = cellSize.getWidth();
		width = width == CellSize.NATURAL_DIMENSION ? visualCell.calcNaturalWidth() : width;
		
		int height = cellSize.getHeight();
		height = height == CellSize.NATURAL_DIMENSION ? visualCell.calcNaturalHeight() : height;
		
		size_out.setExplicit(width, height);
	}	
	
	private void calcCellSize(GridCoordinate coord, CellSize size_out)
	{
		A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
		
		CellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
		CellBuffer cellBuffer = cellManager.getDisplayBuffer();
		if( cellBuffer.isInBoundsAbsolute(coord) )
		{
			BufferCell bufferCell = cellBuffer.getCellAtAbsoluteCoord(coord);
			VisualCell visualCell = (VisualCell) bufferCell.getVisualization();
			
			calcCellSize(visualCell, size_out);
			
			return;
		}
		else
		{
			m_utilMapping.getCoordinate().copy(coord);
			if( m_viewContext.appContext.cellSizeMngr.getCellSizeFromLocalSource(m_utilMapping, size_out) )
			{
				size_out.setIfNatural(grid.getCellWidth(), grid.getCellHeight());
				
				return;
			}
		}
		
		size_out.setExplicit(grid.getCellWidth(), grid.getCellHeight());
	}
	
	private void calcTopLeftOffset(CellSize cellSize, Rect windowSize, Vector topLeftOffset_out)
	{
		A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
		double defaultWidthReq = U_CameraViewport.calcCellWidthRequirement(grid);
		double defaultHeightReq = U_CameraViewport.calcCellHeightRequirement(grid, m_cellHudHeight);
		double roomToTheLeft = (windowSize.getWidth() - defaultWidthReq)/2;
		roomToTheLeft = Math.max(roomToTheLeft, 0);
		double roomToTheTop = (windowSize.getHeight() - defaultHeightReq)/2;
		roomToTheTop = Math.max(roomToTheTop, 0);
		
		int targetWidth = cellSize.getWidth();
		int targetHeight = cellSize.getHeight();
		int defaultWidth = grid.getCellWidth();
		int defaultHeight = grid.getCellHeight();
		
		int sideWidth = (targetWidth - defaultWidth)/2;
		int xOffset = (int) -Math.min(sideWidth, roomToTheLeft);
		int topHeight = (targetHeight - defaultHeight)/2;
		int yOffset = (int) -Math.min(topHeight, roomToTheTop);
		
		topLeftOffset_out.set(xOffset, yOffset, 0);
	}
	
	public void calcFocusedLayout(GridCoordinate coord, FocusedLayout layout_out)
	{
		A_Grid grid = this.m_viewContext.appContext.gridMngr.getGrid();

		this.calcCellSize(coord, layout_out.cellSize);
		double totalCellWidthReq = U_CameraViewport.calcCellWidthRequirement(grid, layout_out.cellSize.getWidth());
		double totalCellHeightReq = U_CameraViewport.calcCellHeightRequirement(grid, layout_out.cellSize.getHeight(), m_cellHudHeight);
		layout_out.cellSizePlusExtras.set(totalCellWidthReq, totalCellHeightReq);
		
		layout_out.window.set(this.getWindowWidthSansScroll(), this.getWindowHeightSansScroll());
		
		this.calcTopLeftOffset(layout_out.cellSize, layout_out.window, layout_out.topLeftOffset);

		layout_out.widthSmaller.value = false;
		layout_out.heightSmaller.value = false;
		
		if( layout_out.window.getWidth() < totalCellWidthReq )
		{
			layout_out.window.incHeight(-m_scrollBarWidthDiv2*2);
			layout_out.widthSmaller.value = true;
		}

		if( layout_out.window.getHeight() < totalCellHeightReq )
		{
			layout_out.window.incWidth(-m_scrollBarWidthDiv2*2);
			layout_out.heightSmaller.value = true;
		}
		
		if( !layout_out.widthSmaller.value && layout_out.window.getWidth() < totalCellWidthReq )
		{
			layout_out.window.incHeight(-m_scrollBarWidthDiv2*2);			
			layout_out.widthSmaller.value = true;
		}
	}
}