package swarm.client.states.code;

import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;

public class Action_EditingCode_Preview extends smA_Action 
{
	@Override
	public void perform(smA_ActionArgs args)
	{
		State_EditingCode.performCommitOrPreview(this);
	}
	
	@Override
	public boolean isPerformable(smA_ActionArgs args)
	{
		return State_EditingCode.isCommitOrPreviewPerformable(true);
	}
	
	@Override
	public Class<? extends smA_State> getStateAssociation()
	{
		return State_EditingCode.class;
	}
}