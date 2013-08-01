package com.b33hive.client.ui.tabs.account;

import com.b33hive.client.states.StateMachine_Tabs;
import com.b33hive.client.states.account.StateMachine_Account;
import com.b33hive.client.states.account.State_AccountStatusPending;
import com.b33hive.client.states.account.State_SignInOrUp;
import com.b33hive.client.ui.bhConsoleBlocker;
import com.b33hive.client.ui.bhSplitPanel;
import com.b33hive.client.ui.tooltip.bhToolTipManager;
import com.b33hive.client.ui.widget.bhButton;
import com.b33hive.client.ui.widget.bhDefaultButton;
import com.b33hive.client.ui.widget.bhI_TextBoxChangeListener;
import com.b33hive.client.ui.widget.bhPasswordTextBox;
import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.account.bhE_SignUpValidationError;
import com.b33hive.shared.statemachine.bhA_Action;
import com.b33hive.shared.statemachine.bhI_StateEventListener;
import com.b33hive.shared.statemachine.bhStateEvent;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class bhSignInOrUpPanel extends FlowPanel implements bhI_StateEventListener, ScrollHandler
{
	static final int MIN_SUB_PANEL_MARGIN = 20;
	
	private final AbsolutePanel m_innerContainer = new AbsolutePanel();
	
	private final bhSignInPanel m_signIn = new bhSignInPanel();
	private final bhSignUpPanel m_signUp = new bhSignUpPanel();
	
	public bhSignInOrUpPanel()
	{
		this.addStyleName("bh_signuporin_panel");
		m_innerContainer.setSize("100%", "100%");
		
		m_innerContainer.add(m_signIn);
		m_innerContainer.add(m_signUp);
		
		this.add(m_innerContainer);
		
		this.addDomHandler(this, ScrollEvent.getType());
	}
	
	@Override
	public void onLoad()
	{
		super.onLoad();
		
		this.onResize();
	}
	
	public void onResize()
	{
		int subPanelWidth = m_signIn.getOffsetWidth();
		int signInHeight = m_signIn.getOffsetHeight();
		int signUpHeight = m_signUp.getOffsetHeight();
		int thisHeight = this.getOffsetHeight();
		int thisWidth = (int) bhSplitPanel.getInstance().getTabPanelWidth(); // sigh...
		int minLayoutHeight = 3 * MIN_SUB_PANEL_MARGIN + signInHeight + signUpHeight;
		int minLayoutWidth = subPanelWidth;
		int left = thisWidth/2 - subPanelWidth/2;
		
		left = left < 0 ? 0 : left;
		
		int margin = 0; 
	
		if( minLayoutHeight >= thisHeight )
		{
			margin = MIN_SUB_PANEL_MARGIN;
			
			m_innerContainer.setHeight(minLayoutHeight + "px");
		}
		else
		{
			margin = (thisHeight - (signInHeight + signUpHeight)) / 3;
			
			m_innerContainer.setHeight("100%");
		}
		
		if( minLayoutWidth >= thisWidth )
		{
			m_innerContainer.setWidth(minLayoutWidth + "px");
		}
		else
		{
			m_innerContainer.setWidth("100%");
		}
		
		m_innerContainer.setWidgetPosition(m_signIn, left, margin);
		m_innerContainer.setWidgetPosition(m_signUp, left, margin*2 + signInHeight);
		
		m_signUp.onResizeOrScroll();
	}

	@Override
	public void onStateEvent(bhStateEvent event)
	{
		switch(event.getType() )
		{
			case DID_BACKGROUND:
			{
				if( event.getState() instanceof StateMachine_Account )
				{
					m_signUp.blur();
				}
				
				break;
			}
		}
		
		m_signIn.onStateEvent(event);
		m_signUp.onStateEvent(event);
	}

	@Override
	public void onScroll(ScrollEvent event)
	{
		m_signUp.onResizeOrScroll();
	}
}
