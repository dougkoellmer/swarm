package com.b33hive.client.ui.tabs.account;

import java.util.logging.Logger;

import com.b33hive.client.entities.bhBufferCell;
import com.b33hive.client.entities.bhClientUser;
import com.b33hive.client.states.StateMachine_Tabs;
import com.b33hive.client.states.account.State_AccountStatusPending;
import com.b33hive.client.states.account.State_ManageAccount;
import com.b33hive.client.states.account.State_SignInOrUp;
import com.b33hive.client.states.camera.State_CameraSnapping;
import com.b33hive.client.states.camera.State_ViewingCell;
import com.b33hive.client.states.code.State_EditingCode;
import com.b33hive.client.states.code.State_EditingCodeBlocker;
import com.b33hive.client.ui.bhConsoleBlocker;
import com.b33hive.client.ui.bhI_UIElement;
import com.b33hive.client.ui.bhSplitPanel;
import com.b33hive.client.ui.tabs.bhI_TabContent;
import com.b33hive.client.ui.tabs.code.bhCodeMirrorWrapper;
import com.b33hive.client.ui.tabs.code.bhI_CodeMirrorListener;
import com.b33hive.client.ui.tooltip.bhToolTipManager;
import com.b33hive.client.ui.widget.bhButton;
import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.debugging.bhU_Debug;
import com.b33hive.shared.entities.bhE_CodeType;
import com.b33hive.shared.statemachine.bhA_Action;
import com.b33hive.shared.statemachine.bhA_State;
import com.b33hive.shared.statemachine.bhStateEvent;
import com.b33hive.shared.structs.bhGridCoordinate;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class bhAccountTab extends AbsolutePanel implements bhI_TabContent
{
	private static final Logger s_logger = Logger.getLogger(bhAccountTab.class.getName());
	
	private final bhSignInOrUpPanel m_signUpOrIn = new bhSignInOrUpPanel();
	private final bhManageAccountPanel m_manage = new bhManageAccountPanel();
	
	public bhAccountTab()
	{
		this.addStyleName("bh_account_tab");
	}
	
	@Override
	public void onStateEvent(bhStateEvent event)
	{
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_SignInOrUp )
				{
					bhU_Debug.ASSERT(m_signUpOrIn.getParent() == null);
					
					this.add(m_signUpOrIn);
				}
				else if( event.getState() instanceof State_ManageAccount )
				{
					bhU_Debug.ASSERT(m_manage.getParent() == null);
					
					this.add(m_manage);
				}
				
				break;
			}
			
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof State_AccountStatusPending )
				{
					bhConsoleBlocker.getInstance().attachTo(this);
					bhConsoleBlocker.getInstance().setHtml("Wait a second...");
				}
				else if( event.getState() instanceof StateMachine_Tabs )
				{
					if( event.getRevealingState() == null )
					{
						//--- DRK > This goes along with the sleight of hand we pull below for not detaching the blocker while animating out.
						//---		This just makes sure that the console blocker gets detached...it might be the case that it gets immediately
						//---		reattached.
						bhConsoleBlocker.getInstance().detachFrom(this);
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
					bhU_Debug.ASSERT(m_signUpOrIn.getParent() == this);
					
					this.remove(m_signUpOrIn);
				}
				else if( event.getState() instanceof State_ManageAccount )
				{
					bhU_Debug.ASSERT(m_manage.getParent() == this);
					
					this.remove(m_manage);
				}
				else if( event.getState() instanceof State_AccountStatusPending )
				{
					//--- DRK > Pulling a little sleight of hand here so that we don't remove the blocker if it appears
					//---		that the console is being animated out.
					if( bhA_State.isForegrounded(StateMachine_Tabs.class) )
					{
						//--- DRK > This should be called in the "exit" event because background event could be called for something
						//---		like an error dialog being pushed over the topmost state in the machine.
						//---		Other tabs needing the console blocker will simply take over if they need it anyway.
						bhConsoleBlocker.getInstance().detachFrom(this);
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
