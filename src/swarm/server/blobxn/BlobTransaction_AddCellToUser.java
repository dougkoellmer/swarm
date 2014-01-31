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
import swarm.server.structs.ServerCodePrivileges;
import swarm.server.structs.ServerGridCoordinate;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.CodePrivileges;
import swarm.shared.structs.E_CellAddressParseError;
import swarm.shared.structs.GridCoordinate;

public class BlobTransaction_AddCellToUser extends A_BlobTransaction
{
private static final Logger s_logger = Logger.getLogger(BlobTransaction_AddCellToUser.class.getName());
	
	private final UserSession	m_session;
	
	private ServerUser m_user = null;
	
	private final BlobTransaction_CreateCell m_createCellTransaction;
	private boolean m_checkUsernameMatch = true;
	
	public BlobTransaction_AddCellToUser(UserSession session, ServerCellAddress[] cellAddresses, GridCoordinate preference, ServerCodePrivileges privileges, int gridExpansionDelta)
	{
		m_createCellTransaction = new BlobTransaction_CreateCell(cellAddresses, preference, privileges, gridExpansionDelta);
		
		m_session = session;
	}
	
	public BlobTransaction_AddCellToUser(UserSession session, ServerCellAddress[] cellAddresses, GridCoordinate preference, int gridExpansionDelta)
	{
		m_createCellTransaction = new BlobTransaction_CreateCell(cellAddresses, preference, null, gridExpansionDelta);
		
		m_session = session;
	}
	
	public void checkUsernameMatch(boolean value)
	{
		m_checkUsernameMatch = value;
	}

	@Override
	protected void performOperations() throws BlobException
	{
		m_user = null;
		
		//--- DRK > Sanity check.
		ServerCellAddress address = m_createCellTransaction.getAddresses()[0];
		if( m_checkUsernameMatch )
		{
			String usernamePart = address.getPart(CellAddress.E_Part.USERNAME);
			if( usernamePart == null || !m_session.getUsername().equals(usernamePart))
			{
				throw new BlobException("Username doesn't match username part of cell address..." + m_session.getUsername() + ", " + usernamePart);
			}
		}
		
		//--- DRK > Another sanity check.
		// NOTE: Allowing user part only for now.
		/*String cellPart = address.getPart(smCellAddress.E_Part.CELL);
		if( cellPart == null )
		{
			throw new smBlobException("Address doesn't have a cell part.");
		}*/
		
		if( address.getParseError() != E_CellAddressParseError.NO_ERROR )
		{
			throw new BlobException("Bad address: " + address.getRawAddressLeadSlash());
		}
		
		//--- DRK > Do a get that we'll use to perform some sanity checks.
		I_BlobManager blobManager = m_blobMngrFactory.create(E_BlobCacheLevel.PERSISTENT);
		HashMap<I_BlobKey, Class<? extends I_Blob>> existanceQuery = new HashMap<I_BlobKey, Class<? extends I_Blob>>();
		existanceQuery.put(m_session, ServerUser.class);
		existanceQuery.put(address, ServerCellAddressMapping.class);
		Map<I_BlobKey, I_Blob> result = blobManager.getBlobs(existanceQuery);
		
		//--- DRK > Make sure user exists.
		if( result == null )
		{
			throw new BlobException("User came up null.");
		}
		m_user = (ServerUser) result.get(m_session);
		if( m_user == null )
		{
			throw new BlobException("User came up null.");
		}
		
		//--- DRK > Make sure address isn't already registered.
		ServerCellAddressMapping mappingResult = (ServerCellAddressMapping) result.get(address);
		if( mappingResult != null )
		{
			throw new BlobException("Address already taken.");
		}
		
		performNested(m_createCellTransaction);
		
		//--- DRK > Add coordinate and pop user back into database.
		ServerCellAddressMapping mapping = m_createCellTransaction.getMapping();
		m_user.addOwnedCell(mapping);
		blobManager.putBlob(m_session, m_user);
	}
	
	public ServerUser getUser()
	{
		return m_user;
	}

	@Override
	protected void onSuccess()
	{
		m_createCellTransaction.onSuccess();
	}
}
