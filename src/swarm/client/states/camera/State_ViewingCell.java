package swarm.client.states.camera;

import swarm.client.input.smBrowserHistoryManager;
import swarm.client.managers.smCellAddressManager;
import swarm.client.managers.smCellCodeManager;
import swarm.client.managers.smUserManager;
import swarm.client.app.sm_c;
import swarm.client.entities.smBufferCell;
import swarm.client.entities.smA_ClientUser;
import swarm.client.entities.smE_CellNuke;
import swarm.client.entities.smE_CodeStatus;
import swarm.client.states.camera.StateMachine_Camera.CameraManager;
import swarm.client.states.code.StateMachine_EditingCode;
import swarm.client.structs.smI_LocalCodeRepository;
import swarm.client.transaction.smClientTransactionManager;
import swarm.client.transaction.smE_TransactionAction;
import swarm.shared.app.smS_App;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.statemachine.smA_Action;

import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smI_StateEventListener;
import swarm.shared.statemachine.smA_StateConstructor;
import swarm.shared.statemachine.smStateEvent;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;



/**
 * ...
 * @author 
 */
public class State_ViewingCell extends smA_State implements smI_StateEventListener
{
	static class Constructor extends smA_StateConstructor
	{
		private final smBufferCell m_cell;
		
		public Constructor(smBufferCell cell)
		{
			m_cell = cell;
		}
	}
	
	public static class Refresh extends smA_CameraAction 
	{
		@Override
		public void perform(smA_ActionArgs args)
		{
			State_ViewingCell state = (State_ViewingCell) this.getState();
			
			state.refreshCell();
		}
		
		@Override
		public boolean isPerformable(smA_ActionArgs args)
		{
			State_ViewingCell state = (State_ViewingCell) this.getState();
			smBufferCell cell = state.getCell();
			
			if( cell.getStatus(smE_CodeType.COMPILED) == smE_CodeStatus.WAITING_ON_CODE )
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		
		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return State_ViewingCell.class;
		}
	}
	
	private smBufferCell m_cell = null;
	
	private boolean m_hasRequestedSourceCode = false;
	
	public State_ViewingCell()
	{
		smA_Action.register(new Refresh());
	}
	
	void refreshCell()
	{
		smBufferCell cell = this.getCell();
		smGridCoordinate coord = cell.getCoordinate();
		
		//--- DRK > Even though "everything" is nuked from cell here, a user's cell will be immediately
		//---		repopulated from the user data itself, so no server request will go out needlessly.
		smCellCodeManager codeManager = smCellCodeManager.getInstance();
		codeManager.nukeFromOrbit(coord, smE_CellNuke.EVERYTHING);
		
		smI_LocalCodeRepository localCodeRepo = ((StateMachine_Camera)this.getParent()).getCodeRepository();
		
		//--- DRK > NOTE: Directly manipulating m_hasRequestedSourceHtml is a little hacky, but whatever.
		if( smA_State.isForegrounded(StateMachine_EditingCode.class) )
		{
			codeManager.populateCell(cell, localCodeRepo, 1, false, true, smE_CodeType.SOURCE);
			
			this.m_hasRequestedSourceCode = true;
		}
		else
		{
			this.m_hasRequestedSourceCode = false;
		}
		
		if( cell.getCellAddress() == null )
		{
			//--- DRK > Try to get address ourselves...very well could turn up null.
			smCellAddressMapping mapping = new smCellAddressMapping(cell.getCoordinate());
			smCellAddressManager addyManager = sm_c.addressMngr;
			addyManager.getCellAddress(mapping, smE_TransactionAction.QUEUE_REQUEST);
		}
		
		codeManager.populateCell(cell, localCodeRepo, 1, false, true, smE_CodeType.SPLASH);
		codeManager.populateCell(cell, localCodeRepo, 1, false, true, smE_CodeType.COMPILED);
		
		smClientTransactionManager txnMngr = sm_c.txnMngr;
		txnMngr.flushRequestQueue();
	}

	public smBufferCell getCell()
	{
		return m_cell;
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
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
		smUserManager userMngr = sm_c.userMngr;
		smA_ClientUser user = userMngr.getUser();
		user.tryPopulatingCell(m_cell.getCoordinate(), smE_CodeType.COMPILED, m_cell);
		
		m_cell = null;
	}
	
	private void requestSourceHtmlForTargetCell()
	{
		//--- DRK > As an optimization, we only retrieve the source html if we're in the html state.
		if( !m_hasRequestedSourceCode )
		{
			if( smA_State.isForegrounded(StateMachine_EditingCode.class) )
			{
				smI_LocalCodeRepository localHtmlSource = ((StateMachine_Camera)this.getParent()).getCodeRepository();
				smCellCodeManager populator = smCellCodeManager.getInstance();
				populator.populateCell(m_cell, localHtmlSource, 1, false, true, smE_CodeType.SOURCE);
				
				populator.flush();
				
				m_hasRequestedSourceCode = true;
			}
		}
	}
	
	@Override
	public void onStateEvent(smStateEvent event)
	{
		switch(event.getType())
		{
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof StateMachine_EditingCode )
				{
					if( m_cell.getStatus(smE_CodeType.SOURCE) == smE_CodeStatus.NEEDS_CODE )
					{
						smU_Debug.ASSERT(!m_hasRequestedSourceCode);
						
						this.requestSourceHtmlForTargetCell();
					}
				}
				
				break;
			}
		}
	}
}