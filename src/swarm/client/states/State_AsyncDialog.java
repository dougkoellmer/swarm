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
			StateMachine_Base baseController = smA_State.getForegroundedInstance(StateMachine_Base.class);
			baseController.dequeueAsyncDialog();
		}
		
		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return State_AsyncDialog.class;
		}
	}
	
	public State_AsyncDialog()
	{
		smA_Action.register(new State_AsyncDialog.Ok());
	}
}
