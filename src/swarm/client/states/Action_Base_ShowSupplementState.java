package swarm.client.states;

import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;

public class Action_Base_ShowSupplementState extends smA_Action 
{
	@Override
	public void perform(smA_ActionArgs args)
	{
		container_foregroundState(this.getState(), StateMachine_Tabs.class);
	}
	
	@Override
	public boolean isPerformable(smA_ActionArgs args)
	{
		return !getContext().isForegrounded(StateMachine_Tabs.class);
	}
	
	@Override
	public Class<? extends smA_State> getStateAssociation()
	{
		return StateContainer_Base.class;
	}
}