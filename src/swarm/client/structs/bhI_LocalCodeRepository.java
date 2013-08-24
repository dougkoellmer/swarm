package swarm.client.structs;

import swarm.client.entities.bhBufferCell;
import swarm.shared.entities.bhA_Cell;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.structs.*;

public interface bhI_LocalCodeRepository
{
	boolean tryPopulatingCell(bhGridCoordinate coordinate, bhE_CodeType eType, bhA_Cell outCell);
}
