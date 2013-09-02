package swarm.server.blobxn;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.smUserSession;

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
import swarm.server.entities.smServerUser;
import swarm.server.structs.smServerCellAddress;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.structs.smServerCodePrivileges;
import swarm.server.structs.smServerGridCoordinate;
import swarm.shared.structs.smCodePrivileges;
import swarm.shared.structs.smE_CellAddressParseError;
import swarm.shared.structs.smGridCoordinate;

public class smBlobTransaction_CreateCell extends smA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(smBlobTransaction_CreateCell.class.getName());
	
	private static final int INVALID_GRID_SIZE = -1;
	
	private boolean m_didGridGrow;
	
	private int m_gridWidth = INVALID_GRID_SIZE;
	private int m_gridHeight = INVALID_GRID_SIZE;
	
	private smServerGrid m_grid = null;
	private smServerCellAddressMapping m_mapping = null;
	private final smServerCellAddress[] m_addresses;
	private final smGridCoordinate m_preference;
	private final smServerCodePrivileges m_privileges;
	
	public smBlobTransaction_CreateCell(smServerCellAddress[] cellAddresses, smGridCoordinate preference, smServerCodePrivileges privileges)
	{
		//--- DRK > debug code
		//preference = preference != null ? preference : new smServerGridCoordinate(0, 4);
		
		m_privileges = privileges; 
		m_addresses = cellAddresses;
		m_preference = preference;
	}
	
	public boolean didGridGrow()
	{
		return m_didGridGrow;
	}
	
	public int getGridWidth()
	{
		return m_gridWidth;
	}
	
	public int getGridHeight()
	{
		return m_gridHeight;
	}
	
	public smServerGrid getGrid()
	{
		return m_grid;
	}
	
	protected void clear()
	{
		m_didGridGrow = false;
		m_gridWidth = INVALID_GRID_SIZE;
		m_gridHeight = INVALID_GRID_SIZE;
		m_grid = null;
		m_mapping = null;
	}
	
	protected smServerCellAddress[] getAddresses()
	{
		return m_addresses;
	}
	
	protected smServerCellAddressMapping getMapping()
	{
		return m_mapping;
	}

	@Override
	protected void performOperations() throws smBlobException
	{
		this.clear();
		
		smI_BlobManager blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.PERSISTENT);
		
		//--- DRK > Make sure grid exists.
		m_grid = blobManager.getBlob(smE_GridType.ACTIVE, smServerGrid.class);
		if( m_grid == null )
		{
			throw new smBlobException("Grid should have been created before any user or cell is created.");
		}
		
		//--- DRK > Try to find a free coordinate.
		int oldWidth = m_grid.getWidth();
		int oldHeight = m_grid.getHeight();
		
		smServerGridCoordinate freeCoord = null;
		try
		{
			freeCoord = m_grid.findFreeCoordinate(sm_s.app.getConfig().gridExpansionDelta, m_preference);
		}
		catch(smServerGrid.GridException e)
		{
			s_logger.log(Level.SEVERE, "", e);
			
			throw new smBlobException(e);
		}
		
		//--- DRK > Put the grid back into the database and see if the grid has grown.
		blobManager.putBlob(smE_GridType.ACTIVE, m_grid);
		int newWidth = m_grid.getWidth();
		int newHeight = m_grid.getHeight();
		if( newWidth != oldWidth || newHeight != m_grid.getHeight() )
		{
			m_didGridGrow = true;
		}
		
		//--- DRK > Add cell addresses to the database.
		m_mapping = new smServerCellAddressMapping(smE_GridType.ACTIVE, freeCoord);
		HashMap<smI_BlobKey, smI_Blob> mappings = new HashMap<smI_BlobKey, smI_Blob>();
		for( int i = 0; i < m_addresses.length; i++ )
		{
			mappings.put(m_addresses[i], m_mapping);
		}
		blobManager.putBlobs(mappings);
		
		//--- DRK > Add an empty cell to the database.
		smServerCell cell = m_privileges != null ? new smServerCell(m_addresses[0], m_privileges) : new smServerCell(m_addresses[0]);
		blobManager.putBlob(m_mapping, cell);
		
		//TODO: All of the above "puts" might be doable in a batch...google docs say no because of different blob types, but I suspect the docs are out of date.
	}
	
	@Override
	protected void onSuccess()
	{
		//TODO: Not sure the reasoning on caching cell addresses was below...commented out for now.
		
		smI_BlobManager blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.MEMCACHE);
		
		//--- DRK > We only update the grid if it grew...we only care that a grid of appropriate size is cached,
		//---		not a grid with accurate cell ownerships.  We only need that when we create a new user or add
		//---		a cell to an existing user's account, and in that case we bypass the cache to ensure a fresh copy (see above).
		if( this.didGridGrow() )
		{
			Map<smI_BlobKey, smI_Blob> values = new HashMap<smI_BlobKey, smI_Blob>();

			/*( int i = 0; i < m_addresses.length; i++ )
			{
				values.put(m_addresses[i], m_mapping);
			}*/
			values.put(smE_GridType.ACTIVE, m_grid);
			
			try
			{
				blobManager.putBlobsAsync(values);
			}
			catch(smBlobException e)
			{
				s_logger.log(Level.WARNING, "Could not cache batch data from create user blob transaction.", e);
			}
		}
		else
		{
			/*try
			{
				blobManager.putBlobAsync(m_address, m_mapping);
			}
			catch(smBlobException e)
			{
				s_logger.log(Level.WARNING, "Could not cache address mapping from create user blob transaction.", e);
			}*/
		}
	}
}
