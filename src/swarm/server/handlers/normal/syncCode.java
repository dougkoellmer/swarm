package swarm.server.handlers.normal;

import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.smE_Role;
import swarm.server.account.smUserSession;

import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smE_GridType;
import swarm.server.entities.smServerCell;
import swarm.server.entities.smServerUser;
import swarm.server.handlers.smU_CellCode;
import swarm.server.session.smSessionManager;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.structs.smServerCode;
import swarm.server.structs.smServerGridCoordinate;
import swarm.server.transaction.smA_DefaultRequestHandler;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.app.smS_App;
import swarm.shared.code.smA_CodeCompiler;
import swarm.shared.code.smCompilerResult;
import swarm.shared.code.smE_CompilationStatus;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.entities.smE_EditingPermission;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class syncCode extends smA_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(syncCode.class.getName());
	
	protected boolean isSandBox(smServerCellAddressMapping mapping)
	{
		return false;
	}
	
	private boolean isAuthorized(smI_BlobManager blobManager, smServerCellAddressMapping mapping, smTransactionRequest request, smTransactionResponse response)
	{
		if( !m_serverContext.sessionMngr.isAuthorized(request, response, smE_Role.USER) )
		{
			return false;
		}

		//--- DRK > Have to do a further checking here to make sure user owns this cell.
		smUserSession session = m_serverContext.sessionMngr.getSession(request, response);
		
		//--- DRK > Used to have this check here...don't see why we shouldn't also
		//---		force admins to own their own cells too.
		//if( session.getRole().ordinal() == smE_Role.USER.ordinal() )
		{
			smServerUser user = null;
			
			try
			{
				user = blobManager.getBlob(session, smServerUser.class);
			}
			catch(smBlobException e)
			{
				response.setError(smE_ResponseError.SERVICE_EXCEPTION);
				
				return false;
			}
			
			//TODO: When flag system is in place, will have to check if this cell's been flagged or not, and check if user has editing permissions for that.
			boolean isCellOwner = user.isCellOwner(mapping.getCoordinate());
			boolean isAllPowerful = user.getEditingPermission() == smE_EditingPermission.ALL_CELLS;
			
			if( user == null || user != null && !isCellOwner && !isAllPowerful )
			{
				response.setError(smE_ResponseError.NOT_AUTHORIZED);
				
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		smServerCellAddressMapping mapping = new smServerCellAddressMapping(smE_GridType.ACTIVE);
		mapping.readJson(m_serverContext.jsonFactory, request.getJsonArgs());
		boolean isSandbox = isSandBox(mapping);
		smI_BlobManager blobManager = m_serverContext.blobMngrFactory.create(smE_BlobCacheLevel.PERSISTENT);
		smI_BlobManager cachingBlobManager = m_serverContext.blobMngrFactory.create(smE_BlobCacheLevel.MEMCACHE);
		
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
		
		smServerCell persistedCell = smU_CellCode.getCellForCompile(blobManager, mapping, response);
		
		if( persistedCell == null )  return;
		
		smServerCode sourceCode = new smServerCode(m_serverContext.jsonFactory, request.getJsonArgs(), smE_CodeType.SOURCE);
		
		smCompilerResult result = smU_CellCode.compileCell(m_serverContext.codeCompiler, persistedCell, sourceCode, mapping, m_serverContext.config.appId);
		
		//--- DRK > This write could obviously cause contention if user was saving from multiple clients,
		//---		but if a user wants to do that for whatever reason, it's their own problem.
		//---
		//---		TODO: The above comment will be invalid if the flagging system stores anything in the blob...not sure if it will.
		//---			  But if it does, valid contention cases could definitely happen.
		if( isSandbox == false)
		{
			smU_CellCode.saveBackCompiledCell(blobManager, cachingBlobManager, mapping, persistedCell, response);
		}
		
		result.writeJson(m_serverContext.jsonFactory, response.getJsonArgs());
	}
}
