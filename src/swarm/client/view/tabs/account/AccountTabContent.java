package swarm.client.view.tabs.account;

import java.util.logging.Logger;

import swarm.client.entities.BufferCell;
import swarm.client.entities.A_ClientUser;
import swarm.client.states.StateMachine_Tabs;
import swarm.client.states.account.State_AccountStatusPending;
import swarm.client.states.account.State_ManageAccount;
import swarm.client.states.account.State_SignInOrUp;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.states.code.State_EditingCode;
import swarm.client.states.code.State_EditingCodeBlocker;
import swarm.client.view.ConsoleBlocker;
import swarm.client.view.I_UIElement;
import swarm.client.view.SplitPanel;
import swarm.client.view.ViewContext;
import swarm.client.view.tabs.I_TabContent;
import swarm.client.view.tabs.code.CodeMirrorWrapper;
import swarm.client.view.tabs.code.I_CodeMirrorListener;
import swarm.client.view.tooltip.ToolTipManager;
import swarm.client.view.widget.BaseButton;
import swarm.shared.app.S_CommonApp;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.E_CodeType;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.StateEvent;
import swarm.shared.structs.GridCoordinate;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class AccountTabContent extends AbsolutePanel implements I_TabContent
{
	private static final Logger s_logger = Logger.getLogger(AccountTabContent.class.getName());
	
	private final SignInOrUpPanel m_signUpOrIn;
	private final ManageAccountPanel m_manage;
	private final ViewContext m_viewContext;
	
	public AccountTabContent(ViewContext viewContext)
	{
		m_viewContext = viewContext;
		
		this.addStyleName("sm_account_tab");
		
		m_signUpOrIn = new SignInOrUpPanel(viewContext);
		m_manage = new ManageAccountPanel(viewContext);
	}
	
	@Override
	public void onStateEvent(StateEvent event)
	{
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_SignInOrUp )
				{
					U_Debug.ASSERT(m_signUpOrIn.getParent() == null);
					
					this.add(m_signUpOrIn);
				}
				else if( event.getState() instanceof State_ManageAccount )
				{
					U_Debug.ASSERT(m_manage.getParent() == null);
					
					this.add(m_manage);
				}
				
				break;
			}
			
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof State_AccountStatusPending )
				{
					m_viewContext.consoleBlocker.attachTo(this);
					m_viewContext.consoleBlocker.setHtml("Wait a second...");
				}
				else if( event.getState() instanceof StateMachine_Tabs )
				{
					if( event.getRevealingState() == null )
					{
						//--- DRK > This goes along with the sleight of hand we pull below for not detaching the blocker while animating out.
						//---		This just makes sure that the console blocker gets detached...it might be the case that it gets immediately
						//---		reattached.
						m_viewContext.consoleBlocker.detachFrom(this);
					}
				}
			
				break;
			}
			
			case DID_BACKGROUND:
			{
				
				break;
			}
			
			case DID_EXIT:
			{
				if( event.getState() instanceof State_SignInOrUp )
				{
					U_Debug.ASSERT(m_signUpOrIn.getParent() == this);
					
					this.remove(m_signUpOrIn);
				}
				else if( event.getState() instanceof State_ManageAccount )
				{
					U_Debug.ASSERT(m_manage.getParent() == this);
					
					this.remove(m_manage);
				}
				else if( event.getState() instanceof State_AccountStatusPending )
				{
					//--- DRK > Pulling a little sleight of hand here so that we don't remove the blocker if it appears
					//---		that the console is being animated out.
					if( event.getContext().isForegrounded(StateMachine_Tabs.class) )
					{
						//--- DRK > This should be called in the "exit" event because background event could be called for something
						//---		like an error dialog being pushed over the topmost state in the machine.
						//---		Other tabs needing the console blocker will simply take over if they need it anyway.
						m_viewContext.consoleBlocker.detachFrom(this);
					}
				}
				break;
			}
		}
		
		m_signUpOrIn.onStateEvent(event);
		m_manage.onStateEvent(event);
	}

	@Override
	public void onResize()
	{
		//TODO: Don't call this if not being displayed.
		m_signUpOrIn.onResize();
		m_manage.onResize();
	}

	@Override
	public void onSelect()
	{
		// TODO Auto-generated method stub
	}
}
