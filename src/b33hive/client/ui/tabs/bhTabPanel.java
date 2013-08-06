package b33hive.client.ui.tabs;

import java.util.logging.Logger;

import b33hive.client.app.bh_c;
import b33hive.client.input.bhClickManager;
import b33hive.client.input.bhI_ClickHandler;
import b33hive.client.managers.bhClientAccountManager;
import b33hive.client.states.StateMachine_Base;
import b33hive.client.states.StateMachine_Tabs;
import b33hive.client.states.State_ViewingBookmarks;
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
import b33hive.client.ui.tabs.account.bhAccountTab;
import b33hive.client.ui.tabs.code.bhCodeEditorTab;
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
	private static enum Tab
	{
		ACCOUNT,
		//BOOKMARKS,
		HTML
	}
	
	public static final double TAB_HEIGHT = 40;
	
	private static final Logger s_logger = Logger.getLogger(bhTabPanel.class.getName());
	
	private final FlowPanel m_tabContainerWrapper = new FlowPanel();
	private final HorizontalPanel m_tabContainer = new HorizontalPanel();
	private final FlowPanel m_contentContainer = new FlowPanel();
	
	private bhI_TabContent m_currentContent = null;
	private bhButtonWithText m_currentTab = null;
	
	private bhI_TabContent m_tabContent[] = new bhI_TabContent[Tab.values().length];
	
	private final bhButtonWithText m_accountTabButton;
	
	private static final StateMachine_Tabs.SelectTab.Args m_args_SelectTab = new StateMachine_Tabs.SelectTab.Args();
	
	public bhTabPanel()
	{
		m_tabContainerWrapper.addStyleName("bh_tab_container_wrapper");
		m_tabContainer.addStyleName("bh_tab_container");
		m_contentContainer.addStyleName("bh_tab_content_container");
		this.addStyleName("tab_panel");
		
		m_tabContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		
		updateLayout();
		
		m_tabContainerWrapper.add(m_tabContainer);
		this.add(m_tabContainerWrapper);
		this.add(m_contentContainer);
		
		int tabIndex = 0;
		m_accountTabButton = this.addTab("Account", "", tabIndex++);
//		//this.addTab("Bookmarks", "View and edit your bookmarks.", tabIndex++);
		this.addTab("HTML", "View or edit the html of a cell.", tabIndex++);
		
		this.updateAccountTabToolTip();
	}
	
	private void updateLayout()
	{
		double tabHeight = TAB_HEIGHT-1;
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
	
	private bhButtonWithText addTab(String tabTitle, String toolTipText, final int tabIndex)
	{
		bhButtonWithText tab = new bhButtonWithText();
		tab.addStyleName("bh_tab");
		tab.setText(tabTitle); 
		bh_c.clickMngr.addClickHandler(tab, new bhI_ClickHandler()
		{
			public void onClick()
			{
				m_args_SelectTab.setIndex(tabIndex);
				bhA_Action.perform(StateMachine_Tabs.SelectTab.class, m_args_SelectTab);
			}
		});
		
		bh_c.toolTipMngr.addTip(tab, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, toolTipText));
		
		m_tabContainer.add(tab);
		
		return tab;
	}
	
	private void selectTab(int index)
	{
		bhI_TabContent newContent = m_tabContent[index];
		
		if( newContent == null )
		{
			switch(Tab.values()[index])
			{
				case ACCOUNT:
				{
					newContent = m_tabContent[index] = new bhAccountTab();
					break;
				}
				
				/*case BOOKMARKS:
				{
					newContent = m_tabContent[index] = new bhBookmarkTab();
					break;
				}*/
				
				case HTML:
				{
					newContent = m_tabContent[index] = new bhCodeEditorTab();
					break;
				}
			}
		}
		
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
	
	private void updateAccountTabToolTip()
	{
		bhToolTipManager tipManager = bh_c.toolTipMngr;
		
		if( bh_c.accountMngr.isSignedIn() )
		{
			tipManager.addTip(m_accountTabButton, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Manage your account."));
		}
		else
		{
			tipManager.addTip(m_accountTabButton, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Sign in or sign up."));
		}
	}
	
	@Override
	public void onStateEvent(bhStateEvent event)
	{
		switch( event.getType() )
		{
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof State_SignInOrUp || event.getState() instanceof State_ManageAccount )
				{
					updateAccountTabToolTip();
				}
				
				break;
			}
			
			case DID_ENTER:
			{
				if( event.getState().getParent() instanceof StateMachine_Tabs )
				{
					int tabIndex = ((StateMachine_Tabs)event.getState().getParent()).calcTabIndex(event.getState().getClass());
					this.selectTab(tabIndex);
				}
				
				break;
			}
			
			case DID_EXIT:
			{
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == StateMachine_Base.OnAccountManagerResponse.class )
				{
					updateAccountTabToolTip();
					
					if( bhA_State.isForegrounded(StateMachine_Tabs.class) )
					{
						if( !bhA_State.isForegrounded(StateMachine_Account.class) )
						{
							StateMachine_Base.OnAccountManagerResponse.Args args = event.getActionArgs();
							bhClientAccountManager.E_ResponseType responseType = args.getType();
							String text = bhU_ToString.get(responseType);
							
							if( text != null )
							{
								bhAlignmentDefinition alignment = bhU_Alignment.createHorRightVerCenter(bhS_UI.TOOl_TIP_PADDING);
								bhE_ToolTipMood severity = responseType.isGood() ? bhE_ToolTipMood.PAT_ON_BACK : bhE_ToolTipMood.OOPS;
								bhToolTipConfig config = new bhToolTipConfig(bhE_ToolTipType.NOTIFICATION, alignment, text, severity);
								bh_c.toolTipMngr.addTip(m_accountTabButton, config);
							}
						}
					}
				}
				
				break;
			}
		}
		
		/*if( m_currentContent != null )
		{
			m_currentContent.onStateEvent(event);
		}*/
		
		for( int i = 0; i < m_tabContent.length; i++ )
		{
			if( m_tabContent[i] != null )
			{
				m_tabContent[i].onStateEvent(event);
			}
		}
	}
}
