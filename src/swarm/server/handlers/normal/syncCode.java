package swarm.server.handlers.normal;

import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.bhE_Role;
import swarm.server.account.bhUserSession;
import swarm.server.account.sm_s;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.entities.bhE_GridType;
import swarm.server.entities.bhServerCell;
import swarm.server.entities.bhServerUser;
import swarm.server.handlers.bhU_CellCode;
import swarm.server.session.bhSessionManager;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.structs.bhServerCode;
import swarm.server.structs.bhServerGridCoordinate;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.app.bhS_App;
import swarm.shared.code.bhA_CodeCompiler;
import swarm.shared.code.bhCompilerResult;
import swarm.shared.code.bhE_CompilationStatus;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.entities.bhE_EditingPermission;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class syncCode implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(syncCode.class.getName());
	
	protected boolean isSandBox(bhServerCellAddressMapping mapping)
	{
		return false;
	}
	
	private boolean isAuthorized(bhI_BlobManager blobManager, bhServerCellAddressMapping mapping, bhTransactionRequest request, bhTransactionResponse response)
	{
		if( !sm_s.sessionMngr.isAuthorized(request, response, bhE_Role.USER) )
		{
			return false;
		}

		//--- DRK > Have to do a further checking here to make sure user owns this cell.
		bhUserSession session = sm_s.sessionMngr.getSession(request, response);
		
		//--- DRK > Used to have this check here...don't see why we shouldn't also
		//---		force admins to own their own cells too.
		//if( session.getRole().ordinal() == bhE_Role.USER.ordinal() )
		{
			bhServerUser user = null;
			
			try
			{
				user = blobManager.getBlob(session, bhServerUser.class);
			}
			catch(bhBlobException e)
			{
				response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
				
				return false;
			}
			
			//TODO: When flag system is in place, will have to check if this cell's been flagged or not, and check if user has editing permissions for that.
			boolean isCellOwner = user.isCellOwner(mapping.getCoordinate());
			boolean isAllPowerful = user.getEditingPermission() == bhE_EditingPermission.ALL_CELLS;
			
			if( user == null || user != null && !isCellOwner && !isAllPowerful )
			{
				response.setError(bhE_ResponseError.NOT_AUTHORIZED);
				
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhServerCellAddressMapping mapping = new bhServerCellAddressMapping(bhE_GridType.ACTIVE);
		mapping.readJson(request.getJson());
		boolean isSandbox = isSandBox(mapping);
		bhI_BlobManager blobManager = sm_s.blobMngrFactory.create(bhE_BlobCacheLevel.PERSISTENT);
		
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
		
		bhServerCell persistedCell = bhU_CellCode.getCellForCompile(blobManager, mapping, response);
		
		if( persistedCell == null )  return;
		
		bhServerCode sourceCode = new bhServerCode(request.getJson(), bhE_CodeType.SOURCE);
		
		bhCompilerResult result = bhU_CellCode.compileCell(persistedCell, sourceCode, mapping);
		
		//--- DRK > This write could obviously cause contention if user was saving from multiple clients,
		//---		but if a user wants to do that for whatever reason, it's their own problem.
		//---
		//---		TODO: The above comment will be invalid if the flagging system stores anything in the blob...not sure if it will.
		//---			  But if it does, valid contention cases could definitely happen.
		if( isSandbox == false)
		{
			bhU_CellCode.saveBackCompiledCell(blobManager, mapping, persistedCell, response);
		}
		
		result.writeJson(response.getJson());
	}
}
