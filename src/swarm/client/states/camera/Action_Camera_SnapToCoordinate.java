package swarm.client.states.camera;

import swarm.client.app.smAppContext;
import swarm.client.states.camera.StateMachine_Camera.Action_SnapToCoordinate.Args;
import swarm.shared.entities.smA_Grid;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smGridCoordinate;

public class Action_Camera_SnapToCoordinate extends smA_CameraAction
{
	public static class Args extends smA_ActionArgs
	{
		private smGridCoordinate m_coordinate;
		private boolean m_onlyCausedRefresh = false;
		
		public Args()
		{
			m_coordinate = null;
		}
		
		public Args(smGridCoordinate coord)
		{
			m_coordinate = coord;
		}
		
		public void setCoordinate(smGridCoordinate coordinate)
		{
			m_coordinate = coordinate;
		}
		
		public boolean onlyCausedRefresh()
		{
			return m_onlyCausedRefresh;
		}
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
		((StateMachine_Camera)this.getState()).snapToCoordinate(null, coordinate);
	}
	
	@Override
	public boolean isPerformable(smA_ActionArgs args)
	{
		smGridCoordinate coordinate = ((Args) args).m_coordinate;
		
		smA_Grid grid = smAppContext.gridMngr.getGrid();
		
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
	
	@Override
	public Class<? extends smA_State> getStateAssociation()
	{
		return StateMachine_Camera.class;
	}
}