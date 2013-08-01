package com.b33hive.server.handlers;

import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.b33hive.server.account.bhE_Role;
import com.b33hive.server.account.bhUserSession;
import com.b33hive.server.app.bhS_ServerApp;
import com.b33hive.server.data.blob.bhBlobException;
import com.b33hive.server.data.blob.bhBlobManagerFactory;
import com.b33hive.server.data.blob.bhE_BlobCacheLevel;
import com.b33hive.server.data.blob.bhE_BlobTransactionType;
import com.b33hive.server.data.blob.bhI_BlobManager;
import com.b33hive.server.data.blob.bhU_Blob;
import com.b33hive.server.data.blob.bhA_BlobTransaction;
import com.b33hive.server.entities.bhS_BlobKeyPrefix;
import com.b33hive.server.entities.bhServerCell;
import com.b33hive.server.entities.bhServerGrid;
import com.b33hive.server.entities.bhServerUser;
import com.b33hive.server.session.bhSessionManager;
import com.b33hive.server.structs.bhServerCellAddress;
import com.b33hive.server.structs.bhServerCellAddressMapping;
import com.b33hive.server.structs.bhServerCode;
import com.b33hive.server.structs.bhServerGridCoordinate;
import com.b33hive.server.transaction.bhServerTransactionManager;
import com.b33hive.server.transaction.bhTransactionContext;
import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.code.bhA_CodeCompiler;
import com.b33hive.shared.code.bhCompilerResult;
import com.b33hive.shared.code.bhE_CompilationStatus;
import com.b33hive.shared.entities.bhA_Grid;
import com.b33hive.shared.entities.bhE_CodeType;
import com.b33hive.shared.structs.bhCellAddress;
import com.b33hive.shared.structs.bhCellAddressMapping;
import com.b33hive.shared.structs.bhCode;
import com.b33hive.shared.structs.bhGridCoordinate;
import com.b33hive.shared.transaction.bhE_RequestPath;
import com.b33hive.shared.transaction.bhE_ResponseError;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

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

	public static bhServerCell getCellForCompile(bhI_BlobManager blobManager, bhServerCellAddressMapping mapping, bhTransactionResponse response)
	{
		bhServerCell persistedCell = null;
		
		try
		{
			persistedCell = blobManager.getBlob(mapping, bhServerCell.class);
			
			if( persistedCell == null )
			{
				s_logger.severe("Expected cell to already exist for user at mapping: " + mapping);
				
				response.setError(bhE_ResponseError.BAD_STATE);
				
				return null;
			}
		}
		catch(bhBlobException e)
		{
			s_logger.severe("Exception getting cell for compile at mapping: " + mapping + " " + e);
			
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			return null;
		}
		
		return persistedCell;
	}
	
	public static bhCompilerResult compileCell(bhServerCell cell, bhCode sourceCode, bhCellAddressMapping mapping)
	{
		cell.setCode(bhE_CodeType.SOURCE, sourceCode); // DRK > may be redundant.
		
		bhA_CodeCompiler compiler = bhA_CodeCompiler.getInstance();
		
		bhCompilerResult result = compiler.compile(sourceCode, cell.getCodePrivileges(), mapping.writeString());
		
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
		//---		space is potentially very limited. Therefore we let user demand determine when/if this cell gets cached.
		bhI_BlobManager blobManager = bhBlobManagerFactory.getInstance().create(bhE_BlobCacheLevel.MEMCACHE);
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
