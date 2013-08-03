package b33hive.client.structs;

import b33hive.client.entities.bhBufferCell;
import b33hive.shared.entities.bhA_Cell;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.structs.*;

public interface bhI_LocalCodeRepository
{
	boolean tryPopulatingCell(bhGridCoordinate coordinate, bhE_CodeType eType, bhA_Cell outCell);
}
