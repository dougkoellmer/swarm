package swarm.client.view.tabs.account;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import swarm.client.app.AppContext;
import swarm.client.managers.ClientAccountManager;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.StateMachine_Tabs;
import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.account.State_ManageAccount;
import swarm.client.states.account.State_SignInOrUp;
import swarm.client.view.S_UI;
import swarm.client.view.U_ToString;
import swarm.client.view.ViewContext;
import swarm.client.view.alignment.AlignmentDefinition;
import swarm.client.view.alignment.U_Alignment;
import swarm.client.view.tabs.A_Tab;
import swarm.client.view.tabs.I_Tab;
import swarm.client.view.tabs.I_TabContent;
import swarm.client.view.tabs.code.CodeEditorTabContent;
import swarm.client.view.tooltip.E_ToolTipMood;
import swarm.client.view.tooltip.E_ToolTipType;
import swarm.client.view.tooltip.ToolTipConfig;
import swarm.client.view.tooltip.ToolTipManager;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.A_BaseStateEvent;
import swarm.shared.statemachine.ActionEvent;

public class AccountTab extends A_Tab
{
	private final ViewContext m_viewContext;
	
	public AccountTab(ViewContext viewContext)
	{
		super("Account", "", new AccountTabContent(viewContext));
		
		m_viewContext = viewContext;
	}
	
	private void updateToolTip()
	{
		ToolTipManager tipManager = m_viewContext.toolTipMngr;
		
		if( m_viewContext.appContext.accountMngr.isSignedIn() )
		{
			tipManager.addTip(m_tabButton, new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Manage your account."));
		}
		else
		{
			tipManager.addTip(m_tabButton, new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Sign in or sign up."));
		}
	}
	
	@Override
	public void onAttached(IsWidget tabButton)
	{
		super.onAttached(tabButton);
		
		updateToolTip();
	}

	@Override
	public void onStateEvent(A_BaseStateEvent event)
	{
		super.onStateEvent(event);
		
		switch( event.getType() )
		{
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof State_SignInOrUp || event.getState() instanceof State_ManageAccount )
				{
					updateToolTip();
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getTargetClass() == StateMachine_Base.OnAccountManagerResponse.class )
				{
					updateToolTip();
					
					if( event.getContext().isForegrounded(StateMachine_Tabs.class) )
					{
						if( !event.getContext().isForegrounded(StateMachine_Account.class) )
						{
							ActionEvent event_cast = event.cast();
							StateMachine_Base.OnAccountManagerResponse.Args args = event_cast.getArgsIn();
							ClientAccountManager.E_ResponseType responseType = args.getType();
							String text = U_ToString.get(responseType);
							
							if( text != null )
							{
								AlignmentDefinition alignment = U_Alignment.createHorRightVerCenter(S_UI.TOOl_TIP_PADDING);
								E_ToolTipMood severity = responseType.isGood() ? E_ToolTipMood.PAT_ON_BACK : E_ToolTipMood.OOPS;
								ToolTipConfig config = new ToolTipConfig(E_ToolTipType.NOTIFICATION, alignment, text, severity);
								m_viewContext.toolTipMngr.addTip(m_tabButton, config);
							}
						}
					}
				}
				
				break;
			}
		}
	}
}
