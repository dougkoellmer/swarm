package swarm.client.view.cell;

import java.util.logging.Logger;

import swarm.client.app.smClientAppConfig;
import swarm.client.app.smAppContext;
import swarm.client.entities.smCamera;
import swarm.client.entities.smBufferCell;
import swarm.client.entities.smE_CodeStatus;
import swarm.client.input.smClickManager;
import swarm.client.input.smI_ClickHandler;
import swarm.client.navigation.smBrowserNavigator;
import swarm.client.navigation.smU_CameraViewport;
import swarm.client.states.camera.Action_Camera_SnapToPoint;
import swarm.client.states.camera.Action_Camera_SetViewSize;
import swarm.client.states.camera.Action_Camera_SnapToAddress;
import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
import swarm.client.states.camera.Action_ViewingCell_Refresh;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.states.code.Action_EditingCode_Preview;
import swarm.client.states.code.Action_EditingCode_Save;
import swarm.client.view.smE_ZIndex;
import swarm.client.view.smI_UIElement;
import swarm.client.view.smU_Css;
import swarm.client.view.smViewContext;
import swarm.client.view.tooltip.smE_ToolTipType;
import swarm.client.view.tooltip.smToolTipConfig;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.client.view.widget.smSpriteButton;
import swarm.shared.app.smS_App;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smA_Grid;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smE_StateTimeType;
import swarm.shared.statemachine.smStateEvent;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;
import swarm.shared.utils.smU_Math;

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

public class smVisualCellHud extends FlowPanel implements smI_UIElement
{
	private static final Logger s_logger = Logger.getLogger(smVisualCellHud.class.getName());
	
	private static final smPoint s_utilPoint1 = new smPoint();
	private static final smPoint s_utilPoint2 = new smPoint();
	
	private class smHudButton extends smSpriteButton
	{
		private smHudButton(String spriteId)
		{
			super(spriteId);
			
			this.addStyleName("sm_hud_button");
		}
	}
	
	private final HorizontalPanel m_innerContainer = new HorizontalPanel();
	private final HorizontalPanel m_leftDock = new HorizontalPanel();
	private final HorizontalPanel m_rightDock = new HorizontalPanel();
	
	private final smHudButton m_back		= new smHudButton("back");
	private final smHudButton m_forward		= new smHudButton("forward");
	private final smHudButton m_refresh		= new smHudButton("refresh");
	private final smHudButton m_close		= new smHudButton("close");

	private boolean m_waitingForBeingRefreshableAgain = false;
	
	private final smViewContext m_viewContext;
	
	private final smClientAppConfig m_appConfig;
	
	private final Action_Camera_SnapToPoint.Args m_args_SetCameraTarget = new Action_Camera_SnapToPoint.Args();
	
	private final double m_minWidth = 169;// TODO: GHETTO
	private double m_width = 0;
	
	private double m_baseAlpha;
	private double m_alpha;
	private final double m_fadeOutTime_seconds;
	
	private double m_lastScaling = -1;
	private final smGridCoordinate m_lastCoordViewed = new smGridCoordinate();
	
