package swarm.client.states.camera;

import swarm.shared.statemachine.bhA_Action;
import swarm.shared.statemachine.bhA_State;

public abstract class bhA_CameraAction extends bhA_Action
{
	@Override
	public void prePerform()
	{
		StateMachine_Camera machine = bhA_State.getEnteredInstance(StateMachine_Camera.class);
		
		machine.tryPoppingGettingAddressState();
	}
}
