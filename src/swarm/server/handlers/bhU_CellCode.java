package swarm.server.handlers;

import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import java.util.logging.Logger;


import swarm.server.account.bhE_Role;
import swarm.server.account.bhUserSession;
import swarm.server.account.sm_s;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhE_BlobTransactionType;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.data.blob.bhU_Blob;
import swarm.server.data.blob.bhA_BlobTransaction;

import swarm.server.entities.bhServerCell;
import swarm.server.entities.bhServerGrid;
import swarm.server.entities.bhServerUser;
import swarm.server.session.bhSessionManager;
import swarm.server.structs.bhServerCellAddress;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.structs.bhServerCode;
import swarm.server.structs.bhServerGridCoordinate;
import swarm.server.transaction.bhServerTransactionManager;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.app.sm;
import swarm.shared.app.bhS_App;
import swarm.shared.code.bhA_CodeCompiler;
import swarm.shared.code.bhCompilerResult;
import swarm.shared.code.bhE_CompilationStatus;
import swarm.shared.entities.bhA_Grid;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.structs.bhCellAddress;
import swarm.shared.structs.bhCellAddressMapping;
import swarm.shared.structs.bhCode;
import swarm.shared.structs.bhGridCoordinate;
import swarm.shared.transaction.bhE_RequestPath;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;


public final class bhU_CellCode
{
	private static final Logger s_logger = Logger.getLogger(bhU_CellCode.class.getName());
	
	private bhU_CellCode()
	{
	}
	
	public static boolean saveBackCompiledCell(bhI_BlobManager blobManager, bhServerCellAddressMapping mapping, bhServerCell cell, bhTransactionResponse response)
	{
		try
		{
			blobManager.putBlob(mapping, cell);
		}
		catch(ConcurrentModificationException e)
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			s_logger.severe("Couldn't save back blob (1). " + e);
			
			return false;
		}
		catch(bhBlobException e)
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			s_logger.severe("Couldn't save back blob (2). " + e);
			
			return false;
		}
		
		bhU_CellCode.removeFromCache(mapping);
		
		return true;
	}
	
	public static bhServerCell getCell(bhI_BlobManager blobManager, bhServerCellAddressMapping mapping, bhTransactionResponse response)
	{
		bhServerCell persistedCell = null;
		
		try
		{
			persistedCell = blobManager.getBlob(mapping, bhServerCell.class);
		}
		catch(bhBlobException e)
		{
			s_logger.severe("Exception getting cell at mapping: " + mapping + " " + e);
			
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			return null;
		}
		
		return persistedCell;
	}

	public static bhServerCell getCellForCompile(bhI_BlobManager blobManager, bhServerCellAddressMapping mapping, bhTransactionResponse response)
	{
		bhServerCell persistedCell = getCell(blobManager, mapping, response);
		
		if( persistedCell == null )
		{
			s_logger.severe("Expected cell to already exist for user at mapping: " + mapping);
			
			response.setError(bhE_ResponseError.BAD_STATE);
			
			return null;
		}
		
		return persistedCell;
	}
	
	public static bhCompilerResult compileCell(bhServerCell cell, bhCode sourceCode, bhCellAddressMapping mapping)
	{
		cell.setCode(bhE_CodeType.SOURCE, sourceCode); // DRK > may be redundant.
		
		bhCompilerResult result = sm.codeCompiler.compile(sourceCode, cell.getCodePrivileges(), mapping.writeString());
		
		if( result.getStatus() == bhE_CompilationStatus.NO_ERROR )
		{
			bhServerCode splash = (bhServerCode) result.getCode(bhE_CodeType.SPLASH);
			cell.setCode(bhE_CodeType.SPLASH, splash);
			
			//--- DRK > Avoid extra data write if splash can stand in for compiled.
			if( splash.isStandInFor(bhE_CodeType.COMPILED) )
			{
				cell.setCode(bhE_CodeType.COMPILED, null);
			}
			else
			{
				bhServerCode compiled = (bhServerCode) result.getCode(bhE_CodeType.COMPILED);
				cell.setCode(bhE_CodeType.COMPILED, compiled);
			}
		}
		
		return result;
	}

	public static void removeFromCache(bhServerCellAddressMapping mapping)
	{
		//--- DRK > Here we attempt to delete this cell from memcache so that subsequent requests for this cell get a fresh copy.
		//---		Note that we could put a fresh copy in memcache, but we don't know how popular this cell is, and memcache
		//---		space is potentially very limited. Therefore we let user demand determine when/if this cell gets cached again.
		bhI_BlobManager blobManager = sm_s.blobMngrFactory.create(bhE_BlobCacheLevel.MEMCACHE);
		try
		{
			blobManager.deleteBlobAsync(mapping, bhServerCell.class);
		}
		catch(bhBlobException e)
		{
			s_logger.log(Level.WARNING, "Could not delete freshly synced cell from memcache.", e);
		}
	}
}
