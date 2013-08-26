package swarm.client.structs;

import swarm.client.entities.smBufferCell;
import swarm.shared.entities.smA_Cell;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.*;

public interface smI_LocalCodeRepository
{
	boolean tryPopulatingCell(smGridCoordinate coordinate, smE_CodeType eType, smA_Cell outCell);
}
