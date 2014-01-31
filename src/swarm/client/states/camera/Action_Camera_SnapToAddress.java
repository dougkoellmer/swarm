package swarm.client.states.camera;

import swarm.client.app.AppContext;
import swarm.client.managers.CellAddressManager;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_ActionArgs;
import swarm.shared.statemachine.A_State;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.E_CellAddressParseError;

public class Action_Camera_SnapToAddress extends A_Action
{
	public static class Args extends A_ActionArgs
	{
		private CellAddress m_address;
		private boolean m_onlyCausedRefresh = false;
		
		public Args()
		{
			m_address = null;
		}
		
		public Args(CellAddress address)
		{
			m_address = address;
		}
		
		public void init(CellAddress address)
		{
			m_address = address;
		}
		
		public boolean onlyCausedRefresh()
		{
			return m_onlyCausedRefresh;
		}
	}
	
	private final CellAddressManager m_addressMngr;
	
	Action_Camera_SnapToAddress(CellAddressManager addressMngr)
	{
		m_addressMngr = addressMngr;
	}
	
	@Override
	public void perform(A_ActionArgs args)
	{
		CellAddress address = ((Args) args).m_address;
		StateMachine_Camera machine = this.getState();
		A_State currentState = machine.getCurrentState();
		
		if( currentState instanceof State_ViewingCell )
		{
			CellAddress viewingAddress = ((State_ViewingCell)currentState).getCell().getCellAddress();
			if( viewingAddress != null )
			{
				if( viewingAddress.isEqualTo(address) )
				{
					((State_ViewingCell)currentState).refreshCell();
					((Args) args).m_onlyCausedRefresh = true;
					
					return;
				}
			}
		}
		
		((Args) args).m_onlyCausedRefresh = false;
		((StateMachine_Camera)this.getState()).snapToCellAddress(address);
	}
	
	@Override
	public boolean isPerformable(A_ActionArgs args)
	{
		CellAddress address = ((Args) args).m_address;
		
		if( address.getParseError() != E_CellAddressParseError.NO_ERROR )
		{
			return false;
		}
		
		if( m_addressMngr.isWaitingOnResponse(address) )
		{
			return false;
		}
		
		StateMachine_Camera machine = this.getState();
		A_State currentState = machine.getCurrentState();
		
		if( currentState instanceof State_CameraSnapping )
		{
			CellAddress snapTargetAddress = ((State_CameraSnapping)currentState).getTargetAddress();
			if( snapTargetAddress == null )
			{
				return true;
			}
			else
			{
				return !snapTargetAddress.isEqualTo(address);
			}
		}
		else
		{
			return true;
		}
	}
}