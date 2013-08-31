package swarm.client.states.camera;

import swarm.client.entities.smBufferCell;
import swarm.client.entities.smE_CodeStatus;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;

public class Action_ViewingCell_Refresh extends smA_CameraAction 
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