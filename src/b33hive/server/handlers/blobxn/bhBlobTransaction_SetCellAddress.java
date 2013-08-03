package b33hive.server.handlers.blobxn;

import java.util.logging.Level;
import java.util.logging.Logger;

import b33hive.server.app.bhS_ServerApp;
import b33hive.server.data.blob.bhA_BlobTransaction;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.entities.bhE_GridType;
import b33hive.server.entities.bhServerCell;
import b33hive.server.entities.bhServerGrid;
import b33hive.server.structs.bhServerCellAddress;
import b33hive.server.structs.bhServerCellAddressMapping;
import b33hive.server.structs.bhServerGridCoordinate;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhGridCoordinate;

public class bhBlobTransaction_SetCellAddress extends bhA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(bhBlobTransaction_SetCellAddress.class.getName());
	
	private final bhServerCellAddressMapping m_mapping;
	private final bhServerCellAddress m_address;
	
	public bhBlobTransaction_SetCellAddress(bhServerCellAddressMapping mapping, bhServerCellAddress address)
	{
		m_mapping = mapping;
		m_address = address;
	}
	
	@Override
	protected void performOperations() throws bhBlobException
	{
		bhI_BlobManager blobManager = bhBlobManagerFactory.getInstance().create(bhE_BlobCacheLevel.MEMCACHE, bhE_BlobCacheLevel.PERSISTENT);
		
		bhServerGrid activeGrid = blobManager.getBlob(bhE_GridType.ACTIVE, bhServerGrid.class);
		
		if( activeGrid == null || activeGrid.getSize() == 0 )
		{
			throw new bhBlobException("Grid was not supposed to be null or empty.");
		}
		
		bhServerCell cell;
		try
		{
			cell = blobManager.getBlob(m_mapping, bhServerCell.class);
			
			if( cell == null )
			{
				throw new bhBlobException("Expected cell at mapping.");
			}
		}
		catch(bhBlobException e)
		{
			throw new bhBlobException("Problem confirming cell at mapping.");
		}
		
		blobManager.putBlob(m_address, m_mapping);
		
		cell.setAddress(m_address);
		
		blobManager.putBlob(m_mapping, cell);
	}

	@Override
	protected void onSuccess()
	{
	}
}
