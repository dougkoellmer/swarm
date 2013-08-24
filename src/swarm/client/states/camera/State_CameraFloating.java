package swarm.client.states.camera;

import swarm.client.states.camera.StateMachine_Camera.CameraManager;
import swarm.shared.statemachine.bhA_Action;
import swarm.shared.statemachine.bhA_State;
import swarm.shared.statemachine.bhA_StateConstructor;
import swarm.shared.statemachine.bhStateEvent;
import swarm.shared.structs.bhGridCoordinate;
import swarm.shared.structs.bhPoint;

public class State_CameraFloating extends bhA_State
{	
	public State_CameraFloating()
	{
	}
	
	@Override
	protected void didEnter(bhA_StateConstructor constructor)
	{
	}
	
	@Override
	protected void didForeground(Class<? extends bhA_State> revealingState, Object[] argsFromRevealingState)
	{
		if ( revealingState == null )
		{
		//	bhCamera camera = (getParent() as StateMachine_CameraController).getCamera();
			//m_targetPosition.copy(camera.getPosition());
		}
	}
	
	@Override
	protected void update(double timeStep)
	{
		//bhCamera camera = (getParent() as StateMachine_CameraController).getCamera();
	}
	
	@Override
	protected void willBackground(Class<? extends bhA_State> blockingState)
	{
		
	}
	
	@Override
	protected void willExit()
	{
		
	}
}


