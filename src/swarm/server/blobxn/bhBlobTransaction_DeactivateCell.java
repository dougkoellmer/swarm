package swarm.server.blobxn;

import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.sm_s;
import swarm.server.data.blob.bhA_BlobTransaction;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.entities.bhE_GridType;
import swarm.server.entities.bhServerCell;
import swarm.server.entities.bhServerGrid;
import swarm.server.structs.bhServerCellAddress;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.structs.bhServerGridCoordinate;
import swarm.shared.structs.bhCellAddress;

public class bhBlobTransaction_DeactivateCell extends bhA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(bhBlobTransaction_DeactivateCell.class.getName());
	
	private final bhServerCellAddress m_address;
	private final bhServerCellAddressMapping m_mapping;
	
	private bhServerCellAddressMapping m_newMapping;
	
	public bhBlobTransaction_DeactivateCell(bhServerCellAddress address)
	{
		m_address = address;
		m_mapping = null;
	}
	
	public bhBlobTransaction_DeactivateCell(bhServerCellAddressMapping mapping)
	{
		m_address = null;
		m_mapping = mapping;
	}
	
	public bhServerCellAddressMapping getNewMapping()
	{
		return m_newMapping;
	}
	
	@Override
	protected void performOperations() throws bhBlobException
	{
		m_newMapping = null;
		
		bhI_BlobManager blobManager = sm_s.blobMngrFactory.create(bhE_BlobCacheLevel.MEMCACHE, bhE_BlobCacheLevel.PERSISTENT);
		
		bhServerGrid activeGrid = blobManager.getBlob(bhE_GridType.ACTIVE, bhServerGrid.class);
		
		if( activeGrid == null || activeGrid.isEmpty() )
		{
			throw new bhBlobException("Grid was not supposed to be null or empty.");
		}
		
		bhServerCellAddress addressToDelete = null;
		bhServerCellAddressMapping mappingToDelete = null;
		
		if( m_address != null )
		{
			addressToDelete = m_address;
			mappingToDelete = blobManager.getBlob(addressToDelete, bhServerCellAddressMapping.class);
			
			if( mappingToDelete == null )
			{
				throw new bhBlobException("Could not find mapping for the address: " + addressToDelete.getRawAddressLeadSlash());
			}
		}
		else
		{
			mappingToDelete = m_mapping;
		}
		
		bhServerCell cell = blobManager.getBlob(mappingToDelete, bhServerCell.class);
		
		if( cell == null )
		{
			throw new bhBlobException("Could not find cell at mapping: " + mappingToDelete);
		}
		
		if( addressToDelete == null )
		{
			addressToDelete = cell.getAddress();
		}
		else
		{
			if( !addressToDelete.isEqualTo(cell.getAddress()) )
			{
				throw new bhBlobException("Address provided and address found in cell didn't match...something's really messed up.");
			}
		}
		
		blobManager.deleteBlob(addressToDelete, bhServerCellAddressMapping.class);
		blobManager.deleteBlob(mappingToDelete, bhServerCell.class);
		
		activeGrid.markCoordinateAvailable(mappingToDelete.getCoordinate());
		
		blobManager.putBlob(bhE_GridType.ACTIVE, activeGrid);
		
		
		/*blobManager = sm_s.blobMngrFactory.create(bhE_BlobCacheLevel.PERSISTENT);
		
		//--- DRK > Make sure inactive grid exists.
		bhServerGrid inactiveGrid = blobManager.getBlob(bhE_GridType.INACTIVE, bhServerGrid.class);
		if( inactiveGrid == null )
		{
			throw new bhBlobException("Inactive grid should have been created before any user or cell is created.");
		}
		
		//--- DRK > Try to find a free coordinate.
		int oldSize = inactiveGrid.getSize();
		bhServerGridCoordinate freeCoord = null;
		try
		{
			freeCoord = inactiveGrid.findFreeCoordinate(bhS_ServerApp.GRID_EXPANSION_DELTA, null);
		}
		catch(bhServerGrid.GridException e)
		{
			s_logger.log(Level.SEVERE, "", e);
			
			throw new bhBlobException(e);
		}
		
		//--- DRK > Put the grid back into the database.
		blobManager.putBlob(bhE_GridType.INACTIVE, inactiveGrid);
		
		//--- DRK > Create the inactive mapping and dump into db.
		m_newMapping = new bhServerCellAddressMapping(bhE_GridType.INACTIVE, freeCoord);
		blobManager.putBlob(m_newMapping, cell);*/
	}

	@Override
	protected void onSuccess()
	{
	}
}
