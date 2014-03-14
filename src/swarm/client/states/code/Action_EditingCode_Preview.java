package swarm.client.states.code;

import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_State;

public class Action_EditingCode_Preview extends A_Action 
{
	@Override
	public void perform(StateArgs args)
	{
		State_EditingCode state = this.getState();
		
		state.performCommitOrPreview(this);
	}
	
	@Override
	public boolean isPerformable(StateArgs args)
	{
		State_EditingCode state = this.getState();
		
		return state.isCommitOrPreviewPerformable(true);
	}
}