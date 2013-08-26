package swarm.server.blobxn;

import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.sm_s;
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

public class smBlobTransaction_ClearCell extends smA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(smBlobTransaction_ClearCell.class.getName());
	
	private final smServerCellAddress m_address;
	
	public smBlobTransaction_ClearCell(smServerCellAddress address)
	{
		m_address = address;
	}
	
	@Override
	protected void performOperations() throws bhBlobException
	{
		smI_BlobManager blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.MEMCACHE, smE_BlobCacheLevel.PERSISTENT);
		
		smServerGrid activeGrid = blobManager.getBlob(smE_GridType.ACTIVE, smServerGrid.class);
		
		if( activeGrid == null || activeGrid.isEmpty() )
		{
			throw new smBlobException("Grid was not supposed to be null or empty.");
		}
		
		smServerCellAddressMapping mapping = null;
		
		if( m_address != null && m_address.isValid() )
		{
			mapping = blobManager.getBlob(m_address, smServerCellAddressMapping.class);
			
			if( mapping == null )
			{
				throw new smBlobException("Could not find mapping for the address: " + m_address.getRawAddressLeadSlash());
			}
		}
		else
		{
			throw new smBlobException("Null or invalid address given.");
		}
		
		smServerCell cell = blobManager.getBlob(mapping, smServerCell.class);
		
		if( cell == null )
		{
			throw new smBlobException("Could not find cell at mapping: " + mapping);
		}
		
		cell.setCode(smE_CodeType.SOURCE, null);
		cell.setCode(smE_CodeType.SPLASH, null);
		cell.setCode(smE_CodeType.COMPILED, null);
		
		blobManager.putBlob(mapping, cell);
	}

	@Override
	protected void onSuccess()
	{
	}
}
