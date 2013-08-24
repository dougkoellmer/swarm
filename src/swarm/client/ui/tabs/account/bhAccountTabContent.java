package swarm.client.ui.tabs.account;

import java.util.logging.Logger;

import swarm.client.entities.bhBufferCell;
import swarm.client.entities.bhA_ClientUser;
import swarm.client.states.StateMachine_Tabs;
import swarm.client.states.account.State_AccountStatusPending;
import swarm.client.states.account.State_ManageAccount;
import swarm.client.states.account.State_SignInOrUp;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.states.code.State_EditingCode;
import swarm.client.states.code.State_EditingCodeBlocker;
import swarm.client.ui.bhConsoleBlocker;
import swarm.client.ui.bhI_UIElement;
import swarm.client.ui.bhSplitPanel;
import swarm.client.ui.tabs.bhI_TabContent;
import swarm.client.ui.tabs.code.bhCodeMirrorWrapper;
import swarm.client.ui.tabs.code.bhI_CodeMirrorListener;
import swarm.client.ui.tooltip.bhToolTipManager;
import swarm.client.ui.widget.bhButton;
import swarm.shared.app.bhS_App;
import swarm.shared.debugging.bhU_Debug;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.statemachine.bhA_Action;
import swarm.shared.statemachine.bhA_State;
import swarm.shared.statemachine.bhStateEvent;
import swarm.shared.structs.bhGridCoordinate;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class bhAccountTabContent extends AbsolutePanel implements bhI_TabContent
{
	private static final Logger s_logger = Logger.getLogger(bhAccountTabContent.class.getName());
	
	private final bhSignInOrUpPanel m_signUpOrIn = new bhSignInOrUpPanel();
	private final bhManageAccountPanel m_manage = new bhManageAccountPanel();
	
	public bhAccountTabContent()
	{
		this.addStyleName("sm_account_tab");
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
