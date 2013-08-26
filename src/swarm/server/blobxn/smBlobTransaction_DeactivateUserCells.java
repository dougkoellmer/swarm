package swarm.server.blobxn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.smUserSession;
import swarm.server.account.sm_s;
import swarm.server.data.blob.smA_BlobTransaction;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smE_BlobTransactionType;
import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smI_BlobKey;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smE_GridType;

import swarm.server.entities.smServerCell;
import swarm.server.entities.smServerGrid;
import swarm.server.entities.smServerUser;
import swarm.server.structs.smServerCellAddress;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.structs.smServerGridCoordinate;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.structs.smGridCoordinate;

public class smBlobTransaction_DeactivateUserCells extends smA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(smBlobTransaction_DeactivateUserCells.class.getName());
	
	private final smUserSession	m_session;
	
	public smBlobTransaction_DeactivateUserCells(smUserSession session)
	{
		m_session = session;
	}
	
	@Override
	protected void performOperations() throws bhBlobException
	{
		smI_BlobManager blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.PERSISTENT);
		
		smServerUser user = blobManager.getBlob(m_session, smServerUser.class);
		
		if( user == null )
		{
			throw new smBlobException("User came up null.");
		}

		ArrayList<smServerCellAddressMapping> mappingsForDeactivatedCells = new ArrayList<smServerCellAddressMapping>();
		Iterator<smServerCellAddressMapping> iterator = user.getOwnedCells();
		while( iterator.hasNext() )
		{
			smServerCellAddressMapping mapping = iterator.next();
			
			if( mapping.getGridType() != smE_GridType.ACTIVE )
			{
				return;
			}
			
			bhBlobTransaction_DeactivateCell deactivateCellTransaction = new smBlobTransaction_DeactivateCell(mapping);
			deactivateCellTransaction.performOperations();
			
			//mappingsForDeactivatedCells.add(deactivateCellTransaction.getNewMapping());
			
			iterator.remove();
		}
		
		for( int i = 0; i < mappingsForDeactivatedCells.size(); i++ )
		{
			user.addOwnedCell(mappingsForDeactivatedCells.get(i));
		}
		
		blobManager.deleteBlob(m_session, smServerUser.class);
	}

	@Override
	protected void onSuccess()
	{
	}
}
