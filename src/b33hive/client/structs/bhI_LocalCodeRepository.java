package com.b33hive.client.structs;

import com.b33hive.client.entities.bhBufferCell;
import com.b33hive.shared.entities.bhA_Cell;
import com.b33hive.shared.entities.bhE_CodeType;
import com.b33hive.shared.structs.*;

public interface bhI_LocalCodeRepository
{
	boolean tryPopulatingCell(bhGridCoordinate coordinate, bhE_CodeType eType, bhA_Cell outCell);
}
