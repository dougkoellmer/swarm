package swarm.server.blobxn;

import java.util.logging.Level;
import java.util.logging.Logger;


import swarm.server.data.blob.A_BlobTransaction;
import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.E_GridType;
import swarm.server.entities.ServerCell;
import swarm.server.entities.BaseServerGrid;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.structs.ServerGridCoordinate;
import swarm.shared.entities.E_CodeType;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CodePrivileges;
import swarm.shared.structs.GridCoordinate;

public class BlobTransaction_SetCellPrivileges extends A_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(BlobTransaction_SetCellPrivileges.class.getName());
	
	private final ServerCellAddressMapping m_mapping;
	private final CodePrivileges m_privileges;
	
	public BlobTransaction_SetCellPrivileges(ServerCellAddressMapping mapping, CodePrivileges privileges)
	{
		m_mapping = mapping;
		m_privileges = privileges;
	}
	
	@Override
	protected void performOperations() throws BlobException
	{
		I_BlobManager blobManager = m_blobMngrFactory.create(E_BlobCacheLevel.MEMCACHE, E_BlobCacheLevel.PERSISTENT);
		
		BaseServerGrid activeGrid = blobManager.getBlob(E_GridType.ACTIVE, BaseServerGrid.class);
		
		if( activeGrid == null || activeGrid.isEmpty() )
		{
			throw new BlobException("Grid was not supposed to be null or empty.");
		}
		
		ServerCell cell;
		try
		{
			cell = blobManager.getBlob(m_mapping, ServerCell.class);
			
			if( cell == null )
			{
				throw new BlobException("Expected cell at mapping.");
			}
		}
		catch(BlobException e)
		{
			throw new BlobException("Problem confirming cell at mapping.");
		}
		
		cell.getCodePrivileges().copy(m_privileges);
		
		blobManager.putBlob(m_mapping, cell);
	}

	@Override
	protected void onSuccess()
	{
	}
}
