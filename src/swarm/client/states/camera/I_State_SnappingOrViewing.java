package swarm.client.states.camera;

import swarm.client.entities.BufferCell;
import swarm.shared.structs.GridCoordinate;

public interface I_State_SnappingOrViewing
{
	GridCoordinate getTargetCoord();
	
	BufferCell getCell();
}
