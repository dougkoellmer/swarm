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

public class BlobTransaction_ClearCell extends A_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(BlobTransaction_ClearCell.class.getName());
	
	private final ServerCellAddress m_address;
	
	public BlobTransaction_ClearCell(ServerCellAddress address)
	{
		m_address = address;
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
		
		ServerCellAddressMapping mapping = null;
		
		if( m_address != null && m_address.isValid() )
		{
			mapping = blobManager.getBlob(m_address, ServerCellAddressMapping.class);
			
			if( mapping == null )
			{
				throw new BlobException("Could not find mapping for the address: " + m_address.getRawAddressLeadSlash());
			}
		}
		else
		{
			throw new BlobException("Null or invalid address given.");
		}
		
		ServerCell cell = blobManager.getBlob(mapping, ServerCell.class);
		
		if( cell == null )
		{
			throw new BlobException("Could not find cell at mapping: " + mapping);
		}
		
		cell.setCode(E_CodeType.SOURCE, null);
		cell.setCode(E_CodeType.SPLASH, null);
		cell.setCode(E_CodeType.COMPILED, null);
		
		blobManager.putBlob(mapping, cell);
	}

	@Override
	protected void onSuccess()
	{
	}
}
