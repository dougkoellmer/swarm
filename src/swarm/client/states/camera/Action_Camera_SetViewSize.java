package swarm.client.states.camera;

import swarm.client.entities.smCamera;
import swarm.client.managers.smCameraManager;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smPoint;

public class Action_Camera_SetViewSize extends smA_Action
{
	public static class Args extends smA_ActionArgs
	{
		private final double[] m_dimensions = new double[2];
		private boolean m_updateBuffer = true;
		
		public void init(double width, double height, boolean updateBuffer)
		{
			m_dimensions[0] = width;
			m_dimensions[1] = height;
			m_updateBuffer = updateBuffer;
		}
	}
	
	private final smCameraManager m_cameraMngr;
	
	public Action_Camera_SetViewSize(smCameraManager cameraMngr)
	{
		m_cameraMngr = cameraMngr;
	}
	
	@Override
	public void perform(smA_ActionArgs args)
	{
		Args args_cast = (Args) args;
		StateMachine_Camera machine = this.getState();
		
		m_cameraMngr.getCamera().setViewRect(args_cast.m_dimensions[0], args_cast.m_dimensions[1]);
		
		if( machine.getCurrentState() instanceof State_ViewingCell )
		{
			State_ViewingCell viewingState = machine.getCurrentState();
			smCamera camera = m_cameraMngr.getCamera();
		}
		
		if( args_cast.m_updateBuffer )
		{
			machine.updateBufferManager();
		}
		
		if( machine.getCurrentState() instanceof State_CameraFloating )
		{
			m_cameraMngr.setTargetPosition(m_cameraMngr.getTargetPosition(), false); // refreshes Z-constraints if necessary.
		}
		else if( machine.getCurrentState() == null )
		{
			m_cameraMngr.setCameraPosition(m_cameraMngr.getTargetPosition(), true);
		}
	}

	@Override
	public boolean isPerformableInBackground()
	{
		return true;
	}
}