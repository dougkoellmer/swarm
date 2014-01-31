package swarm.server.handlers.normal;

import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.E_Role;
import swarm.server.account.UserSession;

import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.E_GridType;
import swarm.server.entities.ServerCell;
import swarm.server.entities.ServerUser;
import swarm.server.handlers.U_CellCode;
import swarm.server.session.SessionManager;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.structs.ServerCode;
import swarm.server.structs.ServerGridCoordinate;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.app.S_CommonApp;
import swarm.shared.code.A_CodeCompiler;
import swarm.shared.code.CompilerResult;
import swarm.shared.code.E_CompilationStatus;
import swarm.shared.entities.E_CodeType;
import swarm.shared.entities.E_EditingPermission;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class syncCode extends A_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(syncCode.class.getName());
	
	protected boolean isSandBox(ServerCellAddressMapping mapping)
	{
		return false;
	}
	
	private boolean isAuthorized(I_BlobManager blobManager, ServerCellAddressMapping mapping, TransactionRequest request, TransactionResponse response)
	{
		if( !m_serverContext.sessionMngr.isAuthorized(request, response, E_Role.USER) )
		{
			return false;
		}

		//--- DRK > Have to do a further checking here to make sure user owns this cell.
		UserSession session = m_serverContext.sessionMngr.getSession(request, response);
		
		//--- DRK > Used to have this check here...don't see why we shouldn't also
		//---		force admins to own their own cells too.
		//if( session.getRole().ordinal() == smE_Role.USER.ordinal() )
		{
			ServerUser user = null;
			
			try
			{
				user = blobManager.getBlob(session, ServerUser.class);
			}
			catch(BlobException e)
			{
				response.setError(E_ResponseError.SERVICE_EXCEPTION);
				
				return false;
			}
			
			//TODO: When flag system is in place, will have to check if this cell's been flagged or not, and check if user has editing permissions for that.
			boolean isCellOwner = user.isCellOwner(mapping.getCoordinate());
			boolean isAllPowerful = user.getEditingPermission() == E_EditingPermission.ALL_CELLS;
			
			if( user == null || user != null && !isCellOwner && !isAllPowerful )
			{
				response.setError(E_ResponseError.NOT_AUTHORIZED);
				
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		ServerCellAddressMapping mapping = new ServerCellAddressMapping(E_GridType.ACTIVE);
		mapping.readJson(m_serverContext.jsonFactory, request.getJsonArgs());
		boolean isSandbox = isSandBox(mapping);
		I_BlobManager blobManager = m_serverContext.blobMngrFactory.create(E_BlobCacheLevel.PERSISTENT);
		I_BlobManager cachingBlobManager = m_serverContext.blobMngrFactory.create(E_BlobCacheLevel.MEMCACHE);
		
		if( !isSandbox )
		{
			if( !isAuthorized(blobManager, mapping, request, response) )
			{
				//--- DRK > Of course, could be someone just spoofing a request...should hopefully never come from
				//---		the actual client though.
				s_logger.warning("Attempted to write to a cell without authorization.");
				
				return;
			}
		}
		
		ServerCell persistedCell = U_CellCode.getCellForCompile(blobManager, mapping, response);
		
		if( persistedCell == null )  return;
		
		ServerCode sourceCode = new ServerCode(m_serverContext.jsonFactory, request.getJsonArgs(), E_CodeType.SOURCE);
		
		CompilerResult result = U_CellCode.compileCell(m_serverContext.codeCompiler, persistedCell, sourceCode, mapping, m_serverContext.config.appId);
		
		//--- DRK > This write could obviously cause contention if user was saving from multiple clients,
		//---		but if a user wants to do that for whatever reason, it's their own problem.
		//---
		//---		TODO: The above comment will be invalid if the flagging system stores anything in the blob...not sure if it will.
		//---			  But if it does, valid contention cases could definitely happen.
		if( isSandbox == false)
		{
			U_CellCode.saveBackCompiledCell(blobManager, cachingBlobManager, mapping, persistedCell, response);
		}
		
		result.writeJson(m_serverContext.jsonFactory, response.getJsonArgs());
	}
}
