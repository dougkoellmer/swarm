package b33hive.client.ui.tabs.account;

import b33hive.client.app.bhS_ClientApp;
import b33hive.client.input.bhClickManager;
import b33hive.client.input.bhI_ClickHandler;
import b33hive.client.managers.bhClientAccountManager;
import b33hive.client.states.account.State_ManageAccount;
import b33hive.client.structs.bhAccountInfo;
import b33hive.client.ui.bhSplitPanel;
import b33hive.client.ui.tabs.bhTabPanel;
import b33hive.client.ui.tooltip.bhE_ToolTipType;
import b33hive.client.ui.tooltip.bhToolTipConfig;
import b33hive.client.ui.tooltip.bhToolTipManager;
import b33hive.client.ui.widget.bhDefaultButton;
import b33hive.shared.code.bhU_Code;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhI_StateEventListener;
import b33hive.shared.statemachine.bhStateEvent;
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

public class bhManageAccountPanel extends FlowPanel implements bhI_StateEventListener
{
	private final FlowPanel m_contentPanel = new FlowPanel();
		private final HorizontalPanel m_centeringPanel = new HorizontalPanel();
		private final Label m_welcomeLabel = new Label();
		
	private final HorizontalPanel m_buttonTray = new HorizontalPanel();
		private final FlowPanel m_buttonTrayWrapper = new FlowPanel();
			private final bhDefaultButton m_signOutButton = new bhDefaultButton();
	
	public bhManageAccountPanel()
	{
		m_contentPanel.getElement().getStyle().setOverflow(Overflow.AUTO);
		
		m_buttonTrayWrapper.addStyleName("bh_button_tray_wrapper");
		m_buttonTray.addStyleName("bh_button_tray");
		m_contentPanel.addStyleName("bh_button_tray_ceiling");
		m_buttonTray.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		m_centeringPanel.setSize("100%", "100%");
		
		m_welcomeLabel.setWordWrap(false);
		m_welcomeLabel.addStyleName("bh_welcome_text");
		
		m_signOutButton.setText("Sign Out");
		
		bhToolTipManager.getInstance().addTip(m_signOutButton, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Don't do it!"));
		
		bhClickManager.getInstance().addClickHandler(m_signOutButton, new bhI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				bhA_Action.perform(State_ManageAccount.SignOut.class);
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
		double contentHeight = RootPanel.get().getOffsetHeight() - bhTabPanel.TAB_HEIGHT*2 - bhS_ClientApp.MAGIC_UI_SPACING*2;
		m_contentPanel.setSize(bhSplitPanel.getInstance().getTabPanelWidth() + "px", contentHeight + "px");
	}

	@Override
	public void onStateEvent(bhStateEvent event)
	{
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_ManageAccount )
				{
					bhAccountInfo info = bhClientAccountManager.getInstance().getAccountInfo();
					String username = info.get(bhAccountInfo.Type.USERNAME);
					String welcomeText = "Welcome, " + username + ".";
					String href = bhU_Code.transformPathToJavascript(username);
					welcomeText += "<br><br><a href=\""+href+"\">Click here</a> to visit your cell.";
					m_welcomeLabel.getElement().setInnerHTML(welcomeText);
				}
				
				break;
			}
		}
	}
}
