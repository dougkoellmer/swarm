package swarm.client.ui.tabs.code;

import java.util.logging.Logger;

import swarm.client.app.sm_c;
import swarm.client.entities.bhA_ClientUser;
import swarm.client.managers.bhClientAccountManager;
import swarm.client.managers.bhUserManager;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.account.State_AccountStatusPending;
import swarm.client.states.account.State_ManageAccount;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.states.code.StateMachine_EditingCode;
import swarm.client.states.code.State_EditingCode;
import swarm.shared.statemachine.bhA_Action;
import swarm.shared.statemachine.bhA_State;
import swarm.shared.statemachine.bhA_StateMachine;
import swarm.shared.statemachine.bhI_StateEventListener;
import swarm.shared.statemachine.bhStateEvent;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class bhCodeEditor extends FlowPanel implements bhI_StateEventListener
{
	private static final Logger s_logger = Logger.getLogger(bhCodeEditor.class.getName());
	
	private final bhCodeMirrorWrapper m_codeMirror_writable;
	private final bhCodeMirrorWrapper m_codeMirror_readOnly;
	private bhCodeMirrorWrapper m_currentCodeMirror = null;
	
	bhCodeEditor(bhI_CodeMirrorListener listener)
	{
		m_codeMirror_writable = new bhCodeMirrorWrapper(listener, false);
		m_codeMirror_readOnly = new bhCodeMirrorWrapper(null, true);

		this.setStyleName("sm_button_tray_ceiling");
		
		m_codeMirror_readOnly.getElement().getStyle().setDisplay(Display.NONE);
		m_codeMirror_writable.getElement().getStyle().setDisplay(Display.NONE);
		//m_codeMirror_readOnly.setVisible(false);
		//m_codeMirror_writable.setVisible(false);
		
		this.add(m_codeMirror_writable);
		this.add(m_codeMirror_readOnly);
		
		setActiveCodeMirrorInstance(m_codeMirror_readOnly);
	}
	
	private void setActiveCodeMirrorInstance(bhCodeMirrorWrapper activeInstance)
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
		bhA_ClientUser user = sm_c.userMngr.getUser();
		
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
	public void onStateEvent(bhStateEvent event)
	{
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_ViewingCell )
				{
					this.toggleActiveCodeMirrorInstance((State_ViewingCell) event.getState());
					
					StateMachine_EditingCode editingState = bhA_State.getEnteredInstance(StateMachine_EditingCode.class);
					
					if( editingState != null )
					{
						this.toggleActiveCodeMirrorInstance((State_ViewingCell) event.getState());
					}
				}
				else if( event.getState() instanceof StateMachine_EditingCode )
				{
					State_ViewingCell viewingState = bhA_State.getEnteredInstance(State_ViewingCell.class);
					
					if( viewingState != null )
					{
						this.toggleActiveCodeMirrorInstance(viewingState);
						
						if( ((bhA_StateMachine)event.getState()).getCurrentState() instanceof State_EditingCode )
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
					State_ViewingCell viewingState = bhA_State.getEnteredInstance(State_ViewingCell.class);
					State_EditingCode editingState = bhA_State.getEnteredInstance(State_EditingCode.class);
					
					if( viewingState != null && editingState != null )
					{
						toggleActiveCodeMirrorInstance(viewingState);
					}
				}
				else if( event.getAction() == State_ManageAccount.SignOut.class )
				{
					setActiveCodeMirrorInstance(m_codeMirror_readOnly);
				}
				
				break;
			}
		}
	}
}
