package swarm.server.handlers;

import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import java.util.logging.Logger;


import swarm.server.account.E_Role;
import swarm.server.account.UserSession;
import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.E_BlobTransactionType;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.data.blob.U_Blob;
import swarm.server.data.blob.A_BlobTransaction;

import swarm.server.entities.ServerCell;
import swarm.server.entities.BaseServerGrid;
import swarm.server.entities.ServerUser;
import swarm.server.session.SessionManager;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.structs.ServerCode;
import swarm.server.structs.ServerGridCoordinate;
import swarm.server.transaction.ServerTransactionManager;
import swarm.server.transaction.TransactionContext;
import swarm.shared.app.BaseAppContext;
import swarm.shared.app.S_CommonApp;
import swarm.shared.code.A_CodeCompiler;
import swarm.shared.code.CompilerResult;
import swarm.shared.code.E_CompilationStatus;
import swarm.shared.entities.A_Grid;
import swarm.shared.entities.E_CodeType;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.Code;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;


public final class U_CellCode
{
	private static final Logger s_logger = Logger.getLogger(U_CellCode.class.getName());
	
	private U_CellCode(){}
	
	public static boolean saveBackCompiledCell(I_BlobManager persistentBlobManager, I_BlobManager cachingBlobManager, ServerCellAddressMapping mapping, ServerCell cell, TransactionResponse response)
	{
		try
		{
			persistentBlobManager.putBlob(mapping, cell);
		}
		catch(ConcurrentModificationException e)
		{
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			s_logger.severe("Couldn't save back blob (1). " + e);
			
			return false;
		}
		catch(BlobException e)
		{
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			s_logger.severe("Couldn't save back blob (2). " + e);
			
			return false;
		}
		
		U_CellCode.removeFromCache(cachingBlobManager, mapping);
		
		return true;
	}
	
	public static ServerCell getCell(I_BlobManager blobManager, ServerCellAddressMapping mapping, TransactionResponse response)
	{
		ServerCell persistedCell = null;
		
		try
		{
			persistedCell = blobManager.getBlob(mapping, ServerCell.class);
		}
		catch(BlobException e)
		{
			s_logger.severe("Exception getting cell at mapping: " + mapping + " " + e);
			
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			
			return null;
		}
		
		return persistedCell;
	}

	public static ServerCell getCellForCompile(I_BlobManager blobManager, ServerCellAddressMapping mapping, TransactionResponse response)
	{
		ServerCell persistedCell = getCell(blobManager, mapping, response);
		
		if( persistedCell == null )
		{
			s_logger.severe("Expected cell to already exist for user at mapping: " + mapping);
			
			response.setError(E_ResponseError.BAD_STATE);
			
			return null;
		}
		
		return persistedCell;
	}
	
	public static CompilerResult compileCell(A_CodeCompiler compiler, ServerCell cell_out, Code sourceCode, CellAddressMapping mapping, String apiNamespace)
	{
		cell_out.setCode(E_CodeType.SOURCE, sourceCode); // DRK > may be redundant.
		
		CompilerResult result = compiler.compile(sourceCode, cell_out.getCodePrivileges(), mapping.writeString(), apiNamespace);
		
		if( result.getStatus() == E_CompilationStatus.NO_ERROR )
		{
			ServerCode splash = (ServerCode) result.getCode(E_CodeType.SPLASH);
			cell_out.setCode(E_CodeType.SPLASH, splash);
			
			//--- DRK > Avoid extra data write if splash can stand in for compiled.
			if( splash.isStandInFor(E_CodeType.COMPILED) )
			{
				cell_out.setCode(E_CodeType.COMPILED, null);
			}
			else
			{
				ServerCode compiled = (ServerCode) result.getCode(E_CodeType.COMPILED);
				cell_out.setCode(E_CodeType.COMPILED, compiled);
			}
		}
		
		return result;
	}

	public static void removeFromCache(I_BlobManager cacheBlobManager, ServerCellAddressMapping mapping)
	{
		//--- DRK > Here we attempt to delete this cell from memcache so that subsequent requests for this cell get a fresh copy.
		//---		Note that we could put a fresh copy in memcache, but we don't know how popular this cell is, and memcache
		//---		space is potentially very limited. Therefore we let user demand determine when/if this cell gets cached again.
		try
		{
			cacheBlobManager.deleteBlobAsync(mapping, ServerCell.class);
		}
		catch(BlobException e)
		{
			s_logger.log(Level.WARNING, "Could not delete freshly synced cell from memcache.", e);
		}
	}
}
