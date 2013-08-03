package b33hive.client.states.camera;

import b33hive.client.states.camera.StateMachine_Camera.CameraManager;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhA_StateConstructor;
import b33hive.shared.statemachine.bhStateEvent;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.structs.bhPoint;

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


