package swarm.client.states;

import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_ActionArgs;
import swarm.shared.statemachine.A_State;

public class State_AsyncDialog extends State_GenericDialog
{
	public static class Ok extends State_GenericDialog.Ok
	{
		@Override
		public void perform(A_ActionArgs args)
		{
			StateMachine_Base baseController = getContext().getForegroundedState(StateMachine_Base.class);
			baseController.dequeueAsyncDialog();
		}
	}
	
	public State_AsyncDialog()
	{
		registerAction(new State_AsyncDialog.Ok());
	}
}
