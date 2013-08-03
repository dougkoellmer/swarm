package b33hive.client.states.camera;

import b33hive.client.input.bhBrowserHistoryManager;
import b33hive.client.managers.bhCellAddressManager;
import b33hive.client.managers.bhCellCodeManager;
import b33hive.client.managers.bhUserManager;
import b33hive.client.entities.bhBufferCell;
import b33hive.client.entities.bhA_ClientUser;
import b33hive.client.entities.bhE_CellNuke;
import b33hive.client.entities.bhE_CodeStatus;
import b33hive.client.states.camera.StateMachine_Camera.CameraManager;
import b33hive.client.states.code.StateMachine_EditingCode;
import b33hive.client.structs.bhI_LocalCodeRepository;
import b33hive.client.transaction.bhClientTransactionManager;
import b33hive.client.transaction.bhE_TransactionAction;
import b33hive.shared.app.bhS_App;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.statemachine.bhA_Action;

import b33hive.shared.statemachine.bhA_ActionArgs;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhI_StateEventListener;
import b33hive.shared.statemachine.bhA_StateConstructor;
import b33hive.shared.statemachine.bhStateEvent;
import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhCellAddressMapping;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.structs.bhPoint;


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
			bhCellAddressManager.getInstance().getCellAddress(mapping, bhE_TransactionAction.QUEUE_REQUEST);
		}
		
		codeManager.populateCell(cell, localCodeRepo, 1, false, true, bhE_CodeType.SPLASH);
		codeManager.populateCell(cell, localCodeRepo, 1, false, true, bhE_CodeType.COMPILED);
		 
		bhClientTransactionManager.getInstance().flushRequestQueue();
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
		bhA_ClientUser user = bhUserManager.getInstance().getUser();
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