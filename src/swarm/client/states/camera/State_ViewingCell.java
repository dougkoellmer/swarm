package swarm.client.states.camera;

import swarm.client.input.bhBrowserHistoryManager;
import swarm.client.managers.bhCellAddressManager;
import swarm.client.managers.bhCellCodeManager;
import swarm.client.managers.bhUserManager;
import swarm.client.app.sm_c;
import swarm.client.entities.bhBufferCell;
import swarm.client.entities.bhA_ClientUser;
import swarm.client.entities.bhE_CellNuke;
import swarm.client.entities.bhE_CodeStatus;
import swarm.client.states.camera.StateMachine_Camera.CameraManager;
import swarm.client.states.code.StateMachine_EditingCode;
import swarm.client.structs.bhI_LocalCodeRepository;
import swarm.client.transaction.bhClientTransactionManager;
import swarm.client.transaction.bhE_TransactionAction;
import swarm.shared.app.bhS_App;
import swarm.shared.debugging.bhU_Debug;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.statemachine.bhA_Action;

import swarm.shared.statemachine.bhA_ActionArgs;
import swarm.shared.statemachine.bhA_State;
import swarm.shared.statemachine.bhI_StateEventListener;
import swarm.shared.statemachine.bhA_StateConstructor;
import swarm.shared.statemachine.bhStateEvent;
import swarm.shared.structs.bhCellAddress;
import swarm.shared.structs.bhCellAddressMapping;
import swarm.shared.structs.bhGridCoordinate;
import swarm.shared.structs.bhPoint;



/**
 * ...
 * @author 
 */
public class State_ViewingCell extends bhA_State implements bhI_StateEventListener
{
	static class Constructor extends bhA_StateConstructor
	{
		private final bhBufferCell m_cell;
		
		public Constructor(bhBufferCell cell)
		{
			m_cell = cell;
		}
	}
	
	public static class Refresh extends bhA_CameraAction 
	{
		@Override
		public void perform(bhA_ActionArgs args)
		{
			State_ViewingCell state = (State_ViewingCell) this.getState();
			
			state.refreshCell();
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
		{
			State_ViewingCell state = (State_ViewingCell) this.getState();
			bhBufferCell cell = state.getCell();
			
			if( cell.getStatus(bhE_CodeType.COMPILED) == bhE_CodeStatus.WAITING_ON_CODE )
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return State_ViewingCell.class;
		}
	}
	
	private bhBufferCell m_cell = null;
	
	private boolean m_hasRequestedSourceCode = false;
	
	public State_ViewingCell()
	{
		bhA_Action.register(new Refresh());
	}
	
	void refreshCell()
	{
		bhBufferCell cell = this.getCell();
		bhGridCoordinate coord = cell.getCoordinate();
		
		//--- DRK > Even though "everything" is nuked from cell here, a user's cell will be immediately
		//---		repopulated from the user data itself, so no server request will go out needlessly.
		bhCellCodeManager codeManager = bhCellCodeManager.getInstance();
		codeManager.nukeFromOrbit(coord, bhE_CellNuke.EVERYTHING);
		
		bhI_LocalCodeRepository localCodeRepo = ((StateMachine_Camera)this.getParent()).getCodeRepository();
		
		//--- DRK > NOTE: Directly manipulating m_hasRequestedSourceHtml is a little hacky, but whatever.
		if( bhA_State.isForegrounded(StateMachine_EditingCode.class) )
		{
			codeManager.populateCell(cell, localCodeRepo, 1, false, true, bhE_CodeType.SOURCE);
			
			this.m_hasRequestedSourceCode = true;
		}
		else
		{
			this.m_hasRequestedSourceCode = false;
		}
		
		if( cell.getCellAddress() == null )
		{
			//--- DRK > Try to get address ourselves...very well could turn up null.
			bhCellAddressMapping mapping = new bhCellAddressMapping(cell.getCoordinate());
			bhCellAddressManager addyManager = sm_c.addressMngr;
			addyManager.getCellAddress(mapping, bhE_TransactionAction.QUEUE_REQUEST);
		}
		
		codeManager.populateCell(cell, localCodeRepo, 1, false, true, bhE_CodeType.SPLASH);
		codeManager.populateCell(cell, localCodeRepo, 1, false, true, bhE_CodeType.COMPILED);
		
		bhClientTransactionManager txnMngr = sm_c.txnMngr;
		txnMngr.flushRequestQueue();
	}

	public bhBufferCell getCell()
	{
		return m_cell;
	}
	
	@Override
	protected void didEnter(bhA_StateConstructor constructor)
	{
		Constructor thisConstructor = (Constructor)constructor;
		
		m_cell = thisConstructor.m_cell;
		
		m_cell.onFocusGained();
		
		m_hasRequestedSourceCode = false;
	}
	
	@Override
	protected void willExit()
	{
		m_cell.onFocusLost();
		
		//--- DRK > This ensures that any "preview" operations performed get cleared out.
		//---		My programmer senses are tingling on this one, telling me it might be a
		//---		hacky solution, at least as far as readability.
		bhUserManager userMngr = sm_c.userMngr;
		bhA_ClientUser user = userMngr.getUser();
		user.tryPopulatingCell(m_cell.getCoordinate(), bhE_CodeType.COMPILED, m_cell);
		
		m_cell = null;
	}
	
	private void requestSourceHtmlForTargetCell()
	{
		//--- DRK > As an optimization, we only retrieve the source html if we're in the html state.
		if( !m_hasRequestedSourceCode )
		{
			if( bhA_State.isForegrounded(StateMachine_EditingCode.class) )
			{
				bhI_LocalCodeRepository localHtmlSource = ((StateMachine_Camera)this.getParent()).getCodeRepository();
				bhCellCodeManager populator = bhCellCodeManager.getInstance();
				populator.populateCell(m_cell, localHtmlSource, 1, false, true, bhE_CodeType.SOURCE);
				
				populator.flush();
				
				m_hasRequestedSourceCode = true;
			}
		}
	}
	
	@Override
	public void onStateEvent(bhStateEvent event)
	{
		switch(event.getType())
		{
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof StateMachine_EditingCode )
				{
					if( m_cell.getStatus(bhE_CodeType.SOURCE) == bhE_CodeStatus.NEEDS_CODE )
					{
						bhU_Debug.ASSERT(!m_hasRequestedSourceCode);
						
						this.requestSourceHtmlForTargetCell();
					}
				}
				
				break;
			}
		}
	}
}