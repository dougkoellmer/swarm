package swarm.client.states.camera;

import swarm.client.managers.smCameraManager;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;

public class Action_Camera_SetCameraViewSize extends smA_Action
{
	public static class Args extends smA_ActionArgs
	{
		private final double[] m_dimensions = new double[2];
		
		public void set(double width, double height)
		{
			m_dimensions[0] = width;
			m_dimensions[1] = height;
		}
	}
	
	private final smCameraManager m_cameraMngr;
	
	public Action_Camera_SetCameraViewSize(smCameraManager cameraMngr)
	{
		m_cameraMngr = cameraMngr;
	}
	
	@Override
	public void perform(smA_ActionArgs args)
	{
		Args args_cast = (Args) args;
		StateMachine_Camera machine = this.getState();
		
		m_cameraMngr.getCamera().setViewRect(args_cast.m_dimensions[0], args_cast.m_dimensions[1]);
		
		machine.updateBufferManager();
		
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
	public Class<? extends smA_State> getStateAssociation()
	{
		return StateMachine_Camera.class;
	}
	
	@Override
	public boolean isPerformableInBackground()
	{
		return true;
	}
}