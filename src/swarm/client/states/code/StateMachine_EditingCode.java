package swarm.client.states.code;

import swarm.client.managers.CellCodeManager;
import swarm.client.managers.UserManager;
import swarm.client.app.AppContext;
import swarm.client.entities.BufferCell;
import swarm.client.entities.A_ClientUser;
import swarm.client.entities.E_CodeStatus;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.camera.Action_ViewingCell_Refresh;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.states.code.State_EditingCodeBlocker.Reason;
import swarm.client.transaction.ClientTransactionManager;
import swarm.client.view.tabs.code.CodeMirrorWrapper;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.E_CodeType;
import swarm.shared.statemachine.A_Action;

import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.A_StateMachine;
import swarm.shared.statemachine.I_StateEventListener;
import swarm.shared.statemachine.A_StateConstructor;
import swarm.shared.statemachine.StateEvent;
import swarm.shared.structs.Code;
import swarm.shared.structs.GridCoordinate;


/**
 * ...
 * @author 
 */
public class StateMachine_EditingCode extends A_StateMachine implements I_StateEventListener
{
	private boolean m_waitingOnHtmlForViewedCell = false;
	
	Code m_code = null;
	private final AppContext m_appContext;
	
	public StateMachine_EditingCode(AppContext appContext)
	{
		m_appContext = appContext;
	}
	
	@Override
	protected void didEnter(A_StateConstructor constructor)
	{
		m_waitingOnHtmlForViewedCell = false;
		m_code = null;
	}
	
	Code getCode()
	{
		return m_code;
	}
	
	@Override
	protected void didForeground(Class<? extends A_State> revealingState, Object[] argsFromRevealingState)
	{
		//--- DRK > Camera controller can be null during start up...should be the only time.
		A_StateMachine cameraController = getContext().getEnteredState(StateMachine_Camera.class);
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
	protected void willBackground(Class<? extends A_State> blockingState)
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
		U_Debug.ASSERT(this.isForegrounded(), "setBlockerReason1");
	
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
	
	private void pushOrPopBlocker(A_State cameraState)
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
				BufferCell viewedCell = viewingState.getCell();
				GridCoordinate coord = viewedCell.getCoordinate();
				A_ClientUser user = m_appContext.userMngr.getUser();
				
				if( user.isCellOwner(coord) )
				{
					//--- DRK > This gets source which has possibly been edited but not saved.
					//---		We access this instead of the cell's direct html source because
					//---		user might have edited, then navigated away, then came back to continue editing.
					m_code = user.getCode(coord, E_CodeType.SOURCE);
				}
				else
				{
					m_code = viewedCell.getCode(E_CodeType.SOURCE);
				}
				
				if( m_code == null )
				{					
					if( viewedCell.getStatus(E_CodeType.SOURCE) != E_CodeStatus.HAS_CODE )
					{
						//--- DRK > I had this smU_Debug.ASSERT here, but NEEDS_HTML is valid if this state is backgrounded.
						//---		Technically as of this writing, the policy is to never get source html unless this
						//---		state is or becomes foregrounded, so technically now I could early out of this method for this smU_Debug.ASSERT case
						//---		I'm NOT early outing for now so that if at any point in the future I decide that source html
						//---		CAN be retrieved whilly nilly, everything will act appropriately without modification.
						//---		As it is, the stuff below shouldn't be performance intensive anyway.
						//smU_Debug.ASSERT(viewedCell.getStatus(smE_HtmlType.SOURCE) != smE_HtmlStatus.NEEDS_HTML);
						
						if( viewedCell.getStatus(E_CodeType.SOURCE) == E_CodeStatus.GET_ERROR )
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
						
						U_Debug.ASSERT(false, "Code should now not be null.");
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
	public void onStateEvent(StateEvent event)
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
					BufferCell viewedCell = viewingState.getCell();
					GridCoordinate coord = viewedCell.getCoordinate();
					
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
						if( viewedCell.getStatus(E_CodeType.SOURCE) == E_CodeStatus.HAS_CODE )
						{
							m_waitingOnHtmlForViewedCell = false;
							
							Code code = viewedCell.getCode(E_CodeType.SOURCE);
							
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
						else if( viewedCell.getStatus(E_CodeType.SOURCE) == E_CodeStatus.GET_ERROR )
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
					State_ViewingCell viewingState = getContext().getEnteredState(State_ViewingCell.class);
					pushOrPopBlocker(viewingState);
				}
				else if( event.getAction() == StateMachine_Base.OnUserCleared.class )
				{
					if( event.getAction() == StateMachine_Base.OnUserCleared.class  )
					{
						State_ViewingCell viewingState = getContext().getEnteredState(State_ViewingCell.class);
						State_EditingCode editingState = getContext().getEnteredState(State_EditingCode.class);
						
						if( viewingState != null && editingState != null )
						{
							pushOrPopBlocker(viewingState);
						}
					}
				}
				else if( event.getAction() == StateMachine_Base.OnUserPopulated.class || 
						 event.getAction() == StateMachine_Base.OnUserCleared.class  )
				{
					State_ViewingCell viewingState = getContext().getEnteredState(State_ViewingCell.class);
					State_EditingCode editingState = getContext().getEnteredState(State_EditingCode.class);
					
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