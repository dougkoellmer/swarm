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
import com.b33hive.server.structs.bhServerCodePrivileges;
import com.b33hive.server.structs.bhServerGridCoordinate;
import com.b33hive.shared.structs.bhCellAddress;
import com.b33hive.shared.structs.bhCellAddressMapping;
import com.b33hive.shared.structs.bhCodePrivileges;
import com.b33hive.shared.structs.bhE_CellAddressParseError;
import com.b33hive.shared.structs.bhGridCoordinate;

public class bhBlobTransaction_AddCellToUser extends bhA_BlobTransaction
{
private static final Logger s_logger = Logger.getLogger(bhBlobTransaction_AddCellToUser.class.getName());
	
	private final bhUserSession	m_session;
	
	private bhServerUser m_user = null;
	
	private final bhBlobTransaction_CreateCell m_createCellTransaction;
	
	public bhBlobTransaction_AddCellToUser(bhUserSession session, bhServerCellAddress cellAddress, bhGridCoordinate preference, bhServerCodePrivileges privileges)
	{
		m_createCellTransaction = new bhBlobTransaction_CreateCell(cellAddress, preference, privileges);
		
		m_session = session;
	}
	
	public bhBlobTransaction_AddCellToUser(bhUserSession session, bhServerCellAddress cellAddress, bhGridCoordinate preference)
	{
		m_createCellTransaction = new bhBlobTransaction_CreateCell(cellAddress, preference, null);
		
		m_session = session;
	}
	
	bhBlobTransaction_AddCellToUser(bhUserSession session, bhServerCellAddress cellAddress)
	{
		m_createCellTransaction = new bhBlobTransaction_CreateCell(cellAddress, null, null);
		
		m_session = session;
	}

	@Override
	protected void performOperations() throws bhBlobException
	{
		m_user = null;
		
		//--- DRK > Sanity check.
		bhServerCellAddress address = m_createCellTransaction.getAddress();
		String usernamePart = address.getPart(bhCellAddress.E_Part.USERNAME);
		if( usernamePart == null || !m_session.getUsername().equals(usernamePart))
		{
			throw new bhBlobException("Username doesn't match username part of cell address..." + m_session.getUsername() + ", " + usernamePart);
		}
		
		//--- DRK > Another sanity check.
		// NOTE: Allowing user part only for now.
		/*String cellPart = address.getPart(bhCellAddress.E_Part.CELL);
		if( cellPart == null )
		{
			throw new bhBlobException("Address doesn't have a cell part.");
		}*/
		
		if( address.getParseError() != bhE_CellAddressParseError.NO_ERROR )
		{
			throw new bhBlobException("Bad address: " + address.getRawAddress());
		}
		
		//--- DRK > Do a get that we'll use to perform some sanity checks.
		bhI_BlobManager blobManager = bhBlobManagerFactory.getInstance().create(bhE_BlobCacheLevel.PERSISTENT);
		HashMap<bhI_BlobKeySource, Class<? extends bhI_Blob>> existanceQuery = new HashMap<bhI_BlobKeySource, Class<? extends bhI_Blob>>();
		existanceQuery.put(m_session, bhServerUser.class);
		existanceQuery.put(address, bhServerCellAddressMapping.class);
		Map<bhI_BlobKeySource, bhI_Blob> result = blobManager.getBlobs(existanceQuery);
		
		//--- DRK > Make sure user exists.
		if( result == null )
		{
			throw new bhBlobException("User came up null.");
		}
		m_user = (bhServerUser) result.get(m_session);
		if( m_user == null )
		{
			throw new bhBlobException("User came up null.");
		}
		
		//--- DRK > Make sure address isn't already registered.
		bhServerCellAddressMapping mappingResult = (bhServerCellAddressMapping) result.get(address);
		if( mappingResult != null )
		{
			throw new bhBlobException("Address already taken.");
		}
		
		m_createCellTransaction.performOperations();
		
		//--- DRK > Add coordinate and pop user back into database.
		bhServerCellAddressMapping mapping = m_createCellTransaction.getMapping();
		m_user.addOwnedCell(mapping);
		blobManager.putBlob(m_session, m_user);
	}
	
	public bhServerUser getUser()
	{
		return m_user;
	}

	@Override
	protected void onSuccess()
	{
		m_createCellTransaction.onSuccess();
	}
}
