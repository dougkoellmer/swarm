package swarm.client.states.camera;

import swarm.client.app.smAppContext;
import swarm.client.managers.smGridManager;
import swarm.shared.entities.smA_Grid;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;

public class Action_Camera_SnapToCoordinate extends smA_CameraAction
{
	public static interface I_Filter
	{
		void adjustTargetPoint(smGridCoordinate targetCoord, smPoint point_out);
		
		void setTargetPoint(smGridCoordinate targetCoord, smPoint point_out);
	}
	
	public static class Args extends smA_ActionArgs
	{
		private smGridCoordinate m_coordinate;
		private smCellAddress m_address;
		private final smPoint m_point = new smPoint();
		private boolean m_hasTargetPoint;
		
		private boolean m_onlyCausedRefresh = false;
		
		public Args()
		{
			init(null);
		}
		
		public void init(smGridCoordinate coordinate)
		{
			m_coordinate = coordinate;
			m_address = null;
			
			m_hasTargetPoint = false;
		}
		
		public void init(smGridCoordinate coordinate, smPoint point)
		{
			m_coordinate = coordinate;
			m_point.copy(point);
			m_address = null;
			
			m_hasTargetPoint = true;
		}
		
		void init(smCellAddress address, smGridCoordinate coordinate)
		{
			m_address = address;
			m_coordinate = coordinate;
			
			m_hasTargetPoint = false;
		}
		
		private void clear()
		{
			m_address = null;
			m_coordinate = null;
			m_hasTargetPoint = false;
		}
		
		public boolean onlyCausedRefresh()
		{
			return m_onlyCausedRefresh;
		}
	}
	
	private final smGridManager m_gridMngr;
	private final I_Filter m_filter;
	
	Action_Camera_SnapToCoordinate(I_Filter filter_nullable, smGridManager gridMngr)
	{
		m_gridMngr = gridMngr;
		m_filter = filter_nullable;
	}
	
	@Override
	public void prePerform(smA_ActionArgs args)
	{
		super.prePerform(args);
		
		if( m_filter != null )
		{
			Args args_cast = (Args) args;
			
			if( args_cast.m_hasTargetPoint )
			{
				m_filter.adjustTargetPoint(args_cast.m_coordinate, args_cast.m_point);
			}
			else
			{
				m_filter.setTargetPoint(args_cast.m_coordinate, args_cast.m_point);
			}
		}
	}
	
	@Override
	public void perform(smA_ActionArgs args)
	{
		smGridCoordinate coordinate = ((Args) args).m_coordinate;
		smPoint point = ((Args) args).m_point;
		StateMachine_Camera machine = this.getState();
		smA_State currentState = machine.getCurrentState();
		
		if( currentState instanceof State_ViewingCell )
		{
			if( ((State_ViewingCell)currentState).getCell().getCoordinate().isEqualTo(coordinate) )
			{
				((State_ViewingCell)currentState).refreshCell();
				((Args) args).m_onlyCausedRefresh = true;
				
				return;
			}
		}
		
		((Args) args).m_onlyCausedRefresh = false;
		((StateMachine_Camera)this.getState()).snapToCoordinate(((Args) args).m_address, coordinate, point);
		
		 ((Args) args).clear();
	}
	
	@Override
	public boolean isPerformable(smA_ActionArgs args)
	{
		smGridCoordinate coordinate = ((Args) args).m_coordinate;
		
		smA_Grid grid = m_gridMngr.getGrid();
		
		if( !grid.isInBounds(coordinate) )
		{
			return false;
		}
		
		if( !grid.isTaken(coordinate) )
		{
			return false;
		}
		
		StateMachine_Camera machine = this.getState();
		smA_State currentState = machine.getCurrentState();

		return true;
	}
}