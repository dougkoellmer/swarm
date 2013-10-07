package swarm.client.states.camera;

import swarm.client.app.smAppContext;
import swarm.client.managers.smGridManager;
import swarm.shared.entities.smA_Grid;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smGridCoordinate;

public class Action_Camera_SnapToCoordinate extends smA_CameraAction
{
	public static class Args extends smA_ActionArgs
	{
		private smGridCoordinate m_coordinate;
		private smCellAddress m_address;
		
		private boolean m_onlyCausedRefresh = false;
		
		public Args()
		{
			m_coordinate = null;
		}
		
		public Args(smGridCoordinate coord)
		{
			init(coord);
		}
		
		public void init(smGridCoordinate coordinate)
		{
			m_coordinate = coordinate;
			m_address = null;
		}
		
		void init(smCellAddress address, smGridCoordinate coordinate)
		{
			m_address = address;
			m_coordinate = coordinate;
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
		((StateMachine_Camera)this.getState()).snapToCoordinate(((Args) args).m_address, coordinate);
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
		
		if( currentState instanceof State_CameraSnapping )
		{
			return !((State_CameraSnapping)currentState).getTargetCoordinate().isEqualTo(coordinate);
		}
		else
		{
			return true;
		}
	}
}