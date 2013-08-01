package com.b33hive.server.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.b33hive.server.account.bhUserSession;
import com.b33hive.server.app.bhS_ServerApp;
import com.b33hive.server.data.blob.bhA_BlobTransaction;
import com.b33hive.server.data.blob.bhBlobException;
import com.b33hive.server.data.blob.bhBlobManagerFactory;
import com.b33hive.server.data.blob.bhE_BlobCacheLevel;
import com.b33hive.server.data.blob.bhE_BlobTransactionType;
import com.b33hive.server.data.blob.bhI_Blob;
import com.b33hive.server.data.blob.bhI_BlobKeySource;
import com.b33hive.server.data.blob.bhI_BlobManager;
import com.b33hive.server.entities.bhE_GridType;
import com.b33hive.server.entities.bhS_BlobKeyPrefix;
import com.b33hive.server.entities.bhServerCell;
import com.b33hive.server.entities.bhServerGrid;
import com.b33hive.server.entities.bhServerUser;
import com.b33hive.server.structs.bhServerCellAddress;
import com.b33hive.server.structs.bhServerCellAddressMapping;
import com.b33hive.server.structs.bhServerGridCoordinate;
import com.b33hive.shared.structs.bhCellAddress;
import com.b33hive.shared.structs.bhCellAddressMapping;
import com.b33hive.shared.structs.bhGridCoordinate;

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
		bhI_BlobManager blobManager = bhBlobManagerFactory.getInstance().create(bhE_BlobCacheLevel.PERSISTENT);
		
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
