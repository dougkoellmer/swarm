package swarm.client.states.camera;

import swarm.client.managers.CellAddressManager;
import swarm.client.managers.CellCodeManager;
import swarm.client.managers.UserManager;
import swarm.client.app.AppContext;
import swarm.client.entities.BufferCell;
import swarm.client.entities.A_ClientUser;
import swarm.client.entities.Camera;
import swarm.client.entities.E_CellNuke;
import swarm.client.entities.E_CodeStatus;
import swarm.client.states.code.StateMachine_EditingCode;
import swarm.client.structs.I_LocalCodeRepository;
import swarm.client.transaction.ClientTransactionManager;
import swarm.client.transaction.E_TransactionAction;
import swarm.shared.app.S_CommonApp;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.E_CodeType;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.I_StateEventListener;
import swarm.shared.statemachine.A_StateConstructor;
import swarm.shared.statemachine.StateEvent;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;



/**
 * ...
 * @author 
 */
public class State_ViewingCell extends A_State implements I_StateEventListener
{
	static class Constructor extends A_StateConstructor
	{
		private final BufferCell m_cell;
		
		public Constructor(BufferCell cell)
		{
			m_cell = cell;
		}
	}
	
	private BufferCell m_cell = null;
	
	private boolean m_hasRequestedSourceCode = false;
	private final AppContext m_appContext;
	private final double m_cellHudHeight;
	
	public State_ViewingCell(AppContext appContext, double cellHudHeight)
	{
		m_appContext = appContext;
		m_cellHudHeight = cellHudHeight;
		
		registerAction(new Action_ViewingCell_Refresh());
	}
	
	void refreshCell()
	{
		BufferCell cell = this.getCell();
		GridCoordinate coord = cell.getCoordinate();
		
		//--- DRK > Even though "everything" is nuked from cell here, a user's cell will be immediately
		//---		repopulated from the user data itself, so no server request will go out needlessly.
		CellCodeManager codeManager = m_appContext.codeMngr;
		codeManager.nukeFromOrbit(coord, E_CellNuke.EVERYTHING);
		
		I_LocalCodeRepository localCodeRepo = ((StateMachine_Camera)this.getParent()).getCodeRepository();
		
		//--- DRK > NOTE: Directly manipulating m_hasRequestedSourceHtml is a little hacky, but whatever.
		if( getContext().isForegrounded(StateMachine_EditingCode.class) )
		{
			codeManager.populateCell(cell, localCodeRepo, 1, false, true, E_CodeType.SOURCE);
			
			this.m_hasRequestedSourceCode = true;
		}
		else
		{
			this.m_hasRequestedSourceCode = false;
		}
		
		if( cell.getAddress() == null )
		{
			//--- DRK > Try to get address ourselves...very well could turn up null.
			CellAddressMapping mapping = new CellAddressMapping(cell.getCoordinate());
			CellAddressManager addyManager = m_appContext.addressMngr;
			addyManager.getCellAddress(mapping, E_TransactionAction.QUEUE_REQUEST);
		}
		
		codeManager.populateCell(cell, localCodeRepo, 1, false, true, E_CodeType.SPLASH);
		codeManager.populateCell(cell, localCodeRepo, 1, false, true, E_CodeType.COMPILED);
		
		ClientTransactionManager txnMngr = m_appContext.txnMngr;
		txnMngr.flushAsyncRequestQueue();
	}

	public BufferCell getCell()
	{
		return m_cell;
	}
	
	@Override
	protected void didEnter(A_StateConstructor constructor)
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
		UserManager userMngr = m_appContext.userMngr;
		A_ClientUser user = userMngr.getUser();
		user.tryPopulatingCell(m_cell.getCoordinate(), E_CodeType.COMPILED, m_cell);
		
		m_cell = null;
	}
	
	private void requestSourceHtmlForTargetCell()
	{
		//--- DRK > As an optimization, we only retrieve the source html if we're in the html state.
		if( !m_hasRequestedSourceCode )
		{
			if( getContext().isForegrounded(StateMachine_EditingCode.class) )
			{
				I_LocalCodeRepository localHtmlSource = ((StateMachine_Camera)this.getParent()).getCodeRepository();
				CellCodeManager codeMngr = m_appContext.codeMngr;
				codeMngr.populateCell(m_cell, localHtmlSource, 1, false, true, E_CodeType.SOURCE);
				
				codeMngr.flush();
				
				m_hasRequestedSourceCode = true;
			}
		}
	}
	
	@Override
	public void onStateEvent(StateEvent event)
	{
		switch(event.getType())
		{
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof StateMachine_EditingCode )
				{
					if( m_cell.getStatus(E_CodeType.SOURCE) == E_CodeStatus.NEEDS_CODE )
					{
						U_Debug.ASSERT(!m_hasRequestedSourceCode);
						
						this.requestSourceHtmlForTargetCell();
					}
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == Action_Camera_SetViewSize.class )
				{
					
				}
				
				break;
			}
		}
	}
}