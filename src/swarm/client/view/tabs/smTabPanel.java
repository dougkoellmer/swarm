package swarm.client.view.tabs;

import java.util.logging.Logger;

import swarm.client.app.smAppContext;
import swarm.client.input.smClickManager;
import swarm.client.input.smI_ClickHandler;
import swarm.client.managers.smClientAccountManager;
import swarm.client.states.Action_Tabs_SelectTab;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.StateMachine_Tabs;
import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.account.State_ManageAccount;
import swarm.client.states.account.State_SignInOrUp;
import swarm.client.states.code.StateMachine_EditingCode;
import swarm.client.states.code.State_EditingCode;
import swarm.client.view.smI_UIElement;
import swarm.client.view.smS_UI;
import swarm.client.view.smU_ToString;
import swarm.client.view.smViewContext;
import swarm.client.view.alignment.smAlignmentDefinition;
import swarm.client.view.alignment.smU_Alignment;
import swarm.client.view.tabs.account.smAccountTabContent;
import swarm.client.view.tabs.code.smCodeEditorTabContent;
import swarm.client.view.tooltip.smE_ToolTipMood;
import swarm.client.view.tooltip.smE_ToolTipType;
import swarm.client.view.tooltip.smToolTipConfig;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.client.view.widget.smButton;
import swarm.client.view.widget.smButtonWithText;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smStateEvent;
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

public class smTabPanel extends AbsolutePanel implements smI_UIElement
{	
	public static final double TAB_HEIGHT = 40;
	
	private static final Logger s_logger = Logger.getLogger(smTabPanel.class.getName());
	
	private final FlowPanel m_tabContainerWrapper = new FlowPanel();
	private final HorizontalPanel m_tabContainer = new HorizontalPanel();
	private final FlowPanel m_contentContainer = new FlowPanel();
	
	private smI_TabContent m_currentContent = null;
	private smButtonWithText m_currentTab = null;
	
	private static final Action_Tabs_SelectTab.Args m_args_SelectTab = new Action_Tabs_SelectTab.Args();
	
	private final smI_Tab[] m_tabs;
	
	private final smViewContext m_viewContext;
	
	public smTabPanel(smViewContext viewContext, smI_Tab[] tabs)
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
	
	private void addTab(smI_Tab tab, final int tabIndex)
	{
		smButtonWithText tabButton = new smButtonWithText();
		tabButton.addStyleName("sm_tab");
		tabButton.setText(tab.getName()); 
		m_viewContext.clickMngr.addClickHandler(tabButton, new smI_ClickHandler()
		{
			public void onClick(int x, int y)
			{
				m_args_SelectTab.setIndex(tabIndex);
				m_viewContext.stateContext.performAction(Action_Tabs_SelectTab.class, m_args_SelectTab);
			}
		});
		
		m_viewContext.toolTipMngr.addTip(tabButton, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, tab.getToolTipText()));
		
		m_tabContainer.add(tabButton);
		
		tab.onAttached(tabButton);
	}
	
	private void selectTab(int index)
	{
		smI_TabContent newContent = m_tabs[index].getContent();
		
		if( newContent == m_currentContent )
		{
			smU_Debug.ASSERT(false, "selectTab3");
			
			return;
		}
		
		if( m_currentContent != null )
		{
			m_currentTab.removeStyleName("sm_tab_selected");
			m_currentTab.addStyleName("sm_tab");
			m_currentTab.setEnabled(true);
			
			smU_Debug.ASSERT(m_currentContent.asWidget().getParent() != null, "selectTab1");
			
			m_currentContent.asWidget().removeFromParent();
		}

		//smU_Debug.ASSERT(newContent.asWidget().getParent() == null, "selectTab2");

		if( newContent.asWidget().getParent() == null )
		{
			m_contentContainer.add(newContent);
		}
		m_currentContent = newContent;
		
		smButtonWithText newTab = (smButtonWithText) m_tabContainer.getWidget(index);
		newTab.removeStyleName("sm_tab");
		newTab.addStyleName("sm_tab_selected");
		newTab.setEnabled(false);
		m_currentTab = newTab;
	}
	
	@Override
	public void onStateEvent(smStateEvent event)
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
