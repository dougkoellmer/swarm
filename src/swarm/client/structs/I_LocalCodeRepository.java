package swarm.client.structs;

import swarm.client.entities.BufferCell;
import swarm.shared.entities.A_Cell;
import swarm.shared.entities.E_CodeType;
import swarm.shared.structs.*;

public interface I_LocalCodeRepository
{
	boolean tryPopulatingCell(GridCoordinate coordinate, E_CodeType eType, A_Cell outCell);
}
