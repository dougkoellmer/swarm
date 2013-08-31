package swarm.client.states.code;

import swarm.client.managers.smCellCodeManager;
import swarm.client.managers.smUserManager;
import swarm.client.app.smAppContext;
import swarm.client.entities.smBufferCell;
import swarm.client.entities.smA_ClientUser;
import swarm.client.entities.smE_CodeStatus;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.camera.Action_ViewingCell_Refresh;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.states.code.State_EditingCodeBlocker.Reason;
import swarm.client.transaction.smClientTransactionManager;
import swarm.client.view.tabs.code.smCodeMirrorWrapper;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.statemachine.smA_Action;

import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateMachine;
import swarm.shared.statemachine.smI_StateEventListener;
import swarm.shared.statemachine.smA_StateConstructor;
import swarm.shared.statemachine.smStateEvent;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smGridCoordinate;


/**
 * ...
 * @author 
 */
public class StateMachine_EditingCode extends smA_StateMachine implements smI_StateEventListener
{
	private boolean m_waitingOnHtmlForViewedCell = false;
	
	smCode m_code = null;
	private final smAppContext m_appContext;
	
	public StateMachine_EditingCode(smAppContext appContext)
	{
		m_appContext = appContext;
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
		m_waitingOnHtmlForViewedCell = false;
		m_code = null;
	}
	
	smCode getCode()
	{
		return m_code;
	}
	
	@Override
	protected void didForeground(Class<? extends smA_State> revealingState, Object[] argsFromRevealingState)
	{
		//--- DRK > Camera controller can be null during start up...should be the only time.
		smA_StateMachine cameraController = smA_State.getEnteredInstance(StateMachine_Camera.class);
		if( cameraController != null )
		{
			pushOrPopBlocker(cameraController.getCurrentState());
		}
		else
		{
			pushOrPopBlocker(null);
		}
	}
	
	@Override 
	protected void willBackground(Class<? extends smA_State> blockingState)
	{
		if( blockingState == null )
		{
			if( this.getCurrentState() instanceof State_EditingCodeBlocker )
			{
				machine_popState(this, (Object) null);
			}
		}
	}
	
	void setBlockerReason(Reason reason)
	{
		smU_Debug.ASSERT(this.isForegrounded(), "setBlockerReason1");
	
		if( this.getCurrentState() instanceof State_EditingCodeBlocker )
		{
			if( reason != null )
			{
				((State_EditingCodeBlocker) this.getCurrentState()).setReason(reason);
			}
			else
			{
				machine_popState(this, ((State_EditingCodeBlocker) this.getCurrentState()).getReason());
			}
		}
		else
		{
			if( this.getCurrentState() == null )
			{
				machine_setState(this, State_EditingCode.class);
			}
			
			if( reason != null )
			{
				State_EditingCodeBlocker.Constructor constructor = new State_EditingCodeBlocker.Constructor(reason);
				
				machine_pushState(this, State_EditingCodeBlocker.class, constructor);
			}
		}
	}
	
	private void pushOrPopBlocker(smA_State cameraState)
	{
		if( !this.isForegrounded() )  return;
		
		boolean noCellSelected = false;
		
		if( cameraState != null )
		{
			if( cameraState instanceof State_CameraSnapping )
			{
				setBlockerReason(State_EditingCodeBlocker.Reason.SNAPPING);
			}
			else if( cameraState instanceof State_ViewingCell )
			{
				m_waitingOnHtmlForViewedCell = false;
				
				State_ViewingCell viewingState = (State_ViewingCell) cameraState;
				smBufferCell viewedCell = viewingState.getCell();
				smGridCoordinate coord = viewedCell.getCoordinate();
				smA_ClientUser user = m_appContext.userMngr.getUser();
				
				if( user.isCellOwner(coord) )
				{
					//--- DRK > This gets source which has possibly been edited but not saved.
					//---		We access this instead of the cell's direct html source because
					//---		user might have edited, then navigated away, then came back to continue editing.
					m_code = user.getCode(coord, smE_CodeType.SOURCE);
				}
				else
				{
					m_code = viewedCell.getCode(smE_CodeType.SOURCE);
				}
				
				if( m_code == null )
				{					
					if( viewedCell.getStatus(smE_CodeType.SOURCE) != smE_CodeStatus.HAS_CODE )
					{
						//--- DRK > I had this smU_Debug.ASSERT here, but NEEDS_HTML is valid if this state is backgrounded.
						//---		Technically as of this writing, the policy is to never get source html unless this
						//---		state is or becomes foregrounded, so technically now I could early out of this method for this smU_Debug.ASSERT case
						//---		I'm NOT early outing for now so that if at any point in the future I decide that source html
						//---		CAN be retrieved whilly nilly, everything will act appropriately without modification.
						//---		As it is, the stuff below shouldn't be performance intensive anyway.
						//smU_Debug.ASSERT(viewedCell.getStatus(smE_HtmlType.SOURCE) != smE_HtmlStatus.NEEDS_HTML);
						
						if( viewedCell.getStatus(smE_CodeType.SOURCE) == smE_CodeStatus.GET_ERROR )
						{
							this.setBlockerReason(State_EditingCodeBlocker.Reason.ERROR);
							
							m_waitingOnHtmlForViewedCell = false;
						}
						else
						{
							this.setBlockerReason(State_EditingCodeBlocker.Reason.LOADING);
							m_waitingOnHtmlForViewedCell = true;
						}
					}
					else
					{
						//--- DRK > Cell "has" html, but for whatever reason, it's null/blank.
						//---		Note that this is not an error case, for now at least.
						this.setBlockerReason(State_EditingCodeBlocker.Reason.NO_HTML);
						
						smU_Debug.ASSERT(false, "Code should now not be null.");
					}
				}
				else
				{
					this.setBlockerReason(null);
				}
			}
			else
			{
				noCellSelected = true;
			}
		}
		else
		{
			noCellSelected = true;
		}
		
		if( noCellSelected )
		{
			m_code = null;
			
			setBlockerReason(State_EditingCodeBlocker.Reason.NO_CELL_SELECTED);
			
			m_waitingOnHtmlForViewedCell = false;
		}
		
	}
	
