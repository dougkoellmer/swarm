package swarm.client.view.cell;

import java.util.logging.Logger;

import swarm.client.app.ClientAppConfig;
import swarm.client.app.AppContext;
import swarm.client.entities.Camera;
import swarm.client.entities.BufferCell;
import swarm.client.entities.E_CodeStatus;
import swarm.client.input.ClickManager;
import swarm.client.input.I_ClickHandler;
import swarm.client.managers.CameraManager;
import swarm.client.managers.CellBuffer;
import swarm.client.managers.CellBufferManager;
import swarm.client.navigation.BrowserNavigator;
import swarm.client.navigation.BrowserNavigator.I_StateChangeListener;
import swarm.client.navigation.U_CameraViewport;
import swarm.client.states.camera.Action_Camera_SnapToPoint;
import swarm.client.states.camera.Action_Camera_SetViewSize;
import swarm.client.states.camera.Action_Camera_SnapToAddress;
import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
import swarm.client.states.camera.Action_ViewingCell_Refresh;
import swarm.client.states.camera.Event_Camera_OnCellSizeFound;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_CameraFloating;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_GettingMapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.states.code.Action_EditingCode_Preview;
import swarm.client.states.code.Action_EditingCode_Save;
import swarm.client.view.E_ZIndex;
import swarm.client.view.I_UIElement;
import swarm.client.view.U_Css;
import swarm.client.view.ViewContext;
import swarm.client.view.tooltip.E_ToolTipType;
import swarm.client.view.tooltip.ToolTipConfig;
import swarm.client.view.tooltip.ToolTipManager;
import swarm.client.view.widget.SpriteButton;
import swarm.shared.app.S_CommonApp;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Grid;
import swarm.shared.entities.E_CodeType;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.E_StateTimeType;
import swarm.shared.statemachine.StateEvent;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.CellSize;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;
import swarm.shared.structs.Rect;
import swarm.shared.structs.Vector;
import swarm.shared.utils.U_Math;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

public class VisualCellHud extends FlowPanel implements I_UIElement
{
	private static final Logger s_logger = Logger.getLogger(VisualCellHud.class.getName());
	
	private static final Point s_utilPoint1 = new Point();
	private static final Point s_utilPoint2 = new Point();
	private static final Vector s_utilVector = new Vector();
	
	private final ViewContext m_viewContext;
	
	private final ClientAppConfig m_appConfig;
	
	private final double m_minWidth = 169;// TODO: GHETTO
	private double m_width = 0;
	private double m_baseWidth = 0;
	private double m_targetWidth = 0;
	private double m_baseProgressValue = 0;
	
	private double m_baseAlpha;
	private double m_alpha;
	private final double m_fadeOutTime_seconds;
	
	private String m_lastTranslation = "";
	private final Point m_lastWorldPositionOnDoubleSnap = new Point();
	private final Point m_lastWorldPosition = new Point();
	
	private final CellAddressMapping m_utilMapping = new CellAddressMapping();
	private final Rect m_utilRect = new Rect();
	private final CellSize m_utilCellSize = new CellSize();
	
	private final GridCoordinate m_lastTargetCoord = new GridCoordinate();
	
	private final VisualCellHudInner m_innerContainer;
	
	public VisualCellHud(ViewContext viewContext, ClientAppConfig appConfig)
	{
		m_innerContainer = new VisualCellHudInner(viewContext);
		
		m_viewContext = viewContext;
		
		m_appConfig = appConfig;
		m_fadeOutTime_seconds = m_viewContext.config.hudFadeOutTime_seconds;
		
		m_alpha = m_baseAlpha = 0.0;
		
		this.addStyleName("sm_cell_hud");
		
		E_ZIndex.CELL_HUD.assignTo(this);
		
		this.setVisible(false);
		this.getElement().getStyle().setProperty("minWidth", m_minWidth + "px");
		U_Css.setTransformOrigin(this.getElement(), "0% 0%");
		
		this.add(m_innerContainer);
	}
	
	private void setAlpha(double alpha)
	{
		m_alpha = alpha <= 0 ? 0 : alpha;
		this.getElement().getStyle().setOpacity(m_alpha);
	}
	
