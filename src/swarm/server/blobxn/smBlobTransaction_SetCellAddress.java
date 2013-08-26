package swarm.server.blobxn;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.sm_s;
import swarm.server.data.blob.smA_BlobTransaction;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smI_BlobKey;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smE_GridType;
import swarm.server.entities.smServerCell;
import swarm.server.entities.smServerGrid;
import swarm.server.structs.smServerCellAddress;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.structs.smServerGridCoordinate;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smGridCoordinate;

public class smBlobTransaction_SetCellAddress extends smA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(smBlobTransaction_SetCellAddress.class.getName());
	
	private final smServerCellAddressMapping m_mapping;
	private final smServerCellAddress[] m_addresses;
	
	public smBlobTransaction_SetCellAddress(smServerCellAddressMapping mapping, smServerCellAddress[] addresses)
	{
		m_mapping = mapping;
		m_addresses = addresses;
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
		
		HashMap<smI_BlobKey, smI_Blob> batch = new HashMap<smI_BlobKey, smI_Blob>();
		
		for( int i = 0; i < m_addresses.length; i++ )
		{
			batch.put(m_addresses[i], m_mapping);
		}
		
		cell.setAddress(m_addresses[0]);
		
		batch.put(m_mapping, cell);
		
		blobManager.putBlobs(batch);
	}

	@Override
	protected void onSuccess()
	{
	}
}
