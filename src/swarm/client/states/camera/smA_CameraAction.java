package swarm.client.states.camera;

import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_ActionArgs;
import swarm.shared.statemachine.A_State;

abstract class smA_CameraAction extends A_Action
{
	@Override
	public void prePerform(A_ActionArgs args)
	{
		StateMachine_Camera machine = getContext().getEnteredState(StateMachine_Camera.class);
		
		machine.tryPoppingGettingAddressState();
	}
}
