package swarm.server.blobxn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.UserSession;

import swarm.server.data.blob.A_BlobTransaction;
import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.I_BlobManager;

import swarm.server.entities.ServerCell;
import swarm.server.entities.BaseServerGrid;
import swarm.server.entities.ServerUser;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.structs.ServerGridCoordinate;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;

public class BlobTransaction_CreateUser extends A_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(BlobTransaction_CreateUser.class.getName());
	
	private final UserSession	m_session;
	
	private ServerUser m_user = null;
	
	private final BlobTransaction_CreateCell m_createCellTransaction;
	
	public BlobTransaction_CreateUser(UserSession session, boolean createHomeCell, int gridExpansionDelta)
	{
		ServerCellAddress[] cellAddresses = {new ServerCellAddress(session.getUsername())};
		m_createCellTransaction = createHomeCell ? new BlobTransaction_CreateCell(cellAddresses, null, null, gridExpansionDelta) : null;
		
		m_session = session;
	}
	
	@Override
	protected void performOperations() throws BlobException
	{
		m_user = null;
		BlobTransaction_CreateCell createCellTxn = m_createCellTransaction;
		
		if( createCellTxn != null )
		{
			try
			{
				performNested(createCellTxn);
			}
			catch(BlobException e )
			{
				s_logger.severe("Couldn't claim cell due to: " + e);
				
				throw e;
			}
		}
		
		I_BlobManager blobManager = m_blobMngrFactory.create(E_BlobCacheLevel.PERSISTENT);
		
		//--- DRK > Add a user to the database.
		m_user = new ServerUser();
		
		if( createCellTxn != null )
		{
			ServerCellAddressMapping mapping = createCellTxn.getMapping();
			m_user.addOwnedCell(mapping);
		}
		
		blobManager.putBlob(m_session, m_user);
	}
	
	public ServerUser getUser()
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
	
	public BaseServerGrid getGrid()
	{
		return m_createCellTransaction.getGrid();
	}
}
