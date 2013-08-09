package b33hive.client.ui.cell;

import b33hive.client.app.bhClientAppConfig;
import b33hive.client.app.bh_c;
import b33hive.client.entities.bhCamera;
import b33hive.client.entities.bhBufferCell;
import b33hive.client.entities.bhE_CodeStatus;
import b33hive.client.input.bhClickManager;
import b33hive.client.input.bhI_ClickHandler;
import b33hive.client.navigation.bhBrowserNavigator;
import b33hive.client.states.camera.StateMachine_Camera;
import b33hive.client.states.camera.State_ViewingCell;
import b33hive.client.states.code.State_EditingCode;
import b33hive.client.ui.bhE_ZIndex;
import b33hive.client.ui.bhI_UIElement;
import b33hive.client.ui.bh_view;
import b33hive.client.ui.tooltip.bhE_ToolTipType;
import b33hive.client.ui.tooltip.bhToolTipConfig;
import b33hive.client.ui.tooltip.bhToolTipManager;
import b33hive.client.ui.widget.bhSpriteButton;
import b33hive.shared.app.bhS_App;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.entities.bhA_Grid;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhStateEvent;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.structs.bhPoint;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

public class bhVisualCellHud extends FlowPanel implements bhI_UIElement
{
	private static final bhPoint s_utilPoint1 = new bhPoint();
	private static final bhPoint s_utilPoint2 = new bhPoint();
	
	private class bhHudButton extends bhSpriteButton
	{
		private bhHudButton(String spriteId)
		{
			super(spriteId);
			
			this.addStyleName("bh_hud_button");
		}
	}
	
	private final HorizontalPanel m_innerContainer = new HorizontalPanel();
	private final HorizontalPanel m_leftDock = new HorizontalPanel();
	private final HorizontalPanel m_rightDock = new HorizontalPanel();
	
	//private final bhHudButton m_back		= new bhHudButton("back");
	//private final bhHudButton m_forward		= new bhHudButton("forward");
	private final bhHudButton m_refresh		= new bhHudButton("refresh");
	private final bhHudButton m_close		= new bhHudButton("close");

	private boolean m_waitingForBeingRefreshableAgain = false;
	
	private final bhClientAppConfig m_appConfig;
	
	private final StateMachine_Camera.SetCameraTarget.Args m_args_SetCameraTarget = new StateMachine_Camera.SetCameraTarget.Args();
	
	public bhVisualCellHud(Panel parent, bhClientAppConfig appConfig)
	{
		m_appConfig = appConfig;
		
		this.addStyleName("bh_cell_hud");
		
		bhE_ZIndex.CELL_HUD.assignTo(this);
		
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
	
		parent.add(this);
		
		/*m_back.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				bhBrowserNavigator.getInstance().go(-1);
			}
		});
		
		m_forward.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				bhBrowserNavigator.getInstance().go(1);
			}
		});*/
		
		bh_c.clickMngr.addClickHandler(m_refresh, new bhI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				if( !m_refresh.isEnabled() )  return;
				
				bh_view.cellMngr.clearAlerts();
				
				bhA_Action.perform(State_ViewingCell.Refresh.class);
			}
		});
		
		bh_c.clickMngr.addClickHandler(m_close, new bhI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				State_ViewingCell state = bhA_State.getEnteredInstance(State_ViewingCell.class);
				
				if( state == null )
				{
					bhU_Debug.ASSERT(false, "bhVisualCellHud::viewing state should have been entered.");
					
					return;
				}
				
				bhBufferCell cell = state.getCell();
				bhGridCoordinate coord = cell.getCoordinate();
				bhA_Grid grid = cell.getGrid();
				
				coord.calcCenterPoint(s_utilPoint1, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
				s_utilPoint1.incZ(m_appConfig.backOffDistance);
				
				m_args_SetCameraTarget.setPoint(s_utilPoint1);
				bhA_Action.perform(StateMachine_Camera.SetCameraTarget.class, m_args_SetCameraTarget);
			}
		});
		
		bhToolTipManager toolTipper = bh_c.toolTipMngr;
		
		//toolTipper.addTip(m_back, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Go back."));
		//toolTipper.addTip(m_forward, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Go forward."));
		toolTipper.addTip(m_refresh, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Refresh this cell."));
		toolTipper.addTip(m_close, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Back off."));
	}
	
	private void updateRefreshButton()
	{
		boolean canRefresh = bhA_Action.isPerformable(State_ViewingCell.Refresh.class);
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
		bhCamera camera = bh_c.camera;
		bhBufferCell cell = ((State_ViewingCell)state).getCell();
		bhA_Grid grid = cell.getGrid();
		bhGridCoordinate coord = cell.getCoordinate();
		coord.calcPoint(s_utilPoint1, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
		camera.calcScreenPoint(s_utilPoint1, s_utilPoint2);
		this.getElement().getStyle().setLeft(s_utilPoint2.getX(), Unit.PX);
		double y = s_utilPoint2.getY()-m_appConfig.cellHudHeight-grid.getCellPadding();
		y -= 3; // account for margin...sigh
		this.getElement().getStyle().setTop(y, Unit.PX);
	}
	
	private void toggleButtons()
	{
		/*boolean enabled = bhA_Action.isPerformable(State_ViewingCell.Back.class);
		m_back.setEnabled(enabled);
		m_forward.setEnabled(bhA_Action.isPerformable(State_ViewingCell.Forward.class));*/
		m_refresh.setEnabled(bhA_Action.isPerformable(State_ViewingCell.Refresh.class));
	}
	
	@Override
	public void onStateEvent(bhStateEvent event)
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
				if( event.getAction() == StateMachine_Camera.SetCameraViewSize.class )
				{
					State_ViewingCell state = (State_ViewingCell)bhA_State.getEnteredInstance(State_ViewingCell.class);
					
					if( state != null )
					{
						bhVisualCellHud.this.updatePosition(state);
					}
				}
				else if((	event.getAction() == State_ViewingCell.Refresh.class				||
							event.getAction() == State_EditingCode.Save.class					||
							event.getAction() == State_EditingCode.Preview.class				||
				
							//--- DRK > These two cover the case of if we snap to a cell that we're already visiting.
							//---		This effectively refreshes the cell in this case.
							event.getAction() == StateMachine_Camera.SnapToAddress.class		||
							event.getAction() == StateMachine_Camera.SnapToCoordinate.class		))
				{
					updateRefreshButton();
				}
				
				break;
			}
		}
	}
}
