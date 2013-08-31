package swarm.client.view.cell;

import swarm.client.app.smClientAppConfig;
import swarm.client.app.smAppContext;
import swarm.client.entities.smCamera;
import swarm.client.entities.smBufferCell;
import swarm.client.entities.smE_CodeStatus;
import swarm.client.input.smClickManager;
import swarm.client.input.smI_ClickHandler;
import swarm.client.navigation.smBrowserNavigator;
import swarm.client.states.camera.Action_Camera_SetCameraTarget;
import swarm.client.states.camera.Action_Camera_SetCameraViewSize;
import swarm.client.states.camera.Action_Camera_SnapToAddress;
import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
import swarm.client.states.camera.Action_ViewingCell_Refresh;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.states.code.Action_EditingCode_Preview;
import swarm.client.states.code.Action_EditingCode_Save;
import swarm.client.view.smE_ZIndex;
import swarm.client.view.smI_UIElement;
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
import swarm.shared.statemachine.smStateEvent;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;
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
	
	//private final smHudButton m_back		= new smHudButton("back");
	//private final smHudButton m_forward		= new smHudButton("forward");
	private final smHudButton m_refresh		= new smHudButton("refresh");
	private final smHudButton m_close		= new smHudButton("close");

	private boolean m_waitingForBeingRefreshableAgain = false;
	
	private final smAppContext m_appContext;
	private final smViewContext m_viewContext;
	
	private final smClientAppConfig m_appConfig;
	
	private final Action_Camera_SetCameraTarget.Args m_args_SetCameraTarget = new Action_Camera_SetCameraTarget.Args();
	
	public smVisualCellHud(smAppContext appContext, smViewContext viewContext, smClientAppConfig appConfig)
	{
		m_appContext = appContext;
		m_viewContext = viewContext;
		
		m_appConfig = appConfig;
		
		this.addStyleName("sm_cell_hud");
		
		smE_ZIndex.CELL_HUD.assignTo(this);
		
		this.setVisible(false);
		
		m_innerContainer.setWidth("100%");
		
		m_innerContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		m_leftDock.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		m_rightDock.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		//m_leftDock.add(m_back);
		//m_leftDock.add(m_forward);
		m_leftDock.add(m_refresh);
		
		m_rightDock.add(m_close);
		
		m_innerContainer.add(m_leftDock);
		m_innerContainer.add(m_rightDock);
		
		m_innerContainer.setCellHorizontalAlignment(m_rightDock, HasHorizontalAlignment.ALIGN_RIGHT);
		
		this.add(m_innerContainer);
		
		/*m_back.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				smBrowserNavigator.getInstance().go(-1);
			}
		});
		
		m_forward.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				smBrowserNavigator.getInstance().go(1);
			}
		});*/
		
		m_viewContext.clickMngr.addClickHandler(m_refresh, new smI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				if( !m_refresh.isEnabled() )  return;
				
				smVisualCellHud.this.m_viewContext.cellMngr.clearAlerts();
				
				smA_Action.perform(Action_ViewingCell_Refresh.class);
			}
		});
		
		m_viewContext.clickMngr.addClickHandler(m_close, new smI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				State_ViewingCell state = smA_State.getEnteredInstance(State_ViewingCell.class);
				
				if( state == null )
				{
					smU_Debug.ASSERT(false, "smVisualCellHud::viewing state should have been entered.");
					
					return;
				}
				
				smBufferCell cell = state.getCell();
				smGridCoordinate coord = cell.getCoordinate();
				smA_Grid grid = cell.getGrid();
				
				coord.calcCenterPoint(s_utilPoint1, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
				s_utilPoint1.incZ(m_appConfig.backOffDistance);
				
				m_args_SetCameraTarget.init(s_utilPoint1, false);
				smA_Action.perform(Action_Camera_SetCameraTarget.class, m_args_SetCameraTarget);
			}
		});
		
		smToolTipManager toolTipper = m_viewContext.toolTipMngr;
		
		//toolTipper.addTip(m_back, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Go back."));
		//toolTipper.addTip(m_forward, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Go forward."));
		toolTipper.addTip(m_refresh, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Refresh this cell."));
		toolTipper.addTip(m_close, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Back off."));
	}
	
	private void updateRefreshButton()
	{
		boolean canRefresh = smA_Action.isPerformable(Action_ViewingCell_Refresh.class);
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
	
	private void updatePosition(State_ViewingCell state)
	{
		smCamera camera = m_appContext.cameraMngr.getCamera();
		smBufferCell cell = ((State_ViewingCell)state).getCell();
		smA_Grid grid = cell.getGrid();
		smGridCoordinate coord = cell.getCoordinate();
		coord.calcPoint(s_utilPoint1, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
		camera.calcScreenPoint(s_utilPoint1, s_utilPoint2);
		this.getElement().getStyle().setLeft(s_utilPoint2.getX(), Unit.PX);
		double y = s_utilPoint2.getY()-m_appConfig.cellHudHeight-grid.getCellPadding();
		y -= 3; // account for margin...sigh
		this.getElement().getStyle().setTop(y, Unit.PX);
	}
	
	private void toggleButtons()
	{
		/*boolean enabled = smA_Action.isPerformable(State_ViewingCell.Back.class);
		m_back.setEnabled(enabled);
		m_forward.setEnabled(smA_Action.isPerformable(State_ViewingCell.Forward.class));*/
		m_refresh.setEnabled(smA_Action.isPerformable(Action_ViewingCell_Refresh.class));
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
					this.updatePosition((State_ViewingCell) event.getState());
					
					this.setVisible(true);
				}
				
				break;
			}
			
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof State_ViewingCell )
				{
					this.toggleButtons();
					updateRefreshButton();
				}
				
				break;
			}
			
			case DID_UPDATE:
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
				
				break;
			}
			
			case DID_EXIT:
			{
				if( event.getState() instanceof State_ViewingCell )
				{
					this.setVisible(false);
					
					m_waitingForBeingRefreshableAgain = false;
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == Action_Camera_SetCameraViewSize.class )
				{
					State_ViewingCell state = (State_ViewingCell)smA_State.getEnteredInstance(State_ViewingCell.class);
					
					if( state != null )
					{
						smVisualCellHud.this.updatePosition(state);
					}
				}
				else if((	event.getAction() == Action_ViewingCell_Refresh.class				||
							event.getAction() == Action_EditingCode_Save.class					||
							event.getAction() == Action_EditingCode_Preview.class				||
				
							//--- DRK > These two cover the case of if we snap to a cell that we're already visiting.
							//---		This effectively refreshes the cell in this case.
							event.getAction() == Action_Camera_SnapToAddress.class		||
							event.getAction() == Action_Camera_SnapToCoordinate.class		))
				{
					updateRefreshButton();
				}
				
				break;
			}
		}
	}
}
