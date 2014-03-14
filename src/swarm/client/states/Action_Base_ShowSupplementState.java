package swarm.client.states;

import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_State;

public class Action_Base_ShowSupplementState extends A_Action 
{
	@Override
	public void perform(StateArgs args)
	{
		foregroundState(this.getState(), StateMachine_Tabs.class);
	}
	
	@Override
	public boolean isPerformable(StateArgs args)
	{
		return !getContext().isForegrounded(StateMachine_Tabs.class);
	}
}