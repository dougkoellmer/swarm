package b33hive.server.blobxn;

import java.util.logging.Level;
import java.util.logging.Logger;

import b33hive.server.account.bh_s;
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

public class bhBlobTransaction_ClearCell extends bhA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(bhBlobTransaction_ClearCell.class.getName());
	
	private final bhServerCellAddress m_address;
	
	public bhBlobTransaction_ClearCell(bhServerCellAddress address)
	{
		m_address = address;
	}
	
	@Override
	protected void performOperations() throws bhBlobException
	{
		bhI_BlobManager blobManager = bh_s.blobMngrFactory.create(bhE_BlobCacheLevel.MEMCACHE, bhE_BlobCacheLevel.PERSISTENT);
		
		bhServerGrid activeGrid = blobManager.getBlob(bhE_GridType.ACTIVE, bhServerGrid.class);
		
		if( activeGrid == null || activeGrid.getSize() == 0 )
		{
			throw new bhBlobException("Grid was not supposed to be null or empty.");
		}
		
		bhServerCellAddressMapping mapping = null;
		
		if( m_address != null && m_address.isValid() )
		{
			mapping = blobManager.getBlob(m_address, bhServerCellAddressMapping.class);
			
			if( mapping == null )
			{
				throw new bhBlobException("Could not find mapping for the address: " + m_address.getRawAddress());
			}
		}
		else
		{
			throw new bhBlobException("Null or invalid address given.");
		}
		
		bhServerCell cell = blobManager.getBlob(mapping, bhServerCell.class);
		
		if( cell == null )
		{
			throw new bhBlobException("Could not find cell at mapping: " + mapping);
		}
		
		cell.setCode(bhE_CodeType.SOURCE, null);
		cell.setCode(bhE_CodeType.SPLASH, null);
		cell.setCode(bhE_CodeType.COMPILED, null);
		
		blobManager.putBlob(mapping, cell);
	}

	@Override
	protected void onSuccess()
	{
	}
}
