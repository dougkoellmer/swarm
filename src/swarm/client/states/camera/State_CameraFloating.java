package swarm.client.states.camera;

import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_BaseStateEvent;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;

public class State_CameraFloating extends A_State
{	
	public State_CameraFloating()
	{
	}
	
	@Override
	protected void didEnter()
	{
	}
	
	@Override
	protected void didForeground(Class<? extends A_State> revealingState, StateArgs argsFromRevealingState)
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
	protected void willBackground(Class<? extends A_State> blockingState)
	{
		
	}
	
	@Override
	protected void willExit()
	{
		
	}
}


