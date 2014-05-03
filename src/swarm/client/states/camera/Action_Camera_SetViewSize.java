package swarm.client.states.camera;

import swarm.client.entities.Camera;
import swarm.client.managers.CameraManager;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_State;
import swarm.shared.structs.Point;

public class Action_Camera_SetViewSize extends A_Action
{
	public static class Args extends StateArgs
	{
		private final double[] m_dimensions = new double[2];
		private boolean m_updateBuffer = true;
		private boolean m_maintainApparentCameraPosition = false;
		
		public void init(double width, double height, boolean updateBuffer, boolean maintainApparentCameraPosition)
		{
			m_dimensions[0] = width;
			m_dimensions[1] = height;
			m_updateBuffer = updateBuffer;
			m_maintainApparentCameraPosition = maintainApparentCameraPosition;
		}
		
		public boolean updateBuffer()
		{
			return m_updateBuffer;
		}
	}
	
	private final Point m_utilPoint1 = new Point();
	
	private final CameraManager m_cameraMngr;
	
	public Action_Camera_SetViewSize(CameraManager cameraMngr)
	{
		m_cameraMngr = cameraMngr;
	}
	
	@Override
	public void perform(StateArgs args)
	{
		Args args_cast = (Args) args;
		StateMachine_Camera machine = this.getState();
		Camera camera = m_cameraMngr.getCamera();
		
		
		double deltaX = args_cast.m_dimensions[0] - camera.getViewWidth();
		double deltaY = args_cast.m_dimensions[1] - camera.getViewHeight();
		
		m_cameraMngr.getCamera().setViewRect(args_cast.m_dimensions[0], args_cast.m_dimensions[1]);
		
		if( machine.getCurrentState() instanceof State_CameraFloating )
		{
			m_cameraMngr.setTargetPosition(m_cameraMngr.getTargetPosition(), false, true); // refreshes Z-constraints if necessary.
		}
		else if( machine.getCurrentState() == null )
		{
			m_cameraMngr.setCameraPosition(m_cameraMngr.getTargetPosition(), true);
		}
		
		if( args_cast.m_maintainApparentCameraPosition )
		{
			m_cameraMngr.shiftCamera(deltaX/2, deltaY/2);
		}
		
		if( args_cast.m_updateBuffer )
		{
			machine.updateBufferManager();
		}
	}

	@Override
	public boolean isPerformableInBackground()
	{
		return true;
	}
}