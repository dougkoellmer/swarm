package com.b33hive.server.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.b33hive.server.account.bhUserSession;
import com.b33hive.server.app.bhS_ServerApp;
import com.b33hive.server.data.blob.bhA_BlobTransaction;
import com.b33hive.server.data.blob.bhBlobException;
import com.b33hive.server.data.blob.bhBlobManagerFactory;
import com.b33hive.server.data.blob.bhE_BlobCacheLevel;
import com.b33hive.server.data.blob.bhI_Blob;
import com.b33hive.server.data.blob.bhI_BlobKeySource;
import com.b33hive.server.data.blob.bhI_BlobManager;
import com.b33hive.server.entities.bhS_BlobKeyPrefix;
import com.b33hive.server.entities.bhServerCell;
import com.b33hive.server.entities.bhServerGrid;
import com.b33hive.server.entities.bhServerUser;
import com.b33hive.server.structs.bhServerCellAddress;
import com.b33hive.server.structs.bhServerCellAddressMapping;
import com.b33hive.server.structs.bhServerGridCoordinate;
import com.b33hive.shared.structs.bhCellAddress;
import com.b33hive.shared.structs.bhCellAddressMapping;

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
		
		bhI_BlobManager blobManager = bhBlobManagerFactory.getInstance().create(bhE_BlobCacheLevel.PERSISTENT);
		
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
	
	public int getNewGridSize()
	{
		return m_createCellTransaction.getNewGridSize();
	}
	
	public bhServerGrid getGrid()
	{
		return m_createCellTransaction.getGrid();
	}
}
