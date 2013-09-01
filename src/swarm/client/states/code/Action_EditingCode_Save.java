package swarm.client.states.code;

import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;

public class Action_EditingCode_Save extends smA_Action
{
	@Override
	public void perform(smA_ActionArgs args)
	{
		State_EditingCode state = this.getState();
		
		state.performCommitOrPreview(this);
	}
	
	@Override
	public boolean isPerformable(smA_ActionArgs args)
	{
		State_EditingCode state = this.getState();
		
		return state.isCommitOrPreviewPerformable(false);
	}
}