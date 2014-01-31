package swarm.server.blobxn;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.UserSession;

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
import swarm.server.entities.ServerUser;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.structs.ServerCodePrivileges;
import swarm.server.structs.ServerGridCoordinate;
import swarm.shared.structs.CodePrivileges;
import swarm.shared.structs.E_CellAddressParseError;
import swarm.shared.structs.GridCoordinate;

public class BlobTransaction_CreateCell extends A_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(BlobTransaction_CreateCell.class.getName());
	
	private static final int INVALID_GRID_SIZE = -1;
	
	private boolean m_didGridGrow;
	
	private int m_gridWidth = INVALID_GRID_SIZE;
	private int m_gridHeight = INVALID_GRID_SIZE;
	
	private BaseServerGrid m_grid = null;
	private ServerCellAddressMapping m_mapping = null;
	private final ServerCellAddress[] m_addresses;
	private final GridCoordinate m_preference;
	private final ServerCodePrivileges m_privileges;
	private final int m_gridExpansionDelta;
	
	public BlobTransaction_CreateCell(ServerCellAddress[] cellAddresses, GridCoordinate preference, ServerCodePrivileges privileges, int gridExpansionDelta)
	{
		//--- DRK > debug code
		//preference = preference != null ? preference : new smServerGridCoordinate(0, 4);
		
		m_privileges = privileges; 
		m_addresses = cellAddresses;
		m_preference = preference;
		m_gridExpansionDelta = gridExpansionDelta;
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
	
	public BaseServerGrid getGrid()
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
	
	protected ServerCellAddress[] getAddresses()
	{
		return m_addresses;
	}
	
	protected ServerCellAddressMapping getMapping()
	{
		return m_mapping;
	}

	@Override
	protected void performOperations() throws BlobException
	{
		this.clear();
		
		I_BlobManager blobManager = m_blobMngrFactory.create(E_BlobCacheLevel.PERSISTENT);
		
		//--- DRK > Make sure grid exists.
		m_grid = blobManager.getBlob(E_GridType.ACTIVE, BaseServerGrid.class);
		if( m_grid == null )
		{
			throw new BlobException("Grid should have been created before any user or cell is created.");
		}
		
		//--- DRK > Try to find a free coordinate.
		int oldWidth = m_grid.getWidth();
		int oldHeight = m_grid.getHeight();
		
		ServerGridCoordinate freeCoord = null;
		try
		{
			freeCoord = m_grid.findFreeCoordinate(m_gridExpansionDelta, m_preference);
		}
		catch(BaseServerGrid.GridException e)
		{
			s_logger.log(Level.SEVERE, "", e);
			
			throw new BlobException(e);
		}
		
		//--- DRK > Put the grid back into the database and see if the grid has grown.
		blobManager.putBlob(E_GridType.ACTIVE, m_grid);
		int newWidth = m_grid.getWidth();
		int newHeight = m_grid.getHeight();
		if( newWidth != oldWidth || newHeight != m_grid.getHeight() )
		{
			m_didGridGrow = true;
		}
		
		//--- DRK > Add cell addresses to the database.
		m_mapping = new ServerCellAddressMapping(E_GridType.ACTIVE, freeCoord);
		HashMap<I_BlobKey, I_Blob> mappings = new HashMap<I_BlobKey, I_Blob>();
		for( int i = 0; i < m_addresses.length; i++ )
		{
			mappings.put(m_addresses[i], m_mapping);
		}
		blobManager.putBlobs(mappings);
		
		//--- DRK > Add an empty cell to the database.
		ServerCell cell = m_privileges != null ? new ServerCell(m_privileges, m_addresses) : new ServerCell(m_addresses[0]);
		blobManager.putBlob(m_mapping, cell);
		
		//TODO: All of the above "puts" might be doable in a batch...google docs say no because of different blob types, but I suspect the docs are out of date.
	}
	
	@Override
	protected void onSuccess()
	{
		//TODO: Not sure the reasoning on caching cell addresses was below...commented out for now.
		
		I_BlobManager blobManager = m_blobMngrFactory.create(E_BlobCacheLevel.MEMCACHE);
		
		//--- DRK > We only update the grid if it grew...we only care that a grid of appropriate size is cached,
		//---		not a grid with accurate cell ownerships.  We only need that when we create a new user or add
		//---		a cell to an existing user's account, and in that case we bypass the cache to ensure a fresh copy (see above).
		if( this.didGridGrow() )
		{
			Map<I_BlobKey, I_Blob> values = new HashMap<I_BlobKey, I_Blob>();

			/*( int i = 0; i < m_addresses.length; i++ )
			{
				values.put(m_addresses[i], m_mapping);
			}*/
			values.put(E_GridType.ACTIVE, m_grid);
			
			try
			{
				blobManager.putBlobsAsync(values);
			}
			catch(BlobException e)
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
