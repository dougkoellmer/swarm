package b33hive.client.states.code;

import b33hive.client.managers.bhCellCodeManager;
import b33hive.client.managers.bhUserManager;
import b33hive.client.app.bh_c;
import b33hive.client.entities.bhBufferCell;
import b33hive.client.entities.bhA_ClientUser;
import b33hive.client.entities.bhE_CodeStatus;
import b33hive.client.states.StateMachine_Base;
import b33hive.client.states.camera.StateMachine_Camera;
import b33hive.client.states.camera.State_CameraSnapping;
import b33hive.client.states.camera.State_ViewingCell;
import b33hive.client.states.camera.State_ViewingCell.Refresh;
import b33hive.client.states.code.State_EditingCodeBlocker.Reason;
import b33hive.client.transaction.bhClientTransactionManager;
import b33hive.client.ui.tabs.code.bhCodeMirrorWrapper;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.statemachine.bhA_Action;

import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhA_StateMachine;
import b33hive.shared.statemachine.bhI_StateEventListener;
import b33hive.shared.statemachine.bhA_StateConstructor;
import b33hive.shared.statemachine.bhStateEvent;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhGridCoordinate;


/**
 * ...
 * @author 
 */
public class StateMachine_EditingCode extends bhA_StateMachine implements bhI_StateEventListener
{
	private boolean m_waitingOnHtmlForViewedCell = false;
	
	bhCode m_code = null;
	
	@Override
	protected void didEnter(bhA_StateConstructor constructor)
	{
		m_waitingOnHtmlForViewedCell = false;
		m_code = null;
	}
	
	bhCode getCode()
	{
		return m_code;
	}
	
	@Override
	protected void didForeground(Class<? extends bhA_State> revealingState, Object[] argsFromRevealingState)
	{
		//--- DRK > Camera controller can be null during start up...should be the only time.
		bhA_StateMachine cameraController = bhA_State.getEnteredInstance(StateMachine_Camera.class);
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
	protected void willBackground(Class<? extends bhA_State> blockingState)
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
		bhU_Debug.ASSERT(this.isForegrounded(), "setBlockerReason1");
	
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
	
	private void pushOrPopBlocker(bhA_State cameraState)
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
				bhBufferCell viewedCell = viewingState.getCell();
				bhGridCoordinate coord = viewedCell.getCoordinate();
				bhA_ClientUser user = bh_c.userMngr.getUser();
				
				if( user.isCellOwner(coord) )
				{
					//--- DRK > This gets source which has possibly been edited but not saved.
					//---		We access this instead of the cell's direct html source because
					//---		user might have edited, then navigated away, then came back to continue editing.
					m_code = user.getCode(coord, bhE_CodeType.SOURCE);
				}
				else
				{
					m_code = viewedCell.getCode(bhE_CodeType.SOURCE);
				}
				
				if( m_code == null )
				{
					bhClientTransactionManager manager = bh_c.txnMngr;
					bhCellCodeManager populator = bhCellCodeManager.getInstance();
					
					if( viewedCell.getStatus(bhE_CodeType.SOURCE) != bhE_CodeStatus.HAS_CODE )
					{
						//--- DRK > I had this bhU_Debug.ASSERT here, but NEEDS_HTML is valid if this state is backgrounded.
						//---		Technically as of this writing, the policy is to never get source html unless this
						//---		state is or becomes foregrounded, so technically now I could early out of this method for this bhU_Debug.ASSERT case
						//---		I'm NOT early outing for now so that if at any point in the future I decide that source html
						//---		CAN be retrieved whilly nilly, everything will act appropriately without modification.
						//---		As it is, the stuff below shouldn't be performance intensive anyway.
						//bhU_Debug.ASSERT(viewedCell.getStatus(bhE_HtmlType.SOURCE) != bhE_HtmlStatus.NEEDS_HTML);
						
						if( viewedCell.getStatus(bhE_CodeType.SOURCE) == bhE_CodeStatus.GET_ERROR )
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
						
						bhU_Debug.ASSERT(false, "Code should now not be null.");
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
	public void onStateEvent(bhStateEvent event)
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
					bhBufferCell viewedCell = viewingState.getCell();
					bhGridCoordinate coord = viewedCell.getCoordinate();
					
					/*if( this.getCurrentState() instanceof State_EditingCodeBlocker )
					{
						State_EditingCodeBlocker blocker = (State_EditingCodeBlocker) this.getCurrentState();
						if( blocker.getReason() == State_EditingCodeBlocker.Reason.SYNCING ||
							blocker.getReason() == State_EditingCodeBlocker.Reason.PREVIEWING )
						{
							bhU_Debug.ASSERT(!m_waitingOnHtmlForViewedCell);
							
							bhClientUser user = bhClientUser.getInstance();
							
							bhU_Debug.ASSERT(user.isCellOwner(coord));
							
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
						if( viewedCell.getStatus(bhE_CodeType.SOURCE) == bhE_CodeStatus.HAS_CODE )
						{
							m_waitingOnHtmlForViewedCell = false;
							
							bhCode code = viewedCell.getCode(bhE_CodeType.SOURCE);
							
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
						else if( viewedCell.getStatus(bhE_CodeType.SOURCE) == bhE_CodeStatus.GET_ERROR )
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
				if( event.getAction() == State_ViewingCell.Refresh.class )
				{
					State_ViewingCell viewingState = bhA_State.getEnteredInstance(State_ViewingCell.class);
					pushOrPopBlocker(viewingState);
				}
				else if( event.getAction() == StateMachine_Base.OnUserCleared.class )
				{
					if( event.getAction() == StateMachine_Base.OnUserCleared.class  )
					{
						State_ViewingCell viewingState = bhA_State.getEnteredInstance(State_ViewingCell.class);
						State_EditingCode editingState = bhA_State.getEnteredInstance(State_EditingCode.class);
						
						if( viewingState != null && editingState != null )
						{
							pushOrPopBlocker(viewingState);
						}
					}
				}
				else if( event.getAction() == StateMachine_Base.OnUserPopulated.class || 
						 event.getAction() == StateMachine_Base.OnUserCleared.class  )
				{
					State_ViewingCell viewingState = bhA_State.getEnteredInstance(State_ViewingCell.class);
					State_EditingCode editingState = bhA_State.getEnteredInstance(State_EditingCode.class);
					
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