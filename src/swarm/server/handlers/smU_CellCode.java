package swarm.server.handlers;

import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import java.util.logging.Logger;


import swarm.server.account.smE_Role;
import swarm.server.account.smUserSession;
import swarm.server.account.sm_s;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smE_BlobTransactionType;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.data.blob.smU_Blob;
import swarm.server.data.blob.smA_BlobTransaction;

import swarm.server.entities.smServerCell;
import swarm.server.entities.smServerGrid;
import swarm.server.entities.smServerUser;
import swarm.server.session.smSessionManager;
import swarm.server.structs.smServerCellAddress;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.structs.smServerCode;
import swarm.server.structs.smServerGridCoordinate;
import swarm.server.transaction.smServerTransactionManager;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.app.smS_App;
import swarm.shared.code.smA_CodeCompiler;
import swarm.shared.code.smCompilerResult;
import swarm.shared.code.smE_CompilationStatus;
import swarm.shared.entities.smA_Grid;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;


public final class smU_CellCode
{
	private static final Logger s_logger = Logger.getLogger(smU_CellCode.class.getName());
	
	private smU_CellCode()
	{
	}
	
	public static boolean saveBackCompiledCell(smI_BlobManager blobManager, smServerCellAddressMapping mapping, smServerCell cell, smTransactionResponse response)
	{
		try
		{
			blobManager.putBlob(mapping, cell);
		}
		catch(ConcurrentModificationException e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			s_logger.severe("Couldn't save back blob (1). " + e);
			
			return false;
		}
		catch(smBlobException e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			s_logger.severe("Couldn't save back blob (2). " + e);
			
			return false;
		}
		
		smU_CellCode.removeFromCache(mapping);
		
		return true;
	}
	
	public static smServerCell getCell(smI_BlobManager blobManager, smServerCellAddressMapping mapping, smTransactionResponse response)
	{
		smServerCell persistedCell = null;
		
		try
		{
			persistedCell = blobManager.getBlob(mapping, smServerCell.class);
		}
		catch(smBlobException e)
		{
			s_logger.severe("Exception getting cell at mapping: " + mapping + " " + e);
			
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			
			return null;
		}
		
		return persistedCell;
	}

	public static smServerCell getCellForCompile(smI_BlobManager blobManager, smServerCellAddressMapping mapping, smTransactionResponse response)
	{
		smServerCell persistedCell = getCell(blobManager, mapping, response);
		
		if( persistedCell == null )
		{
			s_logger.severe("Expected cell to already exist for user at mapping: " + mapping);
			
			response.setError(smE_ResponseError.BAD_STATE);
			
			return null;
		}
		
		return persistedCell;
	}
	
	public static smCompilerResult compileCell(smA_CodeCompiler compiler, smServerCell cell, smCode sourceCode, smCellAddressMapping mapping)
	{
		cell.setCode(smE_CodeType.SOURCE, sourceCode); // DRK > may be redundant.
		
		smCompilerResult result = compiler.compile(sourceCode, cell.getCodePrivileges(), mapping.writeString());
		
		if( result.getStatus() == smE_CompilationStatus.NO_ERROR )
		{
			smServerCode splash = (smServerCode) result.getCode(smE_CodeType.SPLASH);
			cell.setCode(smE_CodeType.SPLASH, splash);
			
			//--- DRK > Avoid extra data write if splash can stand in for compiled.
			if( splash.isStandInFor(smE_CodeType.COMPILED) )
			{
				cell.setCode(smE_CodeType.COMPILED, null);
			}
			else
			{
				smServerCode compiled = (smServerCode) result.getCode(smE_CodeType.COMPILED);
				cell.setCode(smE_CodeType.COMPILED, compiled);
			}
		}
		
		return result;
	}

	public static void removeFromCache(smServerCellAddressMapping mapping)
	{
		//--- DRK > Here we attempt to delete this cell from memcache so that subsequent requests for this cell get a fresh copy.
		//---		Note that we could put a fresh copy in memcache, but we don't know how popular this cell is, and memcache
		//---		space is potentially very limited. Therefore we let user demand determine when/if this cell gets cached again.
		smI_BlobManager blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.MEMCACHE);
		try
		{
			blobManager.deleteBlobAsync(mapping, smServerCell.class);
		}
		catch(smBlobException e)
		{
			s_logger.log(Level.WARNING, "Could not delete freshly synced cell from memcache.", e);
		}
	}
}
