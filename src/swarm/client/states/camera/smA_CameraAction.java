package swarm.client.states.camera;

import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;

abstract class smA_CameraAction extends smA_Action
{
	@Override
	public void prePerform(smA_ActionArgs args)
	{
		StateMachine_Camera machine = getContext().getEnteredState(StateMachine_Camera.class);
		
		machine.tryPoppingGettingAddressState();
	}
}
