package swarm.server.blobxn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.UserSession;

import swarm.server.data.blob.A_BlobTransaction;
import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.E_BlobTransactionType;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.E_GridType;

import swarm.server.entities.ServerCell;
import swarm.server.entities.BaseServerGrid;
import swarm.server.entities.ServerUser;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.structs.ServerGridCoordinate;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.GridCoordinate;

public class BlobTransaction_DeactivateUserCells extends A_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(BlobTransaction_DeactivateUserCells.class.getName());
	
	private final UserSession	m_session;
	
	public BlobTransaction_DeactivateUserCells(UserSession session)
	{
		m_session = session;
	}
	
	@Override
	protected void performOperations() throws BlobException
	{
		I_BlobManager blobManager = m_blobMngrFactory.create(E_BlobCacheLevel.PERSISTENT);
		
		ServerUser user = blobManager.getBlob(m_session, ServerUser.class);
		
		if( user == null )
		{
			throw new BlobException("User came up null.");
		}

		ArrayList<ServerCellAddressMapping> mappingsForDeactivatedCells = new ArrayList<ServerCellAddressMapping>();
		Iterator<ServerCellAddressMapping> iterator = user.getOwnedCells();
		while( iterator.hasNext() )
		{
			ServerCellAddressMapping mapping = iterator.next();
			
			if( mapping.getGridType() != E_GridType.ACTIVE )
			{
				return;
			}
			
			BlobTransaction_DeactivateCell deactivateCellTransaction = new BlobTransaction_DeactivateCell(mapping);
			deactivateCellTransaction.performOperations();
			
			//mappingsForDeactivatedCells.add(deactivateCellTransaction.getNewMapping());
			
			iterator.remove();
		}
		
		for( int i = 0; i < mappingsForDeactivatedCells.size(); i++ )
		{
			user.addOwnedCell(mappingsForDeactivatedCells.get(i));
		}
		
		blobManager.deleteBlob(m_session, ServerUser.class);
	}

	@Override
	protected void onSuccess()
	{
	}
}