	@Override
	public void onStateEvent(smStateEvent event)
	{
		if( !this.isForegrounded() )  return;
		
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_CameraSnapping )
				{
					pushOrPopBlocker(event.getState());
				}
				else if( event.getState() instanceof State_ViewingCell )
				{
					pushOrPopBlocker(event.getState());
				}
				
				break;
			}
			
			case DID_UPDATE:
			{
				if( event.getState() instanceof State_ViewingCell )
				{
					State_ViewingCell viewingState = (State_ViewingCell) event.getState();
					smBufferCell viewedCell = viewingState.getCell();
					smGridCoordinate coord = viewedCell.getCoordinate();
					
					/*if( this.getCurrentState() instanceof State_EditingCodeBlocker )
					{
						State_EditingCodeBlocker blocker = (State_EditingCodeBlocker) this.getCurrentState();
						if( blocker.getReason() == State_EditingCodeBlocker.Reason.SYNCING ||
							blocker.getReason() == State_EditingCodeBlocker.Reason.PREVIEWING )
						{
							smU_Debug.ASSERT(!m_waitingOnHtmlForViewedCell);
							
							smClientUser user = smClientUser.getInstance();
							
							smU_Debug.ASSERT(user.isCellOwner(coord));
							
							if( !user.isSyncingOrPreviewing(coord) )
							{
								if( this.isForegrounded() )
								{
									this.setBlockerReason(null);
								}
								
								return;
							}
						}
					}*/

					if( m_waitingOnHtmlForViewedCell )
					{
						if( viewedCell.getStatus(smE_CodeType.SOURCE) == smE_CodeStatus.HAS_CODE )
						{
							m_waitingOnHtmlForViewedCell = false;
							
							smCode code = viewedCell.getCode(smE_CodeType.SOURCE);
							
							if( code == null )
							{
								this.setBlockerReason(State_EditingCodeBlocker.Reason.NO_HTML);
							}
							else
							{
								m_code = code;
								this.setBlockerReason(null);
							}
						}
						else if( viewedCell.getStatus(smE_CodeType.SOURCE) == smE_CodeStatus.GET_ERROR )
						{
							this.setBlockerReason(State_EditingCodeBlocker.Reason.ERROR);
						}
					}
				}
				
				break;
			}
			
			case DID_EXIT:
			{
				if( event.getState() instanceof State_ViewingCell || event.getState() instanceof State_CameraSnapping )
				{
					pushOrPopBlocker(null);
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == Action_ViewingCell_Refresh.class )
				{
					State_ViewingCell viewingState = smA_State.getEnteredInstance(State_ViewingCell.class);
					pushOrPopBlocker(viewingState);
				}
				else if( event.getAction() == StateMachine_Base.OnUserCleared.class )
				{
					if( event.getAction() == StateMachine_Base.OnUserCleared.class  )
					{
						State_ViewingCell viewingState = smA_State.getEnteredInstance(State_ViewingCell.class);
						State_EditingCode editingState = smA_State.getEnteredInstance(State_EditingCode.class);
						
						if( viewingState != null && editingState != null )
						{
							pushOrPopBlocker(viewingState);
						}
					}
				}
				else if( event.getAction() == StateMachine_Base.OnUserPopulated.class || 
						 event.getAction() == StateMachine_Base.OnUserCleared.class  )
				{
					State_ViewingCell viewingState = smA_State.getEnteredInstance(State_ViewingCell.class);
					State_EditingCode editingState = smA_State.getEnteredInstance(State_EditingCode.class);
					
					if( viewingState != null && editingState != null )
					{
						pushOrPopBlocker(viewingState);
					}
				}
				
				break;
			}
		}
	}
}