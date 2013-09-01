package swarm.client.states.camera;

import swarm.client.app.smAppContext;
import swarm.client.managers.smCellAddressManager;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smE_CellAddressParseError;

public class Action_Camera_SnapToAddress extends smA_Action
{
	public static class Args extends smA_ActionArgs
	{
		private smCellAddress m_address;
		private boolean m_onlyCausedRefresh = false;
		
		public Args()
		{
			m_address = null;
		}
		
		public Args(smCellAddress address)
		{
			m_address = address;
		}
		
		public void init(smCellAddress address)
		{
			m_address = address;
		}
		
		public boolean onlyCausedRefresh()
		{
			return m_onlyCausedRefresh;
		}
	}
	
	private final smCellAddressManager m_addressMngr;
	
	Action_Camera_SnapToAddress(smCellAddressManager addressMngr)
	{
		m_addressMngr = addressMngr;
	}
	
	@Override
	public void perform(smA_ActionArgs args)
	{
		smCellAddress address = ((Args) args).m_address;
		StateMachine_Camera machine = this.getState();
		smA_State currentState = machine.getCurrentState();
		
		if( currentState instanceof State_ViewingCell )
		{
			smCellAddress viewingAddress = ((State_ViewingCell)currentState).getCell().getCellAddress();
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
	public boolean isPerformable(smA_ActionArgs args)
	{
		smCellAddress address = ((Args) args).m_address;
		
		if( address.getParseError() != smE_CellAddressParseError.NO_ERROR )
		{
			return false;
		}
		
		if( m_addressMngr.isWaitingOnResponse(address) )
		{
			return false;
		}
		
		StateMachine_Camera machine = this.getState();
		smA_State currentState = machine.getCurrentState();
		
		if( currentState instanceof State_CameraSnapping )
		{
			smCellAddress snapTargetAddress = ((State_CameraSnapping)currentState).getTargetAddress();
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