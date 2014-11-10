package swarm.client.view.tabs.account;

import swarm.client.app.AppContext;
import swarm.client.input.ClickManager;
import swarm.client.input.I_ClickHandler;
import swarm.client.managers.ClientAccountManager;
import swarm.client.states.account.Action_ManageAccount_SignOut;
import swarm.client.states.account.State_ManageAccount;
import swarm.client.structs.AccountInfo;
import swarm.client.view.S_UI;
import swarm.client.view.SplitPanel;
import swarm.client.view.ViewContext;
import swarm.client.view.tabs.TabPanel;
import swarm.client.view.tooltip.E_ToolTipType;
import swarm.client.view.tooltip.ToolTipConfig;
import swarm.client.view.tooltip.ToolTipManager;
import swarm.client.view.widget.DefaultButton;
import swarm.shared.code.U_Code;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.I_StateEventListener;
import swarm.shared.statemachine.A_BaseStateEvent;
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

public class ManageAccountPanel extends FlowPanel implements I_StateEventListener
{
	private final FlowPanel m_contentPanel = new FlowPanel();
		private final HorizontalPanel m_centeringPanel = new HorizontalPanel();
		private final Label m_welcomeLabel = new Label();
		
	private final HorizontalPanel m_buttonTray = new HorizontalPanel();
		private final FlowPanel m_buttonTrayWrapper = new FlowPanel();
			private final DefaultButton m_signOutButton = new DefaultButton();
	
	private final ViewContext m_viewContext;
	
	public ManageAccountPanel(ViewContext viewContext)
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
		
		m_viewContext.toolTipMngr.addTip(m_signOutButton, new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Don't do it!"));
		
		m_viewContext.clickMngr.addClickHandler(m_signOutButton, new I_ClickHandler()
		{
			@Override
			public void onClick(int x, int y)
			{
				m_viewContext.stateContext.perform(Action_ManageAccount_SignOut.class);
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
		double contentHeight = RootPanel.get().getOffsetHeight() - TabPanel.TAB_HEIGHT*2 - S_UI.MAGIC_UI_SPACING*2;
		m_contentPanel.setSize(m_viewContext.splitPanel.getTabPanelWidth() + "px", contentHeight + "px");
	}

	@Override
	public void onStateEvent(A_BaseStateEvent event)
	{
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_ManageAccount )
				{
					AccountInfo info = m_viewContext.appContext.accountMngr.getAccountInfo();
					String username = info.get(AccountInfo.Type.USERNAME);
					String welcomeText = "Welcome, " + username + ".";
					String href = U_Code.transformPathToJavascript(m_viewContext.appConfig.appId, username);
					welcomeText += "<br><br><a href=\""+href+"\">Click here</a> to visit your cell.";
					m_welcomeLabel.getElement().setInnerHTML(welcomeText);
				}
				
				break;
			}
		}
	}
}