	public smVisualCellHud(smViewContext viewContext, smClientAppConfig appConfig)
	{
		m_viewContext = viewContext;
		
		m_appConfig = appConfig;
		m_fadeOutTime_seconds = m_viewContext.viewConfig.hudFadeOutTime_seconds;
		
		m_alpha = m_baseAlpha = 0.0;
		
		this.addStyleName("sm_cell_hud");
		
		smE_ZIndex.CELL_HUD.assignTo(this);
		
		this.setVisible(false);
		
		
		m_innerContainer.setWidth("100%");
		this.getElement().getStyle().setProperty("minWidth", m_minWidth + "px");
		smU_Css.setTransformOrigin(this.getElement(), "0% 0%");
		
		m_innerContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		m_leftDock.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		m_rightDock.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		m_leftDock.add(m_back);
		m_leftDock.add(m_forward);
		m_leftDock.add(m_refresh);
		
		m_rightDock.add(m_close);
		
		m_innerContainer.add(m_leftDock);
		m_innerContainer.add(m_rightDock);
		
		m_innerContainer.setCellHorizontalAlignment(m_rightDock, HasHorizontalAlignment.ALIGN_RIGHT);
		
		this.add(m_innerContainer);
		
		m_viewContext.clickMngr.addClickHandler(m_back, new smI_ClickHandler()
		{
			@Override
			public void onClick(int x, int y)
			{
				if( !m_back.isEnabled() )  return;
				
				m_viewContext.browserNavigator.go(-1);
			}
		});
		
		m_viewContext.clickMngr.addClickHandler(m_forward, new smI_ClickHandler()
		{
			@Override
			public void onClick(int x, int y)
			{
				if( !m_forward.isEnabled() )  return;
				
				m_viewContext.browserNavigator.go(1);
			}
		});
		
		m_viewContext.clickMngr.addClickHandler(m_refresh, new smI_ClickHandler()
		{
			@Override
			public void onClick(int x, int y)
			{
				if( !m_refresh.isEnabled() )  return;
				
				smVisualCellHud.this.m_viewContext.cellMngr.clearAlerts();
				
				m_viewContext.stateContext.performAction(Action_ViewingCell_Refresh.class);
			}
		});
		
		m_viewContext.clickMngr.addClickHandler(m_close, new smI_ClickHandler()
		{
			@Override
			public void onClick(int x, int y)
			{
				if( !m_close.isEnabled() )  return;

				s_utilPoint1.copy(smVisualCellHud.this.m_viewContext.appContext.cameraMngr.getCamera().getPosition());
				s_utilPoint1.incZ(m_appConfig.backOffDistance);
				
				m_args_SetCameraTarget.init(s_utilPoint1, false, true);
				m_viewContext.stateContext.performAction(Action_Camera_SnapToPoint.class, m_args_SetCameraTarget);
			}
		});
		
		smToolTipManager toolTipper = m_viewContext.toolTipMngr;
		
		toolTipper.addTip(m_back, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Go back."));
		toolTipper.addTip(m_forward, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Go forward."));
		toolTipper.addTip(m_refresh, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Refresh this cell."));
		toolTipper.addTip(m_close, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Back off."));
	}
	
	private void setAlpha(double alpha)
	{
		m_alpha = alpha <= 0 ? 0 : alpha;
		//s_logger.severe(m_alpha + "");
		this.getElement().getStyle().setOpacity(m_alpha);
	}
	
	private void updateCloseButton()
	{
		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
		State_CameraSnapping snappingState = m_viewContext.stateContext.getEnteredState(State_CameraSnapping.class);
		
		if( viewingState != null || snappingState != null && snappingState.getPreviousState() == State_ViewingCell.class )
		{
			m_close.setEnabled(true);
		}
		else
		{
			m_close.setEnabled(false);
		}
	}
	
	private void updateRefreshButton()
	{
		boolean canRefresh = m_viewContext.stateContext.isActionPerformable(Action_ViewingCell_Refresh.class);
		m_refresh.setEnabled(canRefresh);
		
		if( !canRefresh )
		{
			m_waitingForBeingRefreshableAgain = true;
		}
		else
		{
			m_waitingForBeingRefreshableAgain = false;
		}
	}
	
	private void updatePosition(smA_Grid grid, smGridCoordinate coord)
	{
		if( m_lastCoordViewed != coord)
		{
			m_lastCoordViewed.copy(coord);
		}
		
		Element scrollElement = this.getParent().getElement();
		double scrollX = scrollElement.getScrollLeft();
		double scrollY = scrollElement.getScrollTop();
		
		smCamera camera = m_viewContext.appContext.cameraMngr.getCamera();
		
		coord.calcPoint(s_utilPoint1, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
		camera.calcScreenPoint(s_utilPoint1, s_utilPoint2);
		double x = s_utilPoint2.getX() + scrollX*2;
		
		double viewWidth = getViewWidth(camera, grid);
		double hudWidth = Math.max(m_width, m_minWidth);
		if( hudWidth > viewWidth )
		{
			double scrollWidth = scrollElement.getScrollWidth();
			double clientWidth = scrollElement.getClientWidth();
			double diff = (hudWidth - viewWidth) +  smU_CameraViewport.getViewPadding(grid)/2.0;
			double scrollRatio = scrollX / (scrollWidth-clientWidth);
			x -= diff * scrollRatio;
		}
		
		
		double scaling = m_viewContext.cellMngr.getLastScaling();
		double y = s_utilPoint2.getY()-(m_appConfig.cellHudHeight+grid.getCellPadding())*scaling;
		y -= 1*scaling; // account for margin...sigh
		y += scrollY * scaling;
		
		
		boolean has3dTransforms = m_viewContext.appContext.platformInfo.has3dTransforms();
		String translateProperty = smU_Css.createTranslateTransform(x, y, has3dTransforms);
		String scaleProperty = smU_Css.createScaleTransform(scaling, has3dTransforms);
		smU_Css.setTransform(this.getElement(), translateProperty + " " + scaleProperty);
	}
	
	private void updatePosition(State_CameraSnapping state)
	{
		smA_Grid grid = m_viewContext.appContext.gridMngr.getGrid(); // TODO: Can be more than one grid in the future
		smGridCoordinate coord = state.getTargetCoordinate();
		
		this.updatePosition(grid, coord);
	}
	
	private void updatePosition(State_ViewingCell state)
	{
		smBufferCell cell = ((State_ViewingCell)state).getCell();
		smA_Grid grid = cell.getGrid();
		smGridCoordinate coord = cell.getCoordinate();
		
		this.updatePosition(grid, coord);
	}
	
	private void updateHistoryButtons()
	{
		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
		State_CameraSnapping snappingState = m_viewContext.stateContext.getEnteredState(State_CameraSnapping.class);
		
		if( viewingState != null || snappingState != null && snappingState.getPreviousState() == State_ViewingCell.class )
		{
			m_back.setEnabled(m_viewContext.browserNavigator.hasBack());
			m_forward.setEnabled(m_viewContext.browserNavigator.hasForward());
		}
		else
		{
			m_back.setEnabled(false);
			m_forward.setEnabled(false);
		}
	}
	
	private double getViewWidth(smCamera camera, smA_Grid grid)
	{
		double viewPadding = smU_CameraViewport.getViewPadding(grid);
		double viewWidth = camera.getViewWidth() - viewPadding*2;
		
		return viewWidth;
	}
	
	private void updateSize(State_ViewingCell state)
	{
		smA_Grid grid = state.getCell().getGrid();
		smCamera camera = m_viewContext.appContext.cameraMngr.getCamera();
		
		double viewWidth = getViewWidth(camera, grid);
		double cellWidth = grid.getCellWidth();
		double hudWidth = Math.min(viewWidth, cellWidth);
		
		m_width = hudWidth - 9; // TODO: GHETTO...takes into account padding or something.
		//m_width = Math.max(m_width, m_minWidth);
		
		//s_logger.severe("hud width: " + m_width);
		
		this.getElement().getStyle().setWidth(m_width, Unit.PX);
	}
	
	@Override
	public void onStateEvent(smStateEvent event)
	{
		switch( event.getType() )
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_ViewingCell )
				{
					this.updateSize((State_ViewingCell) event.getState());
					this.updatePosition((State_ViewingCell) event.getState());
					this.updateCloseButton();
					this.updateHistoryButtons();
					
					this.setAlpha(1);
					m_baseAlpha = m_alpha;
				}
				else if( event.getState() instanceof State_CameraSnapping )
				{
					if( event.getState().getPreviousState() != State_ViewingCell.class )
					{
						this.updatePosition((State_CameraSnapping)event.getState());
						
						this.setVisible(true);
						m_baseAlpha = m_alpha;
						
					//	s_logger.severe("BASE ALPHA: " + m_baseAlpha);
					}
					
					this.updateCloseButton();
					this.updateHistoryButtons();
				}
				
				break;
			}
			
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof State_ViewingCell )
				{
					this.updateHistoryButtons();
					this.updateRefreshButton();
				}
				
				break;
			}
			
			case DID_UPDATE:
			{
				if( event.getState().getParent() instanceof StateMachine_Camera )
				{
					if( event.getState() instanceof State_ViewingCell )
					{
						State_ViewingCell viewingState = (State_ViewingCell) event.getState();
						if( m_waitingForBeingRefreshableAgain )
						{
							if( viewingState.isForegrounded() )
							{
								updateRefreshButton();
							}
						}
					}
					else if( event.getState() instanceof State_CameraSnapping )
					{
						State_CameraSnapping cameraSnapping = m_viewContext.stateContext.getEnteredState(State_CameraSnapping.class);
						if( cameraSnapping != null && cameraSnapping.getPreviousState() != State_ViewingCell.class )
						{
							//s_logger.severe(cameraSnapping.getOverallSnapProgress() + "");
							this.setAlpha(m_baseAlpha + (1-m_baseAlpha) * cameraSnapping.getOverallSnapProgress());
							//this.setAlpha((1 - m_baseAlpha) * (1 - ));
							
							this.updatePosition((State_CameraSnapping)event.getState());
						}
					}
					else if( !(event.getState() instanceof State_ViewingCell) )
					{
						if( m_alpha <= 1 )
						{
							if( event.getState().isEntered() )
							{
								if( m_alpha > 0 )
								{
									double timeMantissa = event.getState().getTimeInState(smE_StateTimeType.TOTAL) / m_fadeOutTime_seconds;
									timeMantissa = smU_Math.clamp(timeMantissa, 0, 1);
									
									this.setAlpha(m_baseAlpha * (1-timeMantissa));
									smA_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
									this.updatePosition(grid, m_lastCoordViewed);
									
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
				if( event.getState() instanceof State_ViewingCell )
				{					
					m_waitingForBeingRefreshableAgain = false;
					
					this.updateCloseButton();
					this.updateHistoryButtons();
					this.updateRefreshButton();
					
					m_baseAlpha = m_alpha;
				}
				else if( event.getState() instanceof State_CameraSnapping )
				{
					m_baseAlpha = m_alpha;
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == Action_Camera_SetViewSize.class )
				{
					State_ViewingCell state = event.getContext().getEnteredState(State_ViewingCell.class);
					
					if( state != null )
					{
						this.updateSize(state);
						
						Action_Camera_SetViewSize.Args args = event.getActionArgs();
						if( args.updateBuffer() )
						{
							this.updatePosition(state);
						}
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
							this.updatePosition(state);
						}
					}
				}
				else if((	event.getAction() == Action_ViewingCell_Refresh.class		||
							event.getAction() == Action_EditingCode_Save.class			||
							event.getAction() == Action_EditingCode_Preview.class		||
				
							//--- DRK > These two cover the case of if we snap to a cell that we're already visiting.
							//---		This effectively refreshes the cell in this case.
							event.getAction() == Action_Camera_SnapToAddress.class		||
							event.getAction() == Action_Camera_SnapToCoordinate.class	))
				{
					updateRefreshButton();
				}
				
				break;
			}
		}
	}
}
