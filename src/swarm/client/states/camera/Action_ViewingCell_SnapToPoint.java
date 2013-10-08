package swarm.client.states.camera;


import swarm.client.managers.smCameraManager;
import swarm.shared.entities.smA_Grid;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smPoint;
import swarm.shared.structs.smRect;
import swarm.shared.utils.smU_Math;

public class Action_ViewingCell_SnapToPoint extends smA_CameraAction
{
	public static class Args extends smA_ActionArgs
	{
		private final smPoint m_point = new smPoint();
		private boolean m_instant;
		
		public Args()
		{
			init(null, false);
		}
		
		public Args(smPoint point)
		{
			init(point, false);
		}
		
		public void init(smPoint point, boolean instant)
		{
			if( point == null ) 
			{
				m_point.zeroOut();
			}
			else
			{
				m_point.copy(point);
			}
			
			m_instant = instant;
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
	private final double m_cellHudHeight;
	private static final smPoint s_utilPoint = new smPoint();
	
	public Action_ViewingCell_SnapToPoint(smCameraManager cameraMngr, double cellHudHeight)
	{
		m_cameraMngr = cameraMngr;
		m_cellHudHeight = cellHudHeight;
	}
	
	@Override
	public void perform(smA_ActionArgs args)
	{
		State_ViewingCell state = this.getState();
		StateMachine_Camera machine = state.getParent();
		smA_Grid grid = state.getCell().getGrid();
		
		smPoint point = ((Args)args).m_point;
		boolean instant = ((Args)args).isInstant();
		
		snapToPoint(point, state, m_cameraMngr, m_cellHudHeight, instant);
		
		m_cameraMngr.setTargetPosition(point, instant);
		
		//--- DRK > If it's instant, it means the view layer's input is requesting a positional change,
		//---		and then will immediately draw the buffer with the assumption that the positional change
		//---		took place, so we have to update the buffer manually instead of waiting for next time step.
		if( instant )
		{
			machine.updateBufferManager();
		}
	}
	
	static void snapToPoint(smPoint point, State_ViewingCell state, smCameraManager cameraMngr, double hudHeight, boolean instant)
	{
		smA_Grid grid = state.getCell().getGrid();
		StateMachine_Camera machine = state.getParent();
		
		double cellWidth = machine.calcViewWindowWidth(grid);
		double cellHeight = machine.calcViewWindowHeight(grid);
		double viewWidth = cameraMngr.getCamera().getViewWidth();
		double viewHeight = cameraMngr.getCamera().getViewHeight();
		
		machine.calcViewWindowCenter(grid, state.getCell().getCoordinate(), s_utilPoint);

		if( viewWidth < cellWidth )
		{
			double diff = (cellWidth - viewWidth)/2;
			double x = smU_Math.clamp(point.getX(), s_utilPoint.getX() - diff, s_utilPoint.getX() - diff);
			s_utilPoint.setX(x);
		}
		
		if( viewHeight < cellHeight )
		{
			double diff = (cellHeight - viewHeight)/2;
			double y = smU_Math.clamp(point.getY(), s_utilPoint.getY() - diff, s_utilPoint.getY() - diff);
			s_utilPoint.setY(y);
		}
		
		cameraMngr.setTargetPosition(s_utilPoint, instant);
	}

	@Override
	public boolean suppressLog()
	{
		return true;
	}
}