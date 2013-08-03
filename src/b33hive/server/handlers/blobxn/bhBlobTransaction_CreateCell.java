package b33hive.server.handlers.blobxn;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import b33hive.server.account.bhUserSession;
import b33hive.server.app.bhS_ServerApp;
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
	
	private int m_newGridSize = INVALID_GRID_SIZE;
	private bhServerGrid m_grid = null;
	private bhServerCellAddressMapping m_mapping = null;
	private final bhServerCellAddress m_address;
	private final bhGridCoordinate m_preference;
	private final bhServerCodePrivileges m_privileges;
	
	bhBlobTransaction_CreateCell(bhServerCellAddress cellAddress, bhGridCoordinate preference, bhServerCodePrivileges privileges)
	{
		//--- DRK > debug code
		//preference = preference != null ? preference : new bhServerGridCoordinate(0, 4);
		
		m_privileges = privileges; 
		m_address = cellAddress;
		m_preference = preference;
	}
	
	public boolean didGridGrow()
	{
		return m_newGridSize != INVALID_GRID_SIZE;
	}
	
	public int getNewGridSize()
	{
		return m_newGridSize;
	}
	
	public bhServerGrid getGrid()
	{
		return m_grid;
	}
	
	protected void clear()
	{
		m_newGridSize = INVALID_GRID_SIZE;
		m_grid = null;
		m_mapping = null;
	}
	
	protected bhServerCellAddress getAddress()
	{
		return m_address;
	}
	
	protected bhServerCellAddressMapping getMapping()
	{
		return m_mapping;
	}

	@Override
	protected void performOperations() throws bhBlobException
	{
		if( m_address.getParseError() != bhE_CellAddressParseError.NO_ERROR )
		{
			throw new bhBlobException("Address has a parse error! " + m_address);
		}
		
		this.clear();
		
		bhI_BlobManager blobManager = bhBlobManagerFactory.getInstance().create(bhE_BlobCacheLevel.PERSISTENT);
		
		//--- DRK > Make sure grid exists.
		m_grid = blobManager.getBlob(bhE_GridType.ACTIVE, bhServerGrid.class);
		if( m_grid == null )
		{
			throw new bhBlobException("Grid should have been created before any user or cell is created.");
		}
		
		//--- DRK > Try to find a free coordinate.
		int oldSize = m_grid.getSize();
		bhServerGridCoordinate freeCoord = null;
		try
		{
			freeCoord = m_grid.findFreeCoordinate(bhS_ServerApp.GRID_EXPANSION_DELTA, m_preference);
		}
		catch(bhServerGrid.GridException e)
		{
			s_logger.log(Level.SEVERE, "", e);
			
			throw new bhBlobException(e);
		}
		
		//--- DRK > Put the grid back into the database and see if the grid has grown.
		blobManager.putBlob(bhE_GridType.ACTIVE, m_grid);
		int newSize = m_grid.getSize();
		if( newSize != oldSize )
		{
			m_newGridSize = newSize;
		}
		
		//--- DRK > Add cell address to the database.
		m_mapping = new bhServerCellAddressMapping(bhE_GridType.ACTIVE, freeCoord);
		blobManager.putBlob(m_address, m_mapping);
		
		//--- DRK > Add an empty cell to the database.
		bhServerCell cell = m_privileges != null ? new bhServerCell(m_address, m_privileges) : new bhServerCell(m_address);
		blobManager.putBlob(m_mapping, cell);
		
		//TODO: All of the above "puts" might be doable in a batch...google docs say no because of different blob types, but I suspect the docs are out of date.
	}
	
	@Override
	protected void onSuccess()
	{
		bhI_BlobManager blobManager = bhBlobManagerFactory.getInstance().create(bhE_BlobCacheLevel.MEMCACHE);
		
		//--- DRK > We only update the grid if it grew...we only care that a grid of appropriate size is cached,
		//---		not a grid with accurate cell ownerships.  We only need that when we create a new user or add
		//---		a cell to an existing user's account, and in that case we bypass the cache to ensure a fresh copy (see above).
		if( this.didGridGrow() )
		{
			Map<bhI_BlobKeySource, bhI_Blob> values = new HashMap<bhI_BlobKeySource, bhI_Blob>();
			
			values.put(m_address, m_mapping);
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
			try
			{
				blobManager.putBlobAsync(m_address, m_mapping);
			}
			catch(bhBlobException e)
			{
				s_logger.log(Level.WARNING, "Could not cache address mapping from create user blob transaction.", e);
			}
		}
	}
}
