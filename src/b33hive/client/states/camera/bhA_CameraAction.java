package b33hive.client.states.camera;

import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_State;

public abstract class bhA_CameraAction extends bhA_Action
{
	@Override
	public void prePerform()
	{
		StateMachine_Camera machine = bhA_State.getEnteredInstance(StateMachine_Camera.class);
		
		machine.tryPoppingGettingAddressState();
	}
}
