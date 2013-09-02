package swarm.server.blobxn;

import java.util.logging.Level;
import java.util.logging.Logger;


import swarm.server.data.blob.smA_BlobTransaction;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smE_GridType;
import swarm.server.entities.smServerCell;
import swarm.server.entities.smServerGrid;
import swarm.server.structs.smServerCellAddress;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.structs.smServerGridCoordinate;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCodePrivileges;
import swarm.shared.structs.smGridCoordinate;

public class smBlobTransaction_SetCellPrivileges extends smA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(smBlobTransaction_SetCellPrivileges.class.getName());
	
	private final smServerCellAddressMapping m_mapping;
	private final smCodePrivileges m_privileges;
	
	public smBlobTransaction_SetCellPrivileges(smServerCellAddressMapping mapping, smCodePrivileges privileges)
	{
		m_mapping = mapping;
		m_privileges = privileges;
	}
	
	@Override
	protected void performOperations() throws smBlobException
	{
		smI_BlobManager blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.MEMCACHE, smE_BlobCacheLevel.PERSISTENT);
		
		smServerGrid activeGrid = blobManager.getBlob(smE_GridType.ACTIVE, smServerGrid.class);
		
		if( activeGrid == null || activeGrid.isEmpty() )
		{
			throw new smBlobException("Grid was not supposed to be null or empty.");
		}
		
		smServerCell cell;
		try
		{
			cell = blobManager.getBlob(m_mapping, smServerCell.class);
			
			if( cell == null )
			{
				throw new smBlobException("Expected cell at mapping.");
			}
		}
		catch(smBlobException e)
		{
			throw new smBlobException("Problem confirming cell at mapping.");
		}
		
		cell.getCodePrivileges().copy(m_privileges);
		
		blobManager.putBlob(m_mapping, cell);
	}

	@Override
	protected void onSuccess()
	{
	}
}
