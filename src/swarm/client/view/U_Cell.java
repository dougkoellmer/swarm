package swarm.client.view;

import swarm.client.entities.BufferCell;
import swarm.client.states.camera.I_State_SnappingOrViewing;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.shared.statemachine.A_StateContextForwarder;

public class U_Cell
{
	public static BufferCell getBufferCell(A_StateContextForwarder ctx)
	{
		I_State_SnappingOrViewing state = (I_State_SnappingOrViewing)ctx.get(State_CameraSnapping.class, State_ViewingCell.class);
		if( state != null )
		{
			return state.getCell();
		}
		
		return null;
	}
}
