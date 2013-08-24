package swarm.server.blobxn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.bhUserSession;
import swarm.server.account.sm_s;
import swarm.server.data.blob.bhA_BlobTransaction;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhI_Blob;
import swarm.server.data.blob.bhI_BlobKey;
import swarm.server.data.blob.bhI_BlobManager;

import swarm.server.entities.bhServerCell;
import swarm.server.entities.bhServerGrid;
import swarm.server.entities.bhServerUser;
import swarm.server.structs.bhServerCellAddress;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.structs.bhServerGridCoordinate;
import swarm.shared.structs.bhCellAddress;
import swarm.shared.structs.bhCellAddressMapping;

public class bhBlobTransaction_CreateUser extends bhA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(bhBlobTransaction_CreateUser.class.getName());
	
	private final bhUserSession	m_session;
	
	private bhServerUser m_user = null;
	
	private final bhBlobTransaction_CreateCell m_createCellTransaction;
	
	public bhBlobTransaction_CreateUser(bhUserSession session, boolean createHomeCell)
	{
		bhServerCellAddress[] cellAddresses = {new bhServerCellAddress(session.getUsername())};
		m_createCellTransaction = createHomeCell ? new bhBlobTransaction_CreateCell(cellAddresses, null, null) : null;
		
		m_session = session;
	}
	
	@Override
	protected void performOperations() throws bhBlobException
	{
		m_user = null;
		bhBlobTransaction_CreateCell createCellTxn = m_createCellTransaction;
		
		if( createCellTxn != null )
		{
			try
			{
				createCellTxn.performOperations();
			}
			catch(bhBlobException e )
			{
				s_logger.severe("Couldn't claim cell due to: " + e);
			}
			
			createCellTxn = null;
		}
		
		bhI_BlobManager blobManager = sm_s.blobMngrFactory.create(bhE_BlobCacheLevel.PERSISTENT);
		
		//--- DRK > Add a user to the database.
		m_user = new bhServerUser();
		
		if( createCellTxn != null )
		{
			bhServerCellAddressMapping mapping = createCellTxn.getMapping();
			m_user.addOwnedCell(mapping);
		}
		
		blobManager.putBlob(m_session, m_user);
	}
	
	public bhServerUser getUser()
	{
		return m_user;
	}

	@Override
	protected void onSuccess()
	{
		if( m_createCellTransaction != null )
		{
			m_createCellTransaction.onSuccess();
		}
	}
	
	
	public boolean didGridGrow()
	{
		return m_createCellTransaction != null && m_createCellTransaction.didGridGrow();
	}
	
	public int getGridWidth()
	{
		return m_createCellTransaction.getGridWidth();
	}
	
	public int getGridHeight()
	{
		return m_createCellTransaction.getGridHeight();
	}
	
	public bhServerGrid getGrid()
	{
		return m_createCellTransaction.getGrid();
	}
}
