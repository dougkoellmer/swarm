package swarm.client.states.camera;

import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_State;

abstract class smA_CameraAction extends A_Action
{
	@Override
	public StateArgs prePerform(StateArgs args)
	{
		StateMachine_Camera machine = getContext().getEntered(StateMachine_Camera.class);
		
		machine.tryPoppingGettingAddressState();
		
		return args;
	}
}
