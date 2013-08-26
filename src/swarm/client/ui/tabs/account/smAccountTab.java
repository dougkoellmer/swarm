package swarm.client.ui.tabs.account;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import swarm.client.app.sm_c;
import swarm.client.managers.smClientAccountManager;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.StateMachine_Tabs;
import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.account.State_ManageAccount;
import swarm.client.states.account.State_SignInOrUp;
import swarm.client.ui.smS_UI;
import swarm.client.ui.smU_ToString;
import swarm.client.ui.alignment.smAlignmentDefinition;
import swarm.client.ui.alignment.smU_Alignment;
import swarm.client.ui.tabs.smA_Tab;
import swarm.client.ui.tabs.smI_Tab;
import swarm.client.ui.tabs.smI_TabContent;
import swarm.client.ui.tabs.code.smCodeEditorTabContent;
import swarm.client.ui.tooltip.smE_ToolTipMood;
import swarm.client.ui.tooltip.smE_ToolTipType;
import swarm.client.ui.tooltip.smToolTipConfig;
import swarm.client.ui.tooltip.smToolTipManager;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smStateEvent;

public class smAccountTab extends smA_Tab
{
	public smAccountTab()
	{
		super("Account", "", new smAccountTabContent());
	}
	
	private void updateToolTip()
	{
		bhToolTipManager tipManager = sm_c.toolTipMngr;
		
		if( sm_c.accountMngr.isSignedIn() )
		{
			tipManager.addTip(m_tabButton, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Manage your account."));
		}
		else
		{
			tipManager.addTip(m_tabButton, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Sign in or sign up."));
		}
	}
	
	@Override
	public void onAttached(IsWidget tabButton)
	{
		super.onAttached(tabButton);
		
		updateToolTip();
	}

	@Override
	public void onStateEvent(smStateEvent event)
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
				if( event.getAction() == StateMachine_Base.OnAccountManagerResponse.class )
				{
					updateToolTip();
					
					if( smA_State.isForegrounded(StateMachine_Tabs.class) )
					{
						if( !smA_State.isForegrounded(StateMachine_Account.class) )
						{
							StateMachine_Base.OnAccountManagerResponse.Args args = event.getActionArgs();
							bhClientAccountManager.E_ResponseType responseType = args.getType();
							String text = bhU_ToString.get(responseType);
							
							if( text != null )
							{
								bhAlignmentDefinition alignment = bhU_Alignment.createHorRightVerCenter(smS_UI.TOOl_TIP_PADDING);
								smE_ToolTipMood severity = responseType.isGood() ? smE_ToolTipMood.PAT_ON_BACK : smE_ToolTipMood.OOPS;
								bhToolTipConfig config = new smToolTipConfig(smE_ToolTipType.NOTIFICATION, alignment, text, severity);
								sm_c.toolTipMngr.addTip(m_tabButton, config);
							}
						}
					}
				}
				
				break;
			}
		}
	}
}
