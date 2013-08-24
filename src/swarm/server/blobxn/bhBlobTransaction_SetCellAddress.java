package swarm.server.blobxn;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.sm_s;
import swarm.server.data.blob.bhA_BlobTransaction;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhI_Blob;
import swarm.server.data.blob.bhI_BlobKey;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.entities.bhE_GridType;
import swarm.server.entities.bhServerCell;
import swarm.server.entities.bhServerGrid;
import swarm.server.structs.bhServerCellAddress;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.structs.bhServerGridCoordinate;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.structs.bhCellAddress;
import swarm.shared.structs.bhGridCoordinate;

public class bhBlobTransaction_SetCellAddress extends bhA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(bhBlobTransaction_SetCellAddress.class.getName());
	
	private final bhServerCellAddressMapping m_mapping;
	private final bhServerCellAddress[] m_addresses;
	
	public bhBlobTransaction_SetCellAddress(bhServerCellAddressMapping mapping, bhServerCellAddress[] addresses)
	{
		m_mapping = mapping;
		m_addresses = addresses;
	}
	
	@Override
	protected void performOperations() throws bhBlobException
	{
		bhI_BlobManager blobManager = sm_s.blobMngrFactory.create(bhE_BlobCacheLevel.MEMCACHE, bhE_BlobCacheLevel.PERSISTENT);
		
		bhServerGrid activeGrid = blobManager.getBlob(bhE_GridType.ACTIVE, bhServerGrid.class);
		
		if( activeGrid == null || activeGrid.isEmpty() )
		{
			throw new bhBlobException("Grid was not supposed to be null or empty.");
		}
		
		bhServerCell cell;
		try
		{
			cell = blobManager.getBlob(m_mapping, bhServerCell.class);
			
			if( cell == null )
			{
				throw new bhBlobException("Expected cell at mapping.");
			}
		}
		catch(bhBlobException e)
		{
			throw new bhBlobException("Problem confirming cell at mapping.");
		}
		
		HashMap<bhI_BlobKey, bhI_Blob> batch = new HashMap<bhI_BlobKey, bhI_Blob>();
		
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
