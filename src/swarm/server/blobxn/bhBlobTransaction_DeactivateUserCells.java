package swarm.server.blobxn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.bhUserSession;
import swarm.server.account.sm_s;
import swarm.server.data.blob.bhA_BlobTransaction;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhE_BlobTransactionType;
import swarm.server.data.blob.bhI_Blob;
import swarm.server.data.blob.bhI_BlobKey;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.entities.bhE_GridType;

import swarm.server.entities.bhServerCell;
import swarm.server.entities.bhServerGrid;
import swarm.server.entities.bhServerUser;
import swarm.server.structs.bhServerCellAddress;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.structs.bhServerGridCoordinate;
import swarm.shared.structs.bhCellAddress;
import swarm.shared.structs.bhCellAddressMapping;
import swarm.shared.structs.bhGridCoordinate;

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
		bhI_BlobManager blobManager = sm_s.blobMngrFactory.create(bhE_BlobCacheLevel.PERSISTENT);
		
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
