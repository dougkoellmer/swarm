package swarm.server.handlers.admin;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;


import swarm.server.account.E_Role;
import swarm.server.account.UserSession;

import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.E_BlobTransactionType;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.E_GridType;
import swarm.server.entities.ServerCell;
import swarm.server.entities.BaseServerGrid;
import swarm.server.entities.ServerUser;
import swarm.server.handlers.U_CellCode;
import swarm.server.session.SessionManager;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.structs.ServerCode;
import swarm.server.structs.ServerCodePrivileges;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.ServerTransactionManager;
import swarm.server.transaction.TransactionContext;
import swarm.shared.code.A_CodeCompiler;
import swarm.shared.code.CompilerResult;
import swarm.shared.code.E_CompilationStatus;
import swarm.shared.entities.A_User;
import swarm.shared.entities.E_CodeType;
import swarm.shared.structs.Code;
import swarm.shared.structs.E_NetworkPrivilege;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class recompileCells extends A_DefaultRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(recompileCells.class.getName());
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		I_BlobManager blobManager = m_serverContext.blobMngrFactory.create(E_BlobCacheLevel.PERSISTENT);
		I_BlobManager cachedBlobManager = m_serverContext.blobMngrFactory.create(E_BlobCacheLevel.MEMCACHE);
		
		BaseServerGrid grid = null;
		
		try
		{
			grid = blobManager.getBlob(E_GridType.ACTIVE, BaseServerGrid.class);
		}
		catch( BlobException e)
		{
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.severe("Could not retrieve grid data due to exception: " + e);
			
			return;
		}
		
		if( grid == null )
		{
			response.setError(E_ResponseError.BAD_STATE);
			
			s_logger.severe("Grid came up null when it probably should have been initialized.");
			
			return;
		}
		
		ServerCellAddressMapping mapping = new ServerCellAddressMapping(E_GridType.ACTIVE);
		
		for( int i = 0; i < grid.getHeight(); i++ )
		{
			for( int j = 0; j < grid.getWidth(); j++ )
			{
				mapping.getCoordinate().set(j,  i);
				if( grid.isTaken(mapping.getCoordinate()) )
				{
					ServerCell cell = U_CellCode.getCellForCompile(blobManager, mapping, response);
					
					if( cell == null )
					{
						s_logger.severe("Grid was marked as cell taken, but cell came up null at mapping: " + mapping);
					}

					if( !recompile(blobManager, cachedBlobManager, cell, mapping, response) )
					{
						break;
					}
					
					U_CellCode.removeFromCache(cachedBlobManager, mapping);
				}
			}
		}
	}
	
	private boolean recompile(I_BlobManager blobMngr, I_BlobManager cachedBlobMngr, ServerCell persistedCell, ServerCellAddressMapping mapping, TransactionResponse response)
	{
		Code sourceCode = persistedCell.getCode(E_CodeType.SOURCE);
		
		if( sourceCode == null )
		{
			return true;
		}
		
		CompilerResult result = U_CellCode.compileCell(m_serverContext.codeCompiler, persistedCell, sourceCode, mapping, m_serverContext.config.appId);
		
		if( result.getStatus() != E_CompilationStatus.NO_ERROR )
		{
			Code emptySplashCode = new Code("", E_CodeType.SPLASH, E_CodeType.COMPILED);
			persistedCell.setCode(E_CodeType.SPLASH, emptySplashCode);
			persistedCell.setCode(E_CodeType.COMPILED, null);
			
			s_logger.severe("Source code now has an error in it...presumably it did not before.");
		}
		
		return U_CellCode.saveBackCompiledCell(blobMngr, cachedBlobMngr, mapping, persistedCell, response);
	}
}
