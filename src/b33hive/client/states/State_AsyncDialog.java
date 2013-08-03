package b33hive.client.states;

import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_ActionArgs;
import b33hive.shared.statemachine.bhA_State;

public class State_AsyncDialog extends State_GenericDialog
{
	public static class Ok extends State_GenericDialog.Ok
	{
		@Override
		public void perform(bhA_ActionArgs args)
		{
			StateMachine_Base baseController = bhA_State.getForegroundedInstance(StateMachine_Base.class);
			baseController.dequeueAsyncDialog();
		}
		
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return State_AsyncDialog.class;
		}
	}
	
	public State_AsyncDialog()
	{
		bhA_Action.register(new State_AsyncDialog.Ok());
	}
}
