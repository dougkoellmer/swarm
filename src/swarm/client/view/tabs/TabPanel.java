package swarm.client.view.tabs;

import java.util.logging.Logger;

import swarm.client.app.AppContext;
import swarm.client.input.ClickManager;
import swarm.client.input.I_ClickHandler;
import swarm.client.managers.ClientAccountManager;
import swarm.client.states.Action_Tabs_SelectTab;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.StateMachine_Tabs;
import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.account.State_ManageAccount;
import swarm.client.states.account.State_SignInOrUp;
import swarm.client.states.code.StateMachine_EditingCode;
import swarm.client.states.code.State_EditingCode;
import swarm.client.view.I_UIElement;
import swarm.client.view.S_UI;
import swarm.client.view.U_ToString;
import swarm.client.view.ViewContext;
import swarm.client.view.alignment.AlignmentDefinition;
import swarm.client.view.alignment.U_Alignment;
import swarm.client.view.tabs.account.AccountTabContent;
import swarm.client.view.tabs.code.CodeEditorTabContent;
import swarm.client.view.tooltip.E_ToolTipMood;
import swarm.client.view.tooltip.E_ToolTipType;
import swarm.client.view.tooltip.ToolTipConfig;
import swarm.client.view.tooltip.ToolTipManager;
import swarm.client.view.widget.BaseButton;
import swarm.client.view.widget.ButtonWithText;
import swarm.shared.debugging.U_Debug;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.StateEvent;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class TabPanel extends AbsolutePanel implements I_UIElement
{	
	public static final double TAB_HEIGHT = 40;
	
	private static final Logger s_logger = Logger.getLogger(TabPanel.class.getName());
	
	private final FlowPanel m_tabContainerWrapper = new FlowPanel();
	private final HorizontalPanel m_tabContainer = new HorizontalPanel();
	private final FlowPanel m_contentContainer = new FlowPanel();
	
	private I_TabContent m_currentContent = null;
	private ButtonWithText m_currentTab = null;
	
	private static final Action_Tabs_SelectTab.Args m_args_SelectTab = new Action_Tabs_SelectTab.Args();
	
	private final I_Tab[] m_tabs;
	
	private final ViewContext m_viewContext;
	
	public TabPanel(ViewContext viewContext, I_Tab[] tabs)
	{
		m_viewContext = viewContext;
		m_tabs = tabs;
		
		m_tabContainerWrapper.addStyleName("sm_tab_container_wrapper");
		m_tabContainer.addStyleName("sm_tab_container");
		m_contentContainer.addStyleName("sm_tab_content_container");
		this.addStyleName("sm_tab_panel");
		
		m_tabContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		
		updateLayout();
		
		if( m_tabs.length > 1 )
		{
			m_tabContainerWrapper.add(m_tabContainer);
			this.add(m_tabContainerWrapper);
		}
		
		this.add(m_contentContainer);
		
		for( int i = 0; i < m_tabs.length; i++ )
		{
			this.addTab(m_tabs[i], i);
		}
	}
	
	public double getTabButtonContainerHeight()
	{
		double tabHeight = m_tabs.length == 1 ? 0 : TAB_HEIGHT-1;
		
		return tabHeight;
	}
	
	private void updateLayout()
	{
		double tabHeight = getTabButtonContainerHeight();
		this.setHeight(RootPanel.get().getOffsetHeight() + "px");
		double containerHeight = RootPanel.get().getOffsetHeight() - tabHeight;
		m_tabContainerWrapper.setHeight(tabHeight + "px");
		m_contentContainer.setHeight(containerHeight + "px");
	}
	
	public void onResize()
	{
		updateLayout();

		if( m_currentContent != null )
		{
			m_currentContent.onResize();
		}
	}
	
	private void addTab(I_Tab tab, final int tabIndex)
	{
		ButtonWithText tabButton = new ButtonWithText();
		tabButton.addStyleName("sm_tab");
		tabButton.setText(tab.getName()); 
		m_viewContext.clickMngr.addClickHandler(tabButton, new I_ClickHandler()
		{
			public void onClick(int x, int y)
			{
				m_args_SelectTab.setIndex(tabIndex);
				m_viewContext.stateContext.perform(Action_Tabs_SelectTab.class, m_args_SelectTab);
			}
		});
		
		m_viewContext.toolTipMngr.addTip(tabButton, new ToolTipConfig(E_ToolTipType.MOUSE_OVER, tab.getToolTipText()));
		
		m_tabContainer.add(tabButton);
		
		tab.onAttached(tabButton);
	}
	
	private void selectTab(int index)
	{
		I_TabContent newContent = m_tabs[index].getContent();
		
		if( newContent == m_currentContent )
		{
			U_Debug.ASSERT(false, "selectTab3");
			
			return;
		}
		
		if( m_currentContent != null )
		{
			m_currentTab.removeStyleName("sm_tab_selected");
			m_currentTab.addStyleName("sm_tab");
			m_currentTab.setEnabled(true);
			
			U_Debug.ASSERT(m_currentContent.asWidget().getParent() != null, "selectTab1");
			
			m_currentContent.asWidget().removeFromParent();
		}

		//smU_Debug.ASSERT(newContent.asWidget().getParent() == null, "selectTab2");

		if( newContent.asWidget().getParent() == null )
		{
			m_contentContainer.add(newContent);
		}
		m_currentContent = newContent;
		
		ButtonWithText newTab = (ButtonWithText) m_tabContainer.getWidget(index);
		newTab.removeStyleName("sm_tab");
		newTab.addStyleName("sm_tab_selected");
		newTab.setEnabled(false);
		m_currentTab = newTab;
	}
	
	@Override
	public void onStateEvent(StateEvent event)
	{
		switch( event.getType() )
		{
			case DID_ENTER:
			{
				if( event.getState().getParent() instanceof StateMachine_Tabs )
				{
					StateMachine_Tabs tabMachine = event.getState().getParent();
					int tabIndex = tabMachine.calcTabIndex(event.getState().getClass());
					this.selectTab(tabIndex);
				}
				
				break;
			}
		}
		
		for( int i = 0; i < m_tabs.length; i++ )
		{
			m_tabs[i].onStateEvent(event);
		}
	}
}
