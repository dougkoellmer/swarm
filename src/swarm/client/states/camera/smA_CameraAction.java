package swarm.client.states.camera;

import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_State;

abstract class smA_CameraAction extends smA_Action
{
	@Override
	public void prePerform()
	{
		StateMachine_Camera machine = smA_State.getEnteredInstance(StateMachine_Camera.class);
		
		machine.tryPoppingGettingAddressState();
	}
}
