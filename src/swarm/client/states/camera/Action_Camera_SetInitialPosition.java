package swarm.client.states.camera;

import swarm.client.managers.smCameraManager;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smPoint;

//TODO: Minor issue here is I think this has to be called before SetCameraViewSize by the view.
//		Could make Action API for this machine more robust by allowing any order of calls.
public class Action_Camera_SetInitialPosition extends smA_CameraAction
{
	public static class Args extends smA_ActionArgs
	{
		private smPoint m_point;
		
		public void setPoint(smPoint point)
		{
			m_point = point;
		}
	}
	
	private final smCameraManager m_cameraMngr;
	
	Action_Camera_SetInitialPosition(smCameraManager cameraMngr)
	{
		m_cameraMngr = cameraMngr;
	}
	
	@Override
	public void perform(smA_ActionArgs args)
	{
		StateMachine_Camera machine = this.getState();
		
		smPoint point = ((Args)args).m_point;
		
		m_cameraMngr.setCameraPosition(point, false);
	}
	
	@Override
	public boolean isPerformable(smA_ActionArgs args)
	{
		StateMachine_Camera machine = this.getState();
		return machine.getUpdateCount() == 0;
	}
}
