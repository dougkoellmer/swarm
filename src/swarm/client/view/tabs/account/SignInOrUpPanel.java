package swarm.client.view.tabs.account;

import swarm.client.states.StateMachine_Tabs;
import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.account.State_AccountStatusPending;
import swarm.client.states.account.State_SignInOrUp;
import swarm.client.view.ConsoleBlocker;
import swarm.client.view.SplitPanel;
import swarm.client.view.ViewContext;
import swarm.client.view.tooltip.ToolTipManager;
import swarm.client.view.widget.BaseButton;
import swarm.client.view.widget.DefaultButton;
import swarm.client.view.widget.I_TextBoxChangeListener;
import swarm.client.view.widget.PasswordTextField;
import swarm.shared.app.S_CommonApp;
import swarm.shared.account.E_SignUpValidationError;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.I_StateEventListener;
import swarm.shared.statemachine.StateEvent;
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

public class SignInOrUpPanel extends FlowPanel implements I_StateEventListener, ScrollHandler
{
	static final int MIN_SUB_PANEL_MARGIN = 20;
	
	private final AbsolutePanel m_innerContainer = new AbsolutePanel();
	
	private final SignInPanel m_signIn;
	private final SignUpPanel m_signUp;
	
	private final ViewContext m_viewContext;
	
	public SignInOrUpPanel(ViewContext viewContext)
	{
		m_viewContext = viewContext;
		
		m_signIn = new SignInPanel(viewContext);
		m_signUp = new SignUpPanel(viewContext);
		
		this.addStyleName("sm_signuporin_panel");
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
		int thisWidth = (int) m_viewContext.splitPanel.getTabPanelWidth(); // sigh...
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
	public void onStateEvent(StateEvent event)
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
