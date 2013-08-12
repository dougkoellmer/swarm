package b33hive.client.ui.tabs;

import java.util.logging.Logger;

import b33hive.client.app.bh_c;
import b33hive.client.input.bhClickManager;
import b33hive.client.input.bhI_ClickHandler;
import b33hive.client.managers.bhClientAccountManager;
import b33hive.client.states.StateMachine_Base;
import b33hive.client.states.StateMachine_Tabs;
import b33hive.client.states.account.StateMachine_Account;
import b33hive.client.states.account.State_ManageAccount;
import b33hive.client.states.account.State_SignInOrUp;
import b33hive.client.states.code.StateMachine_EditingCode;
import b33hive.client.states.code.State_EditingCode;
import b33hive.client.ui.bhI_UIElement;
import b33hive.client.ui.bhS_UI;
import b33hive.client.ui.bhU_ToString;
import b33hive.client.ui.alignment.bhAlignmentDefinition;
import b33hive.client.ui.alignment.bhU_Alignment;
import b33hive.client.ui.tabs.account.bhAccountTabContent;
import b33hive.client.ui.tabs.code.bhCodeEditorTabContent;
import b33hive.client.ui.tooltip.bhE_ToolTipMood;
import b33hive.client.ui.tooltip.bhE_ToolTipType;
import b33hive.client.ui.tooltip.bhToolTipConfig;
import b33hive.client.ui.tooltip.bhToolTipManager;
import b33hive.client.ui.widget.bhButton;
import b33hive.client.ui.widget.bhButtonWithText;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhStateEvent;
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

public class bhTabPanel extends AbsolutePanel implements bhI_UIElement
{	
	public static final double TAB_HEIGHT = 40;
	
	private static final Logger s_logger = Logger.getLogger(bhTabPanel.class.getName());
	
	private final FlowPanel m_tabContainerWrapper = new FlowPanel();
	private final HorizontalPanel m_tabContainer = new HorizontalPanel();
	private final FlowPanel m_contentContainer = new FlowPanel();
	
	private bhI_TabContent m_currentContent = null;
	private bhButtonWithText m_currentTab = null;
	
	private static final StateMachine_Tabs.SelectTab.Args m_args_SelectTab = new StateMachine_Tabs.SelectTab.Args();
	
	private final bhI_Tab[] m_tabs;
	
	public bhTabPanel(bhI_Tab[] tabs)
	{
		m_tabs = tabs;
		
		m_tabContainerWrapper.addStyleName("bh_tab_container_wrapper");
		m_tabContainer.addStyleName("bh_tab_container");
		m_contentContainer.addStyleName("bh_tab_content_container");
		this.addStyleName("tab_panel");
		
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
	
	private void addTab(bhI_Tab tab, final int tabIndex)
	{
		bhButtonWithText tabButton = new bhButtonWithText();
		tabButton.addStyleName("bh_tab");
		tabButton.setText(tab.getName()); 
		bh_c.clickMngr.addClickHandler(tabButton, new bhI_ClickHandler()
		{
			public void onClick()
			{
				m_args_SelectTab.setIndex(tabIndex);
				bhA_Action.perform(StateMachine_Tabs.SelectTab.class, m_args_SelectTab);
			}
		});
		
		bh_c.toolTipMngr.addTip(tabButton, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, tab.getToolTipText()));
		
		m_tabContainer.add(tabButton);
		
		tab.onAttached(tabButton);
	}
	
	private void selectTab(int index)
	{
		bhI_TabContent newContent = m_tabs[index].getContent();
		
		if( newContent == m_currentContent )
		{
			bhU_Debug.ASSERT(false, "selectTab3");
			
			return;
		}
		
		if( m_currentContent != null )
		{
			m_currentTab.removeStyleName("bh_tab_selected");
			m_currentTab.addStyleName("bh_tab");
			m_currentTab.setEnabled(true);
			
			bhU_Debug.ASSERT(m_currentContent.asWidget().getParent() != null, "selectTab1");
			
			m_currentContent.asWidget().removeFromParent();
		}

		//bhU_Debug.ASSERT(newContent.asWidget().getParent() == null, "selectTab2");

		if( newContent.asWidget().getParent() == null )
		{
			m_contentContainer.add(newContent);
		}
		m_currentContent = newContent;
		
		bhButtonWithText newTab = (bhButtonWithText) m_tabContainer.getWidget(index);
		newTab.removeStyleName("bh_tab");
		newTab.addStyleName("bh_tab_selected");
		newTab.setEnabled(false);
		m_currentTab = newTab;
	}
	
	@Override
	public void onStateEvent(bhStateEvent event)
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
