package swarm.client.view.tabs.code;

import java.util.logging.Logger;

import swarm.client.app.smAppContext;
import swarm.client.entities.smA_ClientUser;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smUserManager;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.account.Action_ManageAccount_SignOut;
import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.account.State_AccountStatusPending;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.states.code.StateMachine_EditingCode;
import swarm.client.states.code.State_EditingCode;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateMachine;
import swarm.shared.statemachine.smI_StateEventListener;
import swarm.shared.statemachine.smStateEvent;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class smCodeEditor extends FlowPanel implements smI_StateEventListener
{
	private static final Logger s_logger = Logger.getLogger(smCodeEditor.class.getName());
	
	private final smCodeMirrorWrapper m_codeMirror_writable;
	private final smCodeMirrorWrapper m_codeMirror_readOnly;
	private smCodeMirrorWrapper m_currentCodeMirror = null;
	private final smUserManager m_userMngr;
	
	smCodeEditor(smUserManager userMngr, smI_CodeMirrorListener listener)
	{
		m_userMngr = userMngr;
		m_codeMirror_writable = new smCodeMirrorWrapper(listener, false);
		m_codeMirror_readOnly = new smCodeMirrorWrapper(null, true);

		this.setStyleName("sm_button_tray_ceiling");
		
		m_codeMirror_readOnly.getElement().getStyle().setDisplay(Display.NONE);
		m_codeMirror_writable.getElement().getStyle().setDisplay(Display.NONE);
		//m_codeMirror_readOnly.setVisible(false);
		//m_codeMirror_writable.setVisible(false);
		
		this.add(m_codeMirror_writable);
		this.add(m_codeMirror_readOnly);
		
		setActiveCodeMirrorInstance(m_codeMirror_readOnly);
	}
	
	private void setActiveCodeMirrorInstance(smCodeMirrorWrapper activeInstance)
	{
		if( activeInstance == m_currentCodeMirror )  return;
		
		if( m_currentCodeMirror != null )
		{
			m_currentCodeMirror.getElement().getStyle().setDisplay(Display.NONE);
		}
		
		m_currentCodeMirror = activeInstance;
		
		m_currentCodeMirror.getElement().getStyle().clearDisplay();
	}
	
	public void setContent(String content)
	{
		//TODO: This seems to get called more than it has to when snapping from cell to cell.
		//UPDATE: The above comment might be invalid because of lots of changes made to html editing view, but it still needs to be tested.
		
		m_currentCodeMirror.setContent(content);
	}
	
	public String getContent()
	{
		return m_currentCodeMirror.getContent();
	}
	
	private void toggleActiveCodeMirrorInstance(State_ViewingCell viewingState )
	{
		smA_ClientUser user = m_userMngr.getUser();
		
		if( user.isCellOwner(viewingState.getCell().getCoordinate()) )
		{
			this.setActiveCodeMirrorInstance(m_codeMirror_writable);
		}
		else
		{
			this.setActiveCodeMirrorInstance(m_codeMirror_readOnly);
		}

		m_currentCodeMirror.refresh();
	}
	
	void updateLayout()
	{
		int height = this.getOffsetHeight() - 1;
		m_codeMirror_writable.setCodeMirrorHeight(height + "px");
		m_codeMirror_readOnly.setCodeMirrorHeight(height + "px");
	}

	@Override
	public void onStateEvent(smStateEvent event)
	{
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_ViewingCell )
				{
					this.toggleActiveCodeMirrorInstance((State_ViewingCell) event.getState());
					
					StateMachine_EditingCode editingState = event.getContext().getEnteredState(StateMachine_EditingCode.class);
					
					if( editingState != null )
					{
						this.toggleActiveCodeMirrorInstance((State_ViewingCell) event.getState());
					}
				}
				else if( event.getState() instanceof StateMachine_EditingCode )
				{
					State_ViewingCell viewingState = event.getContext().getEnteredState(State_ViewingCell.class);
					
					if( viewingState != null )
					{
						this.toggleActiveCodeMirrorInstance(viewingState);
						
						if( ((smA_StateMachine)event.getState()).getCurrentState() instanceof State_EditingCode )
						{
							m_currentCodeMirror.focus();
						}
					}
				}
				
				break;
			}
		
			case DID_EXIT:
			{
				if( event.getState() instanceof State_ViewingCell )
				{
					m_currentCodeMirror.setContent("");
					m_currentCodeMirror.blur();
					
					this.setActiveCodeMirrorInstance(m_codeMirror_readOnly);
					
					m_currentCodeMirror.setContent("");
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == StateMachine_Base.OnUserPopulated.class || 
					event.getAction() == StateMachine_Base.OnUserCleared.class  )
				{
					State_ViewingCell viewingState = event.getContext().getEnteredState(State_ViewingCell.class);
					State_EditingCode editingState = event.getContext().getEnteredState(State_EditingCode.class);
					
					if( viewingState != null && editingState != null )
					{
						toggleActiveCodeMirrorInstance(viewingState);
					}
				}
				else if( event.getAction() == Action_ManageAccount_SignOut.class )
				{
					setActiveCodeMirrorInstance(m_codeMirror_readOnly);
				}
				
				break;
			}
		}
	}
}
