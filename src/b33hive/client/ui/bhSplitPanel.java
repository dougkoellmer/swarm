package com.b33hive.client.ui;

import java.util.logging.Logger;

import com.b33hive.client.input.bhClickManager;
import com.b33hive.client.input.bhI_ClickHandler;
import com.b33hive.client.managers.bhClientAccountManager;
import com.b33hive.client.recaptcha.bhRecaptchaWrapper;
import com.b33hive.client.states.StateContainer_Base;
import com.b33hive.client.states.StateMachine_Base;
import com.b33hive.client.states.StateMachine_Tabs;
import com.b33hive.client.states.account.State_ManageAccount;
import com.b33hive.client.states.account.State_SignInOrUp;
import com.b33hive.client.ui.alignment.bhAlignmentDefinition;
import com.b33hive.client.ui.alignment.bhU_Alignment;
import com.b33hive.client.ui.cell.bhVisualCellContainer;
import com.b33hive.client.ui.tabs.bhTabPanel;
import com.b33hive.client.ui.tooltip.bhE_ToolTipMood;
import com.b33hive.client.ui.tooltip.bhE_ToolTipType;
import com.b33hive.client.ui.tooltip.bhToolTipConfig;
import com.b33hive.client.ui.tooltip.bhToolTipManager;
import com.b33hive.client.ui.widget.bhSpriteButton;
import com.b33hive.shared.statemachine.bhA_Action;
import com.b33hive.shared.statemachine.bhA_State;
import com.b33hive.shared.statemachine.bhE_StateEventType;
import com.b33hive.shared.statemachine.bhStateEvent;
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

public class bhSplitPanel extends SplitLayoutPanel implements bhI_UIElement
{
	private static final Logger s_logger = Logger.getLogger(bhSplitPanel.class.getName());
	
	private static final bhSplitPanel s_instance = new bhSplitPanel();
	
	private final bhTabPanel m_tabPanel = new bhTabPanel();
	private final bhVisualCellContainer m_cellContainer = new bhVisualCellContainer();
	
	private final bhSpriteButton m_panelButton = new bhSpriteButton(null);
	
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
	
	private final bhTweener m_tweener = new bhTweener(bhS_UI.CONSOLE_ANIMATE_TIME);
	private boolean m_justTweened = false;
	private boolean m_reloadCaptchaASAP = false;
	
	private bhSplitPanel()
	{
		super((int) PARENT_SPLITTER_WIDTH);
		
		this.addStyleName("split_panel");
		
		int pageWidth = RootPanel.get().getOffsetWidth();
		double panelWidth = pageWidth*STARTING_PANEL_RATIO;
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
		m_splitter.addStyleName("bh_button bh_hdragger");
		m_splitter.getElement().getStyle().setProperty("borderRadius", "0px"); // for some reason can't override bh_button in css
		m_splitter.getElement().getStyle().setWidth(8, Unit.PX);
		m_splitter.getParent().addDomHandler(mouseDownHandler, MouseDownEvent.getType());
		
		m_panelButton.addStyleName("bh_panel_button");
		
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
		bhClickManager.getInstance().addClickHandler(m_panelButton, new bhI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				if( bhA_Action.perform(StateContainer_Base.Hide.class) )
				{
					m_lastTabPanelSize = m_tabPanel.getOffsetWidth();
					
					m_tweener.start(m_lastTabPanelSize, 0);
				}
				else if( bhA_Action.perform(StateContainer_Base.Show.class) )
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
		
		bhE_ZIndex.MINIMIZER_MAXIMIZER.assignTo(m_panelButton);

		setToggleButton(false);
		updateButtonPosition(panelWidth);
		RootPanel.get().add(m_panelButton);
	}
	
	public static bhSplitPanel getInstance()
	{
		return s_instance;
	}
	
	public double getTabPanelWidth()
	{
		return m_tabPanelWidth;
	}
	
	public double getCellPanelWidth()
	{
		return RootPanel.get().getOffsetWidth() - m_tabPanelWidth;
	}
	
	private void setToggleButton(boolean openArrow)
	{
		bhToolTipManager toolTipper = bhToolTipManager.getInstance();
		
		if( openArrow )
		{
			m_panelButton.setSpriteId("maximize");
			
			toolTipper.addTip(m_panelButton, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Expand the console."));
		}
		else
		{
			m_panelButton.setSpriteId("minimize");
			
			toolTipper.addTip(m_panelButton, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Collapse the console."));
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
	
	public bhVisualCellContainer getCellContainer()
	{
		return m_cellContainer;
	}
	
	public bhTabPanel getTabPanel()
	{
		return m_tabPanel;
	}
	
	@Override
	public void onStateEvent(bhStateEvent event)
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
					setToggleButton(false);
				}
				
				break;
			}
			
			case DID_BACKGROUND:
			{
				if( event.getState() instanceof StateMachine_Tabs )
				{
					setToggleButton(true);
					
					m_tabPanel.getElement().blur();
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == StateMachine_Base.OnAccountManagerResponse.class )
				{
					StateMachine_Base.OnAccountManagerResponse.Args args = event.getActionArgs();
					bhClientAccountManager.E_ResponseType responseType = args.getType();
					String text = bhU_ToString.get(responseType);
					
					if( text != null )
					{
						if( bhA_State.isForegrounded(StateContainer_Base.class) )
						{
							if( !bhA_State.isForegrounded(StateMachine_Tabs.class) )
							{
								bhAlignmentDefinition alignment = bhU_Alignment.createHorRightVerCenter(bhS_UI.TOOl_TIP_PADDING);
	
								bhE_ToolTipMood severity = responseType.isGood() ? bhE_ToolTipMood.PAT_ON_BACK : bhE_ToolTipMood.OOPS;
								bhToolTipConfig config = new bhToolTipConfig(bhE_ToolTipType.NOTIFICATION, alignment, text, severity);
								bhToolTipManager.getInstance().addTip(m_panelButton, config);
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
							m_reloadCaptchaASAP = true; // sloppy
							
							break;
						}
					}
				}
				
				break;
			}
		}
		
		m_tabPanel.onStateEvent(event);
		m_cellContainer.onStateEvent(event);
		
		//--- DRK > Doing this here so tabpanel has a chance to first attach recaptcha to DOM.
		if( m_reloadCaptchaASAP )
		{
			if( event.getType() != bhE_StateEventType.DID_UPDATE )
			{
				if( bhA_State.isForegrounded(State_SignInOrUp.class) )
				{
					m_reloadCaptchaASAP = false;
					bhRecaptchaWrapper.getInstance().loadNewImage();
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
		boolean showable = bhA_Action.isPerformable(StateContainer_Base.Show.class);
		boolean hideable = bhA_Action.isPerformable(StateContainer_Base.Hide.class);
		
		if( showable )
		{
			if( m_splitter.getAbsoluteLeft() > 0 )
			{
				bhA_Action.perform(StateContainer_Base.Show.class);
			}
		}
		else if( hideable )
		{
			if( m_splitter.getAbsoluteLeft() <= 0 )
			{
				m_lastTabPanelSize = 0;
				bhA_Action.perform(StateContainer_Base.Hide.class);
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
