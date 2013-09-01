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
import swarm.shared.structs.smCellAddress;

public class smBlobTransaction_DeactivateCell extends smA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(smBlobTransaction_DeactivateCell.class.getName());
	
	private final smServerCellAddress m_address;
	private final smServerCellAddressMapping m_mapping;
	
	private smServerCellAddressMapping m_newMapping;
	
	public smBlobTransaction_DeactivateCell(smServerCellAddress address)
	{
		m_address = address;
		m_mapping = null;
	}
	
	public smBlobTransaction_DeactivateCell(smServerCellAddressMapping mapping)
	{
		m_address = null;
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
		
		smI_BlobManager blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.MEMCACHE, smE_BlobCacheLevel.PERSISTENT);
		
		smServerGrid activeGrid = blobManager.getBlob(smE_GridType.ACTIVE, smServerGrid.class);
		
		if( activeGrid == null || activeGrid.isEmpty() )
		{
			throw new smBlobException("Grid was not supposed to be null or empty.");
		}
		
		smServerCellAddress addressToDelete = null;
		smServerCellAddressMapping mappingToDelete = null;
		
		if( m_address != null )
		{
			addressToDelete = m_address;
			mappingToDelete = blobManager.getBlob(addressToDelete, smServerCellAddressMapping.class);
			
			if( mappingToDelete == null )
			{
				throw new smBlobException("Could not find mapping for the address: " + addressToDelete.getRawAddressLeadSlash());
			}
		}
		else
		{
			mappingToDelete = m_mapping;
		}
		
		smServerCell cell = blobManager.getBlob(mappingToDelete, smServerCell.class);
		
		if( cell == null )
		{
			throw new smBlobException("Could not find cell at mapping: " + mappingToDelete);
		}
		
		if( addressToDelete == null )
		{
			addressToDelete = cell.getAddress();
		}
		else
		{
			if( !addressToDelete.isEqualTo(cell.getAddress()) )
			{
				throw new smBlobException("Address provided and address found in cell didn't match...something's really messed up.");
			}
		}
		
		blobManager.deleteBlob(addressToDelete, smServerCellAddressMapping.class);
		blobManager.deleteBlob(mappingToDelete, smServerCell.class);
		
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