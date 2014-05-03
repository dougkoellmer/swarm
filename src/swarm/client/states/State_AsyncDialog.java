package swarm.client.states;

import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_State;

public class State_AsyncDialog extends State_GenericDialog
{
	public static class Ok extends State_GenericDialog.Ok
	{
		@Override
		public void perform(StateArgs args)
		{
			StateMachine_Base baseController = getContext().getForegrounded(StateMachine_Base.class);
			baseController.dequeueAsyncDialog();
		}
	}
	
	public State_AsyncDialog()
	{
		register(new State_AsyncDialog.Ok());
	}
}
