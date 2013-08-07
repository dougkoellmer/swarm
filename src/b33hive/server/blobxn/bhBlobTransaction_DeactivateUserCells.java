package b33hive.server.blobxn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import b33hive.server.account.bhUserSession;
import b33hive.server.account.bh_s;
import b33hive.server.data.blob.bhA_BlobTransaction;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhE_BlobTransactionType;
import b33hive.server.data.blob.bhI_Blob;
import b33hive.server.data.blob.bhI_BlobKeySource;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.entities.bhE_GridType;
import b33hive.server.entities.bhS_BlobKeyPrefix;
import b33hive.server.entities.bhServerCell;
import b33hive.server.entities.bhServerGrid;
import b33hive.server.entities.bhServerUser;
import b33hive.server.structs.bhServerCellAddress;
import b33hive.server.structs.bhServerCellAddressMapping;
import b33hive.server.structs.bhServerGridCoordinate;
import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhCellAddressMapping;
import b33hive.shared.structs.bhGridCoordinate;

public class bhBlobTransaction_DeactivateUserCells extends bhA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(bhBlobTransaction_DeactivateUserCells.class.getName());
	
	private final bhUserSession	m_session;
	
	public bhBlobTransaction_DeactivateUserCells(bhUserSession session)
	{
		m_session = session;
	}
	
	@Override
	protected void performOperations() throws bhBlobException
	{
		bhI_BlobManager blobManager = bh_s.blobMngrFactory.create(bhE_BlobCacheLevel.PERSISTENT);
		
		bhServerUser user = blobManager.getBlob(m_session, bhServerUser.class);
		
		if( user == null )
		{
			throw new bhBlobException("User came up null.");
		}

		ArrayList<bhServerCellAddressMapping> mappingsForDeactivatedCells = new ArrayList<bhServerCellAddressMapping>();
		Iterator<bhServerCellAddressMapping> iterator = user.getOwnedCells();
		while( iterator.hasNext() )
		{
			bhServerCellAddressMapping mapping = iterator.next();
			
			if( mapping.getGridType() != bhE_GridType.ACTIVE )
			{
				return;
			}
			
			bhBlobTransaction_DeactivateCell deactivateCellTransaction = new bhBlobTransaction_DeactivateCell(mapping);
			deactivateCellTransaction.performOperations();
			
			//mappingsForDeactivatedCells.add(deactivateCellTransaction.getNewMapping());
			
			iterator.remove();
		}
		
		for( int i = 0; i < mappingsForDeactivatedCells.size(); i++ )
		{
			user.addOwnedCell(mappingsForDeactivatedCells.get(i));
		}
		
		blobManager.deleteBlob(m_session, bhServerUser.class);
	}

	@Override
	protected void onSuccess()
	{
	}
}
