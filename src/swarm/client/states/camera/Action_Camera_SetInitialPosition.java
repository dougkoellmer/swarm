package swarm.client.states.camera;

import swarm.client.managers.CameraManager;
import swarm.shared.statemachine.A_ActionArgs;
import swarm.shared.statemachine.A_State;
import swarm.shared.structs.Point;

//TODO: Minor issue here is I think this has to be called before SetCameraViewSize by the view.
//		Could make Action API for this machine more robust by allowing any order of calls.
public class Action_Camera_SetInitialPosition extends smA_CameraAction
{
	public static class Args extends A_ActionArgs
	{
		private Point m_point;
		
		public void init(Point point)
		{
			m_point = point;
		}
	}
	
	private final CameraManager m_cameraMngr;
	
	Action_Camera_SetInitialPosition(CameraManager cameraMngr)
	{
		m_cameraMngr = cameraMngr;
	}
	
	@Override
	public void perform(A_ActionArgs args)
	{
		StateMachine_Camera machine = this.getState();
		
		Point point = ((Args)args).m_point;
		
		m_cameraMngr.setCameraPosition(point, false);
	}
	
	@Override
	public boolean isPerformable(A_ActionArgs args)
	{
		StateMachine_Camera machine = this.getState();
		return machine.getUpdateCount() == 0;
	}
}