	private void updatePositionFromScreenPoint(A_Grid grid, Point screenPoint)
	{
		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
		s_utilPoint1.copy(screenPoint);
		double xOffset = 0;//U_CameraViewport.calcXOffset((int) Math.ceil(m_width), grid.getCellWidth());
		
		if( m_viewContext.stateContext.isEntered(State_ViewingCell.class) )
		{
			Element scrollElement = this.getParent().getElement();
			double scrollX = scrollElement.getScrollLeft();
			s_utilPoint1.incX(-scrollX);
			//s_utilPoint1.incX(xOffset);
		}
		
		screenPoint.incX(xOffset);
		camera.calcWorldPoint(s_utilPoint1, m_lastWorldPosition);
		
		boolean has3dTransforms = m_viewContext.appContext.platformInfo.has3dTransforms();
		m_lastTranslation = U_Css.createTranslateTransform(screenPoint.getX(), screenPoint.getY(), has3dTransforms);
		
		double scaling = m_viewContext.cellMngr.getLastScaling();
		String scaleProperty = U_Css.createScaleTransform(scaling, has3dTransforms);
		U_Css.setTransform(this.getElement(), m_lastTranslation + " " + scaleProperty);
	}
	
	private void updatePositionFromWorldPoint(A_Grid grid, Point worldPoint)
	{
		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
		
		camera.calcScreenPoint(worldPoint, s_utilPoint2);
		
		this.updatePositionFromScreenPoint(grid, s_utilPoint2);
	}
	
	private void updatePosition(A_Grid grid, GridCoordinate coord)
	{		
		this.calcScreenPositionFromCoord(grid, coord, s_utilPoint2);
		
		this.updatePositionFromScreenPoint(grid, s_utilPoint2);
	}
	
	private double calcScrollOffset(A_Grid grid)
	{
		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
		Element scrollElement = this.getParent().getElement();
		double scrollX = scrollElement.getScrollLeft();
		double toReturn = 0;
		
		double viewWidth = getViewWidth(camera, grid);
		double hudWidth = Math.max(m_width, m_minWidth);
		if( hudWidth > viewWidth && m_viewContext.stateContext.isEntered(State_ViewingCell.class) )
		{
			double scrollWidth = scrollElement.getScrollWidth();
			double clientWidth = scrollElement.getClientWidth();
			double diff = (hudWidth - viewWidth) +  U_CameraViewport.getViewPadding(grid)/2.0;
			double scrollRatio = scrollX / (scrollWidth-clientWidth);
			toReturn -= diff * scrollRatio;
		}
		
		return toReturn;
	}
	
	private void calcScreenPositionFromCoord(A_Grid grid, GridCoordinate coord, Point point_out)
	{
		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
		
		coord.calcPoint(s_utilPoint1, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
		camera.calcScreenPoint(s_utilPoint1, point_out);

		Element scrollElement = this.getParent().getElement();
		double scrollX = scrollElement.getScrollLeft();
		double scrollY = scrollElement.getScrollTop();
		
		double x = (point_out.getX() + scrollX*2) + this.calcScrollOffset(grid);
		
		double scaling = m_viewContext.cellMngr.getLastScaling();
		double y = point_out.getY()-(m_appConfig.cellHudHeight+grid.getCellPadding())*scaling;
		y -= 1*scaling; // account for margin...sigh, TODO: Shouldn't be using arbitrary constant here.
		y += scrollY * scaling;
		
		point_out.set(x, y, 0);
	}
	
	private void updatePositionFromState(State_CameraSnapping state)
	{
		A_Grid grid = m_viewContext.appContext.gridMngr.getGrid(); // TODO: Can be more than one grid in the future
		GridCoordinate coord = state.getTargetCoordinate();
		
		this.updatePosition(grid, coord);
	}
	
	private void updatePositionFromState(State_ViewingCell state)
	{
		BufferCell cell = ((State_ViewingCell)state).getCell();
		A_Grid grid = cell.getGrid();
		GridCoordinate coord = cell.getCoordinate();
		
		this.updatePosition(grid, coord);
	}
	
	private double getViewWidth(Camera camera, A_Grid grid)
	{
		m_viewContext.scrollNavigator.getScrollableWindow(m_utilRect);
		double viewPadding = U_CameraViewport.getViewPadding(grid);
		double viewWidth = m_utilRect.getWidth() - viewPadding*2;
		
		return viewWidth;
	}
	
	private void onDoubleSnap()
	{
		m_lastWorldPositionOnDoubleSnap.copy(m_lastWorldPosition);
	}
	
	@Override
	public void onStateEvent(StateEvent event)
	{
		switch( event.getType() )
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_CameraSnapping )
				{
					if( event.getState().getPreviousState() != State_ViewingCell.class )
					{						
						this.setVisible(true);
						m_baseAlpha = m_alpha;
					}
				}
				else if( event.getState() instanceof State_ViewingCell )
				{
					this.ensureTargetWidth();
					this.flushWidth();
					
					this.updatePositionFromState((State_ViewingCell) event.getState());
					
					this.setAlpha(1);
					m_baseAlpha = m_alpha;
				}
				else if( event.getState() instanceof State_CameraFloating )
				{
					A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
					this.setTargetWidth(grid.getCellWidth());
				}
				
				break;
			}
			
