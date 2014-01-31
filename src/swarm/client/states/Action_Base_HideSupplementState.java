package swarm.client.states;

import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_ActionArgs;
import swarm.shared.statemachine.A_State;

public class Action_Base_HideSupplementState extends A_Action
{
	@Override
	public void perform(A_ActionArgs args)
	{
		container_backgroundState(this.getState(), StateMachine_Tabs.class);
	}
	
	@Override
	public boolean isPerformable(A_ActionArgs args)
	{
		return getContext().isForegrounded(StateMachine_Tabs.class);
	}
}