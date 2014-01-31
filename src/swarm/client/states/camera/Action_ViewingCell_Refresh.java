package swarm.client.states.camera;

import swarm.client.entities.BufferCell;
import swarm.client.entities.E_CodeStatus;
import swarm.shared.entities.E_CodeType;
import swarm.shared.statemachine.A_ActionArgs;
import swarm.shared.statemachine.A_State;

public class Action_ViewingCell_Refresh extends smA_CameraAction 
{
	@Override
	public void perform(A_ActionArgs args)
	{
		State_ViewingCell state = (State_ViewingCell) this.getState();
		
		state.refreshCell();
	}
	
	@Override
	public boolean isPerformable(A_ActionArgs args)
	{
		State_ViewingCell state = (State_ViewingCell) this.getState();
		BufferCell cell = state.getCell();
		
		if( cell.getStatus(E_CodeType.COMPILED) == E_CodeStatus.WAITING_ON_CODE )
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}