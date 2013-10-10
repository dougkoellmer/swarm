package swarm.client.states.camera;


import swarm.client.managers.smCameraManager;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smPoint;

public class Action_Camera_SnapToPoint extends smA_CameraAction
{
	public static class Args extends smA_ActionArgs
	{
		private smPoint m_point;
		private boolean m_instant;
		private boolean m_breakViewingState;
		
		public Args()
		{
			m_point = null;
			m_instant = false;
			m_breakViewingState = true;
		}
		
		public void init(smPoint point, boolean instant, boolean breakViewingState)
		{
			m_point = point;
			m_instant = instant;
			m_breakViewingState = breakViewingState;
		}
		
		public smPoint getPoint()
		{
			return m_point;
		}
		
		public boolean isInstant()
		{
			return m_instant;
		}
	}
	
	private final smCameraManager m_cameraMngr;
	
	public Action_Camera_SnapToPoint(smCameraManager cameraMngr)
	{
		m_cameraMngr = cameraMngr;
	}
	
	@Override
	public void perform(smA_ActionArgs args)
	{
		StateMachine_Camera machine = this.getState();
		
		boolean breakViewingState = args != null ? ((Args)args).m_breakViewingState : true;
		
		if( machine.getCurrentState() instanceof State_ViewingCell && breakViewingState )
		{
			machine_setState(machine, State_CameraFloating.class);
		}
		else if( !(machine.getCurrentState() instanceof State_CameraFloating) )
		{
			machine_setState(machine, State_CameraFloating.class);
		}
		
		if( args == null )  return;
		
		smPoint point = ((Args)args).m_point;
		boolean instant = ((Args)args).isInstant();
		
		if( point == null )  return;
		
		m_cameraMngr.setTargetPosition(point, instant);
		
		//--- DRK > If it's instant, it means the view layer's input is requesting a positional change,
		//---		and then will immediately draw the buffer with the assumption that the positional change
		//---		took place, so we have to update the buffer manually instead of waiting for next time step.
		if( instant )
		{
			machine.updateBufferManager();
		}
	}

	@Override
	public boolean suppressLog()
	{
		return true;
	}
}