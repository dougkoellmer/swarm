package swarm.server.blobxn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;






import swarm.server.data.blob.A_BlobTransaction;
import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.E_GridType;
import swarm.server.entities.ServerCell;
import swarm.server.entities.BaseServerGrid;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.structs.ServerGridCoordinate;
import swarm.shared.structs.CellAddress;

public class BlobTransaction_DeactivateCell extends A_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(BlobTransaction_DeactivateCell.class.getName());
	
	private final ServerCellAddressMapping m_mapping;
	
	private ServerCellAddressMapping m_newMapping;
	
	public BlobTransaction_DeactivateCell(ServerCellAddressMapping mapping)
	{
		m_mapping = mapping;
	}
	
	public ServerCellAddressMapping getNewMapping()
	{
		return m_newMapping;
	}
	
	@Override
	protected void performOperations() throws BlobException
	{
		m_newMapping = null;
		
		I_BlobManager blobManager = m_blobMngrFactory.create(E_BlobCacheLevel.MEMCACHE, E_BlobCacheLevel.PERSISTENT);
		
		BaseServerGrid activeGrid = blobManager.getBlob(E_GridType.ACTIVE, BaseServerGrid.class);
		
		if( activeGrid == null || activeGrid.isEmpty() )
		{
			throw new BlobException("Grid was not supposed to be null or empty.");
		}
		
		ServerCellAddressMapping mappingToDelete = m_mapping;
		
		ServerCell cell = blobManager.getBlob(mappingToDelete, ServerCell.class);
		
		if( cell == null )
		{
			throw new BlobException("Could not find cell at mapping: " + mappingToDelete);
		}
		
		HashMap<I_BlobKey, Class<? extends I_Blob>> deletions = new HashMap<I_BlobKey, Class<? extends I_Blob>>();
		deletions.put(mappingToDelete, ServerCell.class);
		Iterator<ServerCellAddress> addresses = cell.getAddresses();
		while( addresses.hasNext() )
		{
			deletions.put(addresses.next(), ServerCellAddressMapping.class);
		}
		
		blobManager.deleteBlobs(deletions);
		
		activeGrid.markCoordinateAvailable(mappingToDelete.getCoordinate());
		
		blobManager.putBlob(E_GridType.ACTIVE, activeGrid);
		
		
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
