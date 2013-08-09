package b33hive.server.blobxn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import b33hive.server.account.bhUserSession;
import b33hive.server.account.bh_s;
import b33hive.server.data.blob.bhA_BlobTransaction;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhI_Blob;
import b33hive.server.data.blob.bhI_BlobKeySource;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.entities.bhS_BlobKeyPrefix;
import b33hive.server.entities.bhServerCell;
import b33hive.server.entities.bhServerGrid;
import b33hive.server.entities.bhServerUser;
import b33hive.server.structs.bhServerCellAddress;
import b33hive.server.structs.bhServerCellAddressMapping;
import b33hive.server.structs.bhServerGridCoordinate;
import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhCellAddressMapping;

public class bhBlobTransaction_CreateUser extends bhA_BlobTransaction
{
	private static final Logger s_logger = Logger.getLogger(bhBlobTransaction_CreateUser.class.getName());
	
	private final bhUserSession	m_session;
	
	private bhServerUser m_user = null;
	
	private final bhBlobTransaction_CreateCell m_createCellTransaction;
	
	public bhBlobTransaction_CreateUser(bhUserSession session, boolean createHomeCell)
	{
		m_createCellTransaction = createHomeCell ? new bhBlobTransaction_CreateCell(new bhServerCellAddress(session.getUsername()), null, null) : null;
		
		m_session = session;
	}
	
	@Override
	protected void performOperations() throws bhBlobException
	{
		m_user = null;
		
		if( m_createCellTransaction != null )
		{
			m_createCellTransaction.performOperations();
		}
		
		bhI_BlobManager blobManager = bh_s.blobMngrFactory.create(bhE_BlobCacheLevel.PERSISTENT);
		
		//--- DRK > Add a user to the database.
		m_user = new bhServerUser();
		
		if( m_createCellTransaction != null )
		{
			bhServerCellAddressMapping mapping = m_createCellTransaction.getMapping();
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
		return m_createCellTransaction.didGridGrow();
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
