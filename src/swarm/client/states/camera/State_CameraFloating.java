package swarm.client.states.camera;

import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateConstructor;
import swarm.shared.statemachine.smStateEvent;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;

public class State_CameraFloating extends smA_State
{	
	public State_CameraFloating()
	{
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
	}
	
	@Override
	protected void didForeground(Class<? extends smA_State> revealingState, Object[] argsFromRevealingState)
	{
		if ( revealingState == null )
		{
		//	smCamera camera = (getParent() as StateMachine_CameraController).getCamera();
			//m_targetPosition.copy(camera.getPosition());
		}
	}
	
	@Override
	protected void update(double timeStep)
	{
		//smCamera camera = (getParent() as StateMachine_CameraController).getCamera();
	}
	
	@Override
	protected void willBackground(Class<? extends smA_State> blockingState)
	{
		
	}
	
	@Override
	protected void willExit()
	{
		
	}
}

