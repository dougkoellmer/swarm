package swarm.client.view;

import java.util.logging.Logger;

import swarm.client.app.smAppContext;
import swarm.client.input.smClickManager;
import swarm.client.input.smI_ClickHandler;
import swarm.client.managers.smClientAccountManager;
import swarm.client.states.Action_Base_HideSupplementState;
import swarm.client.states.Action_Base_ShowSupplementState;
import swarm.client.states.StateContainer_Base;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.StateMachine_Tabs;
import swarm.client.states.account.State_ManageAccount;
import swarm.client.states.account.State_SignInOrUp;
import swarm.client.thirdparty.captcha.smRecaptchaWrapper;
import swarm.client.view.alignment.smAlignmentDefinition;
import swarm.client.view.alignment.smU_Alignment;
import swarm.client.view.cell.smVisualCellContainer;
import swarm.client.view.tabs.smTabPanel;
import swarm.client.view.tooltip.smE_ToolTipMood;
import swarm.client.view.tooltip.smE_ToolTipType;
import swarm.client.view.tooltip.smToolTipConfig;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.client.view.widget.smSpriteButton;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smE_StateEventType;
import swarm.shared.statemachine.smStateEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class smSplitPanel extends SplitLayoutPanel implements smI_UIElement
{
	private static final Logger s_logger = Logger.getLogger(smSplitPanel.class.getName());
	
	private final smTabPanel m_tabPanel;
	private final smVisualCellContainer m_cellContainer;
	
	private final smSpriteButton m_panelButton = new smSpriteButton(null);
	
	private Widget m_splitter = null;
	
	private double m_lastTabPanelSize = 0;
	
	private static final double PANEL_RATIO_IF_WINDOW_CUTS_OFF = .66666666;
	private static final double STARTING_PANEL_RATIO = .3333333333;
	private static final double MIN_INITIAL_CONSOLE_WIDTH = 370;
	private static final double MIN_INITIAL_GRID_WIDTH = 520;
	private static final double PARENT_SPLITTER_WIDTH = 10;
	private static final double SPLITTER_WIDTH = 10;
	private static final double PANEL_BUTTON_WIDTH = 16;
	private static final double PANEL_BUTTON_HEIGHT = 64;
	
	private double m_tabPanelWidth = 0;
	
	private final smTweener m_tweener = new smTweener(smS_UI.CONSOLE_ANIMATE_TIME);
	private boolean m_justTweened = false;
	private boolean m_reloadCaptchaASAP = false;
	
	private final smViewContext m_viewContext;
	
	smSplitPanel(smViewContext viewContext, smViewConfig config)
	{
		super((int) PARENT_SPLITTER_WIDTH);
		
		m_viewContext = viewContext;
		
		m_tabPanel = new smTabPanel(m_viewContext, config.tabs);
		m_cellContainer = new smVisualCellContainer(m_viewContext, config);
		
		this.addStyleName("split_panel");
		
		int pageWidth = RootPanel.get().getOffsetWidth();
		double panelWidth = 0;
		
		if( m_viewContext.stateContext.isForegrounded(StateMachine_Tabs.class) )
		{
			panelWidth = pageWidth*STARTING_PANEL_RATIO;

			if( panelWidth < MIN_INITIAL_CONSOLE_WIDTH )
			{
				panelWidth = MIN_INITIAL_CONSOLE_WIDTH;
			}
			if( (pageWidth - panelWidth) < MIN_INITIAL_GRID_WIDTH )
			{
				panelWidth = pageWidth - MIN_INITIAL_GRID_WIDTH;
			}
			if( panelWidth < 0 )
			{
				panelWidth = 0;
			}
		}
		
		m_tabPanelWidth = panelWidth;
		this.addWest(m_tabPanel, panelWidth);
		this.add(m_cellContainer);

		//--- DRK > This makes it so that tab panel stops animating in/out if it's clicked in any way, including the splitter.
		MouseDownHandler mouseDownHandler = new MouseDownHandler()
		{
			@Override
			public void onMouseDown(MouseDownEvent event)
			{
				if( m_tweener.isTweening() )
				{
					m_tweener.stop();
					
					showOrHideTabControllerStateFromManualResize();
				}
			}
		};
		m_splitter = findHSplitter();
		m_splitter.addStyleName("sm_button sm_hdragger");
		m_splitter.getElement().getStyle().setProperty("borderRadius", "0px"); // for some reason can't override sm_button in css
		m_splitter.getElement().getStyle().setWidth(8, Unit.PX);
		m_splitter.getParent().addDomHandler(mouseDownHandler, MouseDownEvent.getType());
		
		m_panelButton.addStyleName("sm_panel_button");
		
		//--- DRK > I think it just looks a little sloppy if you experiment around and can drag this particular button.
		m_panelButton.addDragStartHandler(new DragStartHandler()
		{
			@Override
			public void onDragStart(DragStartEvent event)
			{
				event.preventDefault();
			}
		});
		
		//--- DRK > Initiates an open/close animation.
		m_viewContext.clickMngr.addClickHandler(m_panelButton, new smI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				if( m_viewContext.stateContext.performAction(Action_Base_HideSupplementState.class) )
				{
					m_lastTabPanelSize = m_tabPanel.getOffsetWidth();
					
					m_tweener.start(m_lastTabPanelSize, 0);
				}
				else if( m_viewContext.stateContext.performAction(Action_Base_ShowSupplementState.class) )
				{
					double windowWidth = RootPanel.get().getOffsetWidth();
					
					if( m_lastTabPanelSize > windowWidth )
					{
						m_lastTabPanelSize = windowWidth * PANEL_RATIO_IF_WINDOW_CUTS_OFF;
					}
					else if( m_lastTabPanelSize <= 0 )
					{
						m_lastTabPanelSize = windowWidth * STARTING_PANEL_RATIO;
					}
					
					m_tweener.start(m_tabPanel.getOffsetWidth(), m_lastTabPanelSize);
				}
			}
		});
		
		smE_ZIndex.MINIMIZER_MAXIMIZER.assignTo(m_panelButton);

		updateToggleButton();
		updateButtonPosition(panelWidth);
		RootPanel.get().add(m_panelButton);
	}
	
	public double getTabPanelWidth()
	{
		return m_tabPanelWidth;
	}
	
	public double getCellPanelWidth()
	{
		return RootPanel.get().getOffsetWidth() - m_tabPanelWidth - SPLITTER_WIDTH;
	}
	
	private void updateToggleButton()
	{
		smToolTipManager toolTipper = m_viewContext.toolTipMngr;
		
		StateMachine_Tabs tabMachine = m_viewContext.stateContext.getEnteredState(StateMachine_Tabs.class);
		
		boolean isCollapsed = !tabMachine.isForegrounded() && tabMachine.getBlockingState() == null;
		
		if( isCollapsed )
		{
			m_panelButton.setSpriteId("maximize");
			
			toolTipper.addTip(m_panelButton, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Expand the console."));
		}
		else
		{
			m_panelButton.setSpriteId("minimize");
			
			toolTipper.addTip(m_panelButton, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Collapse the console."));
		}
	}

	private Widget findHSplitter()
	{
		int widgetCount = this.getWidgetCount ();
		for (int i = 0; i < widgetCount; i++)
		{
			Widget w = this.getWidget(i);
			
			if (w.getStyleName ().equals ("gwt-SplitLayoutPanel-HDragger"))
			{
				return w;
			}
		}
		
		return null;
	}
	
	public smVisualCellContainer getCellContainer()
	{
		return m_cellContainer;
	}
	
	public smTabPanel getTabPanel()
	{
		return m_tabPanel;
	}
	
	@Override
	public void onStateEvent(smStateEvent event)
	{
		switch(event.getType())
		{
			case DID_UPDATE:
			{
				if( event.getState() instanceof StateContainer_Base)
				{
					if( m_tweener.isTweening() )
					{
						double tweenValue = m_tweener.update(event.getState().getLastTimeStep());
						
						m_justTweened = true;
						{
							this.setWidgetSize(m_tabPanel, tweenValue);
							this.forceLayout();
						}
						m_justTweened = false;
					}
				}
				break;
			}
			
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof StateMachine_Tabs )
				{
					updateToggleButton();
				}
				
				break;
			}
			
			case DID_BACKGROUND:
			{
				if( event.getState() instanceof StateMachine_Tabs )
				{
					updateToggleButton();
					
					m_tabPanel.getElement().blur();
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				//TODO(DRK): Pretty sure this can be moved to smAccountTab...think it is here because
				//			 at some point the tab UI didn't get state events if the tab content's state wasn't foregrounded.
				if( event.getAction() == StateMachine_Base.OnAccountManagerResponse.class )
				{
					StateMachine_Base.OnAccountManagerResponse.Args args = event.getActionArgs();
					smClientAccountManager.E_ResponseType responseType = args.getType();
					String text = smU_ToString.get(responseType);
					
					if( text != null )
					{
						if( m_viewContext.stateContext.isForegrounded(StateContainer_Base.class) )
						{
							if( !m_viewContext.stateContext.isForegrounded(StateMachine_Tabs.class) )
							{
								smAlignmentDefinition alignment = smU_Alignment.createHorRightVerCenter(smS_UI.TOOl_TIP_PADDING);
	
								smE_ToolTipMood severity = responseType.isGood() ? smE_ToolTipMood.PAT_ON_BACK : smE_ToolTipMood.OOPS;
								smToolTipConfig config = new smToolTipConfig(smE_ToolTipType.NOTIFICATION, alignment, text, severity);
								m_viewContext.toolTipMngr.addTip(m_panelButton, config);
							}
						}
					}
					
					//--- DRK > Have to load a new captcha on various account-related events. This is handled way up here
					//---		at the split panel level (rather than in the sign up panel) because events generally don't
					//---		trickle down to ui when it isn't visible.
					switch(responseType)
					{
						case SIGN_UP_SUCCESS:
						case SIGN_IN_SUCCESS:
						case PASSWORD_CONFIRM_SUCCESS:
						{
							break;
						}
						
						default:
						{
							m_reloadCaptchaASAP = true; // kinda sloppy
							
							break;
						}
					}
				}
				
				break;
			}
		}
		
		m_tabPanel.onStateEvent(event);
		m_cellContainer.onStateEvent(event);
		
		//--- DRK > Doing this here, after forwarding event to tabpanel, so it has a chance to first attach recaptcha to DOM.
		if( m_reloadCaptchaASAP )
		{
			if( event.getType() != smE_StateEventType.DID_UPDATE )
			{
				if( m_viewContext.stateContext.isForegrounded(State_SignInOrUp.class) )
				{
					m_reloadCaptchaASAP = false;
					m_viewContext.recaptchaWrapper.loadNewImage();
				}
			}
		}
	}
	
	// TODO: This method is so fucking hacked...
	private void updateButtonPosition(double splitterX)
	{
		// TODO: get these implicitly from css...value is 0 until attached to the DOM or something, and I think we need it before.
		double splitterWidth = SPLITTER_WIDTH;
		double buttonWidth = PANEL_BUTTON_WIDTH;
		double buttonHeight = PANEL_BUTTON_HEIGHT;
		
		double buttonX = splitterX - buttonWidth/2;// + (SPLITTER_WIDTH/2);
		double buttonY = RootPanel.get().getOffsetHeight()/2 - buttonHeight/2;
		
		m_panelButton.getElement().getStyle().setLeft(buttonX, Unit.PX);
		m_panelButton.getElement().getStyle().setTop(buttonY, Unit.PX);
	}
	
	private void updateButtonPosition()
	{
		double splitterX = m_splitter.getAbsoluteLeft();
		
		updateButtonPosition(splitterX);
	}
	
	private void showOrHideTabControllerStateFromManualResize()
	{
		boolean showable = m_viewContext.stateContext.isActionPerformable(Action_Base_ShowSupplementState.class);
		boolean hideable = m_viewContext.stateContext.isActionPerformable(Action_Base_HideSupplementState.class);
		
		if( showable )
		{
			if( m_splitter.getAbsoluteLeft() > 0 )
			{
				m_viewContext.stateContext.performAction(Action_Base_ShowSupplementState.class);
			}
		}
		else if( hideable )
		{
			if( m_splitter.getAbsoluteLeft() <= 0 )
			{
				m_lastTabPanelSize = 0;
				m_viewContext.stateContext.performAction(Action_Base_HideSupplementState.class);
			}
		}
	}
	
	@Override
	public void onResize()
	{
		super.onResize();
		
		m_tabPanelWidth = m_splitter.getAbsoluteLeft();
		
		/*double rootPanelWidth = RootPanel.get().getOffsetWidth();
		
		if( m_tabPanelWidth > RootPanel.get().getOffsetWidth() )
		{
			this.setWidgetSize(m_tabPanel, rootPanelWidth-PARENT_SPLITTER_WIDTH);
			return;
		}*/
		
		m_tabPanel.onResize();
		
		if( !m_tweener.isTweening() && !m_justTweened )
		{
			showOrHideTabControllerStateFromManualResize();
		}
		
		updateButtonPosition();
		
		m_cellContainer.onResize();
	}
}