			case DID_UPDATE:
			{
				if( event.getState().getParent() instanceof StateMachine_Camera )
				{
					if( event.getState() instanceof State_CameraSnapping )
					{
						State_CameraSnapping cameraSnapping = event.getState();
						if( cameraSnapping.isEntered() )
						{
							if( cameraSnapping.getPreviousState() != State_ViewingCell.class )
							{
								this.setAlpha(m_baseAlpha + (1-m_baseAlpha) * cameraSnapping.getOverallSnapProgress());
							}
							
							this.updateWidth();
					
							if( cameraSnapping.getPreviousState() != State_ViewingCell.class)
							{
								this.updatePositionFromState((State_CameraSnapping)event.getState());
							}
							else
							{
								A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
								GridCoordinate coord = cameraSnapping.getTargetCoordinate();
								CameraManager cameraMngr = m_viewContext.appContext.cameraMngr;
								Camera camera = cameraMngr.getCamera();
								
								this.calcScreenPositionFromCoord(grid, coord, s_utilPoint2);
								camera.calcWorldPoint(s_utilPoint2, s_utilPoint2);
								s_utilPoint2.calcDifference(m_lastWorldPositionOnDoubleSnap, s_utilVector);
								double progress = cameraMngr.getWeightedSnapProgress();
								s_utilVector.scaleByNumber(progress);
								s_utilPoint2.copy(m_lastWorldPositionOnDoubleSnap);
								s_utilPoint2.translate(s_utilVector);
								
								this.updatePositionFromWorldPoint(grid, s_utilPoint2);
							}
						}
					}
					else if( event.getState() instanceof State_CameraFloating )
					{
						if( m_alpha <= 1 )
						{
							if( event.getState().isEntered() )
							{
								if( m_alpha > 0 )
								{
									A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
									double timeMantissa = event.getState().getTimeInState(E_StateTimeType.TOTAL) / m_fadeOutTime_seconds;
									timeMantissa = U_Math.clamp(timeMantissa, 0, 1);
									this.setAlpha(m_baseAlpha * (1-timeMantissa));
									this.updateWidth();
									this.updatePositionFromWorldPoint(grid, m_lastWorldPosition);
									
									if( m_alpha <= 0 )
									{
										this.setVisible(false);
									}
								}
							}
						}
					}
				}
				
				break;
			}
			
