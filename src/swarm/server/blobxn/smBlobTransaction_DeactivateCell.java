package swarm.server.blobxn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;






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
import swarm.shared.structs.smCellAddress;

public class smBlobTransaction_DeactivateCell extends smA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(smBlobTransaction_DeactivateCell.class.getName());
	
	private final smServerCellAddressMapping m_mapping;
	
	private smServerCellAddressMapping m_newMapping;
	
	public smBlobTransaction_DeactivateCell(smServerCellAddressMapping mapping)
	{
		m_mapping = mapping;
	}
	
	public smServerCellAddressMapping getNewMapping()
	{
		return m_newMapping;
	}
	
	@Override
	protected void performOperations() throws smBlobException
	{
		m_newMapping = null;
		
		smI_BlobManager blobManager = m_blobMngrFactory.create(smE_BlobCacheLevel.MEMCACHE, smE_BlobCacheLevel.PERSISTENT);
		
		smServerGrid activeGrid = blobManager.getBlob(smE_GridType.ACTIVE, smServerGrid.class);
		
		if( activeGrid == null || activeGrid.isEmpty() )
		{
			throw new smBlobException("Grid was not supposed to be null or empty.");
		}
		
		smServerCellAddressMapping mappingToDelete = m_mapping;
		
		smServerCell cell = blobManager.getBlob(mappingToDelete, smServerCell.class);
		
		if( cell == null )
		{
			throw new smBlobException("Could not find cell at mapping: " + mappingToDelete);
		}
		
		HashMap<smI_BlobKey, Class<? extends smI_Blob>> deletions = new HashMap<smI_BlobKey, Class<? extends smI_Blob>>();
		deletions.put(mappingToDelete, smServerCell.class);
		Iterator<smServerCellAddress> addresses = cell.getAddresses();
		while( addresses.hasNext() )
		{
			deletions.put(addresses.next(), smServerCellAddressMapping.class);
		}
		
		blobManager.deleteBlobs(deletions);
		
		activeGrid.markCoordinateAvailable(mappingToDelete.getCoordinate());
		
		blobManager.putBlob(smE_GridType.ACTIVE, activeGrid);
		
		
		/*blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.PERSISTENT);
		
		//--- DRK > Make sure inactive grid exists.
		smServerGrid inactiveGrid = blobManager.getBlob(smE_GridType.INACTIVE, smServerGrid.class);
		if( inactiveGrid == null )
		{
			throw new smBlobException("Inactive grid should have been created before any user or cell is created.");
		}
		
		//--- DRK > Try to find a free coordinate.
		int oldSize = inactiveGrid.getSize();
		smServerGridCoordinate freeCoord = null;
		try
		{
			freeCoord = inactiveGrid.findFreeCoordinate(smS_ServerApp.GRID_EXPANSION_DELTA, null);
		}
		catch(smServerGrid.GridException e)
		{
			s_logger.log(Level.SEVERE, "", e);
			
			throw new smBlobException(e);
		}
		
		//--- DRK > Put the grid back into the database.
		blobManager.putBlob(smE_GridType.INACTIVE, inactiveGrid);
		
		//--- DRK > Create the inactive mapping and dump into db.
		m_newMapping = new smServerCellAddressMapping(smE_GridType.INACTIVE, freeCoord);
		blobManager.putBlob(m_newMapping, cell);*/
	}

	@Override
	protected void onSuccess()
	{
	}
}
