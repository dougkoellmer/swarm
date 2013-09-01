package swarm.client.states;

import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;

public class State_AsyncDialog extends State_GenericDialog
{
	public static class Ok extends State_GenericDialog.Ok
	{
		@Override
		public void perform(smA_ActionArgs args)
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
