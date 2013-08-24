package swarm.client.ui.tabs.account;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import swarm.client.app.sm_c;
import swarm.client.managers.bhClientAccountManager;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.StateMachine_Tabs;
import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.account.State_ManageAccount;
import swarm.client.states.account.State_SignInOrUp;
import swarm.client.ui.bhS_UI;
import swarm.client.ui.bhU_ToString;
import swarm.client.ui.alignment.bhAlignmentDefinition;
import swarm.client.ui.alignment.bhU_Alignment;
import swarm.client.ui.tabs.bhA_Tab;
import swarm.client.ui.tabs.bhI_Tab;
import swarm.client.ui.tabs.bhI_TabContent;
import swarm.client.ui.tabs.code.bhCodeEditorTabContent;
import swarm.client.ui.tooltip.bhE_ToolTipMood;
import swarm.client.ui.tooltip.bhE_ToolTipType;
import swarm.client.ui.tooltip.bhToolTipConfig;
import swarm.client.ui.tooltip.bhToolTipManager;
import swarm.shared.statemachine.bhA_State;
import swarm.shared.statemachine.bhStateEvent;

public class bhAccountTab extends bhA_Tab
{
	public bhAccountTab()
	{
		super("Account", "", new bhAccountTabContent());
	}
	
	private void updateToolTip()
	{
		bhToolTipManager tipManager = sm_c.toolTipMngr;
		
		if( sm_c.accountMngr.isSignedIn() )
		{
			tipManager.addTip(m_tabButton, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Manage your account."));
		}
		else
		{
			tipManager.addTip(m_tabButton, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Sign in or sign up."));
		}
	}
	
	@Override
	public void onAttached(IsWidget tabButton)
	{
		super.onAttached(tabButton);
		
		updateToolTip();
	}

	@Override
	public void onStateEvent(bhStateEvent event)
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