			case DID_EXIT:
			{
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == Action_Camera_SetViewSize.class )
				{
					State_ViewingCell viewingState = event.getContext().getEnteredState(State_ViewingCell.class);
					State_CameraSnapping snappingState = event.getContext().getEnteredState(State_CameraSnapping.class);
					
					if( viewingState != null )
					{
						this.setTargetWidth(viewingState.getCell().getCoordinate());
						this.ensureTargetWidth();
						this.flushWidth();
						
						Action_Camera_SetViewSize.Args args = event.getActionArgs();
						if( args.updateBuffer() )
						{
							this.updatePositionFromState(viewingState);
						}
					}
					else if( snappingState != null )
					{
						this.setTargetWidth(snappingState.getTargetCoordinate());
						
						this.onDoubleSnap();
					}
				}
				else if( event.getAction() == Action_Camera_SnapToPoint.class )
				{
					State_ViewingCell state = event.getContext().getEnteredState(State_ViewingCell.class);
					
					if( state != null )
					{
						Action_Camera_SnapToPoint.Args args = event.getActionArgs();
						if( args.isInstant() )
						{
							this.updatePositionFromState(state);
						}
					}
				}
				else if( event.getAction() == Action_Camera_SnapToCoordinate.class )
				{
					State_CameraSnapping state = event.getContext().getEnteredState(State_CameraSnapping.class);
					Action_Camera_SnapToCoordinate.Args args = event.getActionArgs();
					m_lastTargetCoord.copy(args.getTargetCoordinate());
					
					if( m_alpha <= 0 )
					{
						//--- Ensure that cell hud is the default size of grid cells.
						A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
						this.setTargetWidth(grid.getCellWidth());
						this.ensureTargetWidth();
						this.flushWidth();
						
						this.updatePositionFromState(state);
					}

					this.setTargetWidth(args.getTargetCoordinate());
					
					this.onDoubleSnap();
				}
				else if( event.getAction() == Event_Camera_OnCellSizeFound.class )
				{
					Event_Camera_OnCellSizeFound.Args args = event.getActionArgs();
					this.setTargetWidth(args.getCellSize().getWidth());
					
					if( event.getContext().isEntered(State_ViewingCell.class) )
					{
						this.ensureTargetWidth();
						this.flushWidth();
					}
				}
				
				break;
			}
		}
		
		m_innerContainer.onStateEvent(event);
	}
	
	private void flushWidth()
	{
		this.getElement().getStyle().setWidth(m_width, Unit.PX);
	}
	
	private void updateWidth()
	{
		if( m_width == m_targetWidth )  return;
		
		double mantissa = 0;
		
		State_CameraFloating floatingState = m_viewContext.stateContext.getEnteredState(State_CameraFloating.class);
		if( floatingState != null )
		{
			m_baseProgressValue += floatingState.getLastTimeStep();
			mantissa = m_baseProgressValue / m_viewContext.config.cellSizeChangeTime_seconds;
		}
		else
		{
			double snapProgress = m_viewContext.appContext.cameraMngr.getWeightedSnapProgress();
			mantissa = m_baseProgressValue == 1 ? 1 : (snapProgress - m_baseProgressValue) / (1-m_baseProgressValue);
			mantissa = U_Math.clampMantissa(mantissa);
		}
		
		double widthDelta = (m_targetWidth - m_baseWidth) * mantissa;
		m_width = (int) (m_baseWidth + widthDelta);
		
		if( mantissa >= 1 )
		{
			this.ensureTargetWidth();
		}
		
		this.flushWidth();
	}
	
	private void setTargetWidth(GridCoordinate coord)
	{
		m_utilMapping.getCoordinate().copy(coord);
		if( m_viewContext.appContext.cellSizeMngr.getCellSizeFromLocalSource(m_utilMapping, m_utilCellSize) )
		{
			this.setTargetWidth(m_utilCellSize.getWidth());
		}
		else
		{
			A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
			//this.setTargetWidth(grid.getCellWidth());
		}
	}
	
	private void setTargetWidth(double width)
	{
		if( m_viewContext.stateContext.isEntered(State_CameraFloating.class) )
		{
			m_baseProgressValue = 0;
		}
		else
		{
			m_baseProgressValue = m_viewContext.appContext.cameraMngr.getWeightedSnapProgress();
		}
		
		A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
		double viewWidth = getViewWidth(camera, grid);
		m_targetWidth = Math.min(viewWidth, width);
		m_baseWidth = m_width;
	}
	
	private void ensureTargetWidth()
	{
		m_width = m_baseWidth = m_targetWidth;
	}
}
