package swarm.client.view.tabs.account;

import swarm.client.app.smAppContext;
import swarm.client.input.smClickManager;
import swarm.client.input.smI_ClickHandler;
import swarm.client.managers.smClientAccountManager;
import swarm.client.states.account.Action_ManageAccount_SignOut;
import swarm.client.states.account.State_ManageAccount;
import swarm.client.structs.smAccountInfo;
import swarm.client.view.smS_UI;
import swarm.client.view.smSplitPanel;
import swarm.client.view.smViewContext;
import swarm.client.view.tabs.smTabPanel;
import swarm.client.view.tooltip.smE_ToolTipType;
import swarm.client.view.tooltip.smToolTipConfig;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.client.view.widget.smDefaultButton;
import swarm.shared.code.smU_Code;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smI_StateEventListener;
import swarm.shared.statemachine.smStateEvent;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class smManageAccountPanel extends FlowPanel implements smI_StateEventListener
{
	private final FlowPanel m_contentPanel = new FlowPanel();
		private final HorizontalPanel m_centeringPanel = new HorizontalPanel();
		private final Label m_welcomeLabel = new Label();
		
	private final HorizontalPanel m_buttonTray = new HorizontalPanel();
		private final FlowPanel m_buttonTrayWrapper = new FlowPanel();
			private final smDefaultButton m_signOutButton = new smDefaultButton();
	
	private final smViewContext m_viewContext;
	
	public smManageAccountPanel(smViewContext viewContext)
	{
		m_viewContext = viewContext;
		
		m_contentPanel.getElement().getStyle().setOverflow(Overflow.AUTO);
		
		m_buttonTrayWrapper.addStyleName("sm_button_tray_wrapper");
		m_buttonTray.addStyleName("sm_button_tray");
		m_contentPanel.addStyleName("sm_button_tray_ceiling");
		m_buttonTray.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		m_centeringPanel.setSize("100%", "100%");
		
		m_welcomeLabel.setWordWrap(false);
		m_welcomeLabel.addStyleName("sm_welcome_text");
		
		m_signOutButton.setText("Sign Out");
		
		m_viewContext.toolTipMngr.addTip(m_signOutButton, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Don't do it!"));
		
		m_viewContext.clickMngr.addClickHandler(m_signOutButton, new smI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				m_viewContext.stateContext.performAction(Action_ManageAccount_SignOut.class);
			}
		});
		
		m_buttonTray.add(m_signOutButton);
		
		m_buttonTrayWrapper.add(m_buttonTray);
		
		m_centeringPanel.add(m_welcomeLabel);
		m_centeringPanel.setCellHorizontalAlignment(m_welcomeLabel, HasHorizontalAlignment.ALIGN_CENTER);
		m_centeringPanel.setCellVerticalAlignment(m_welcomeLabel, HasVerticalAlignment.ALIGN_MIDDLE);
		
		m_contentPanel.add(m_centeringPanel);
		
		this.add(m_contentPanel);
		
		this.add(m_buttonTrayWrapper);
	}
	
	@Override
	public void onLoad()
	{
		super.onLoad();
		
		this.updateLayout();
	}
	
	public void onResize()
	{
		this.updateLayout();
	}
	
	private void updateLayout()
	{
		double contentHeight = RootPanel.get().getOffsetHeight() - smTabPanel.TAB_HEIGHT*2 - smS_UI.MAGIC_UI_SPACING*2;
		m_contentPanel.setSize(m_viewContext.splitPanel.getTabPanelWidth() + "px", contentHeight + "px");
	}

	@Override
	public void onStateEvent(smStateEvent event)
	{
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_ManageAccount )
				{
					smAccountInfo info = m_viewContext.appContext.accountMngr.getAccountInfo();
					String username = info.get(smAccountInfo.Type.USERNAME);
					String welcomeText = "Welcome, " + username + ".";
					String href = smU_Code.transformPathToJavascript(m_viewContext.appConfig.appId, username);
					welcomeText += "<br><br><a href=\""+href+"\">Click here</a> to visit your cell.";
					m_welcomeLabel.getElement().setInnerHTML(welcomeText);
				}
				
				break;
			}
		}
	}
}
