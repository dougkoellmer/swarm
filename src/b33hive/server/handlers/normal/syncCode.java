package b33hive.server.handlers.normal;

import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import b33hive.server.account.bhE_Role;
import b33hive.server.account.bhUserSession;
import b33hive.server.account.bh_s;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.entities.bhE_GridType;
import b33hive.server.entities.bhServerCell;
import b33hive.server.entities.bhServerUser;
import b33hive.server.handlers.bhU_CellCode;
import b33hive.server.session.bhSessionManager;
import b33hive.server.structs.bhServerCellAddressMapping;
import b33hive.server.structs.bhServerCode;
import b33hive.server.structs.bhServerGridCoordinate;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.app.bhS_App;
import b33hive.shared.code.bhA_CodeCompiler;
import b33hive.shared.code.bhCompilerResult;
import b33hive.shared.code.bhE_CompilationStatus;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.entities.bhE_EditingPermission;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class syncCode implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(syncCode.class.getName());
	
	private boolean isSandBox(bhServerCellAddressMapping mapping)
	{
		return mapping.getCoordinate().getM() == bhS_App.SANDBOX_COORD_M && mapping.getCoordinate().getN() == bhS_App.SANDBOX_COORD_N;
	}
	
	private boolean isAuthorized(bhI_BlobManager blobManager, bhServerCellAddressMapping mapping, bhTransactionRequest request, bhTransactionResponse response)
	{
		if( !bh_s.sessionMngr.isAuthorized(request, response, bhE_Role.USER) )
		{
			return false;
		}

		//--- DRK > Have to do a further checking here to make sure user owns this cell.
		bhUserSession session = bh_s.sessionMngr.getSession(request, response);
		
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
		bhI_BlobManager blobManager = bh_s.blobMngrFactory.create(bhE_BlobCacheLevel.PERSISTENT);
		
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
