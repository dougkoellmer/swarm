package b33hive.server.blobxn;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import b33hive.server.account.bhUserSession;
import b33hive.server.account.bh_s;
import b33hive.server.data.blob.bhA_BlobTransaction;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhI_Blob;
import b33hive.server.data.blob.bhI_BlobKeySource;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.entities.bhE_GridType;
import b33hive.server.entities.bhServerCell;
import b33hive.server.entities.bhServerGrid;
import b33hive.server.entities.bhServerUser;
import b33hive.server.structs.bhServerCellAddress;
import b33hive.server.structs.bhServerCellAddressMapping;
import b33hive.server.structs.bhServerCodePrivileges;
import b33hive.server.structs.bhServerGridCoordinate;
import b33hive.shared.structs.bhCodePrivileges;
import b33hive.shared.structs.bhE_CellAddressParseError;
import b33hive.shared.structs.bhGridCoordinate;

public class bhBlobTransaction_CreateCell extends bhA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(bhBlobTransaction_CreateCell.class.getName());
	
	private static final int INVALID_GRID_SIZE = -1;
	
	private boolean m_didGridGrow;
	
	private int m_gridWidth = INVALID_GRID_SIZE;
	private int m_gridHeight = INVALID_GRID_SIZE;
	
	private bhServerGrid m_grid = null;
	private bhServerCellAddressMapping m_mapping = null;
	private final bhServerCellAddress[] m_addresses;
	private final bhGridCoordinate m_preference;
	private final bhServerCodePrivileges m_privileges;
	
	public bhBlobTransaction_CreateCell(bhServerCellAddress[] cellAddresses, bhGridCoordinate preference, bhServerCodePrivileges privileges)
	{
		//--- DRK > debug code
		//preference = preference != null ? preference : new bhServerGridCoordinate(0, 4);
		
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
	
	public bhServerGrid getGrid()
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
	
	protected bhServerCellAddress[] getAddresses()
	{
		return m_addresses;
	}
	
	protected bhServerCellAddressMapping getMapping()
	{
		return m_mapping;
	}

	@Override
	protected void performOperations() throws bhBlobException
	{
		this.clear();
		
		bhI_BlobManager blobManager = bh_s.blobMngrFactory.create(bhE_BlobCacheLevel.PERSISTENT);
		
		//--- DRK > Make sure grid exists.
		m_grid = blobManager.getBlob(bhE_GridType.ACTIVE, bhServerGrid.class);
		if( m_grid == null )
		{
			throw new bhBlobException("Grid should have been created before any user or cell is created.");
		}
		
		//--- DRK > Try to find a free coordinate.
		int oldWidth = m_grid.getWidth();
		int oldHeight = m_grid.getHeight();
		
		bhServerGridCoordinate freeCoord = null;
		try
		{
			freeCoord = m_grid.findFreeCoordinate(bh_s.app.getConfig().gridExpansionDelta, m_preference);
		}
		catch(bhServerGrid.GridException e)
		{
			s_logger.log(Level.SEVERE, "", e);
			
			throw new bhBlobException(e);
		}
		
		//--- DRK > Put the grid back into the database and see if the grid has grown.
		blobManager.putBlob(bhE_GridType.ACTIVE, m_grid);
		int newWidth = m_grid.getWidth();
		int newHeight = m_grid.getHeight();
		if( newWidth != oldWidth || newHeight != m_grid.getHeight() )
		{
			m_didGridGrow = true;
		}
		
		//--- DRK > Add cell addresses to the database.
		m_mapping = new bhServerCellAddressMapping(bhE_GridType.ACTIVE, freeCoord);
		HashMap<bhI_BlobKeySource, bhI_Blob> mappings = new HashMap<bhI_BlobKeySource, bhI_Blob>();
		for( int i = 0; i < m_addresses.length; i++ )
		{
			mappings.put(m_addresses[i], m_mapping);
		}
		blobManager.putBlobs(mappings);
		
		//--- DRK > Add an empty cell to the database.
		bhServerCell cell = m_privileges != null ? new bhServerCell(m_addresses[0], m_privileges) : new bhServerCell(m_addresses[0]);
		blobManager.putBlob(m_mapping, cell);
		
		//TODO: All of the above "puts" might be doable in a batch...google docs say no because of different blob types, but I suspect the docs are out of date.
	}
	
	@Override
	protected void onSuccess()
	{
		//TODO: Not sure the reasoning on caching cell addresses was below...commented out for now.
		
		bhI_BlobManager blobManager = bh_s.blobMngrFactory.create(bhE_BlobCacheLevel.MEMCACHE);
		
		//--- DRK > We only update the grid if it grew...we only care that a grid of appropriate size is cached,
		//---		not a grid with accurate cell ownerships.  We only need that when we create a new user or add
		//---		a cell to an existing user's account, and in that case we bypass the cache to ensure a fresh copy (see above).
		if( this.didGridGrow() )
		{
			Map<bhI_BlobKeySource, bhI_Blob> values = new HashMap<bhI_BlobKeySource, bhI_Blob>();

			/*( int i = 0; i < m_addresses.length; i++ )
			{
				values.put(m_addresses[i], m_mapping);
			}*/
			values.put(bhE_GridType.ACTIVE, m_grid);
			
			try
			{
				blobManager.putBlobsAsync(values);
			}
			catch(bhBlobException e)
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
			catch(bhBlobException e)
			{
				s_logger.log(Level.WARNING, "Could not cache address mapping from create user blob transaction.", e);
			}*/
		}
	}
}
