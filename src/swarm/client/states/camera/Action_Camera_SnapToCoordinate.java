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
	public static class Args extends smA_ActionArgs
	{
		private smGridCoordinate m_coordinate;
		private smCellAddress m_address;
		private smPoint m_point = null;
		
		private boolean m_onlyCausedRefresh = false;
		
		public Args()
		{
			init(null);
		}
		
		public void init(smGridCoordinate coordinate)
		{
			m_coordinate = coordinate;
			m_address = null;
			m_point = null;
		}
		
		public void init(smGridCoordinate coordinate, smPoint point)
		{
			m_coordinate = coordinate;
			m_point = point;
			m_address = null;
		}
		
		void init(smCellAddress address, smGridCoordinate coordinate)
		{
			m_address = address;
			m_coordinate = coordinate;
			m_point = null;
		}
		
		private void clear()
		{
			m_address = null;
			m_coordinate = null;
			m_point = null;
		}
		
		public boolean onlyCausedRefresh()
		{
			return m_onlyCausedRefresh;
		}
	}
	
	private final smGridManager m_gridMngr;
	
	Action_Camera_SnapToCoordinate(smGridManager gridMngr)
	{
		m_gridMngr = gridMngr;
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