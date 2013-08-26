package swarm.server.handlers.admin;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;


import swarm.server.account.smE_Role;
import swarm.server.account.smUserSession;
import swarm.server.account.sm_s;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smE_BlobTransactionType;
import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smI_BlobKey;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smE_GridType;
import swarm.server.entities.smServerCell;
import swarm.server.entities.smServerGrid;
import swarm.server.entities.smServerUser;
import swarm.server.handlers.smU_CellCode;
import swarm.server.session.smSessionManager;
import swarm.server.structs.smServerCellAddress;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.structs.smServerCode;
import swarm.server.structs.smServerCodePrivileges;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smServerTransactionManager;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.code.smA_CodeCompiler;
import swarm.shared.code.smCompilerResult;
import swarm.shared.code.smE_CompilationStatus;
import swarm.shared.entities.smA_User;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smE_NetworkPrivilege;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class recompileCells implements smI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(recompileCells.class.getName());
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		smI_BlobManager blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.PERSISTENT);
		
		smServerGrid grid = null;
		
		try
		{
			grid = blobManager.getBlob(smE_GridType.ACTIVE, smServerGrid.class);
		}
		catch( bhBlobException e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.severe("Could not retrieve grid data due to exception: " + e);
			
			return;
		}
		
		if( grid == null )
		{
			response.setError(smE_ResponseError.BAD_STATE);
			
			s_logger.severe("Grid came up null when it probably should have been initialized.");
			
			return;
		}
		
		smServerCellAddressMapping mapping = new smServerCellAddressMapping(smE_GridType.ACTIVE);
		
		for( int i = 0; i < grid.getHeight(); i++ )
		{
			for( int j = 0; j < grid.getWidth(); j++ )
			{
				mapping.getCoordinate().set(j,  i);
				if( grid.isTaken(mapping.getCoordinate()) )
				{
					smServerCell cell = bhU_CellCode.getCellForCompile(blobManager, mapping, response);
					
					if( cell == null )
					{
						s_logger.severe("Grid was marked as cell taken, but cell came up null at mapping: " + mapping);
					}

					if( !recompile(blobManager, cell, mapping, response) )
					{
						break;
					}
					
					bhU_CellCode.removeFromCache(mapping);
				}
			}
		}
	}
	
	private boolean recompile(smI_BlobManager blobManager, smServerCell persistedCell, smServerCellAddressMapping mapping, smTransactionResponse response)
	{
		bhCode sourceCode = persistedCell.getCode(smE_CodeType.SOURCE);
		
		if( sourceCode == null )
		{
			return true;
		}
		
		bhCompilerResult result = bhU_CellCode.compileCell(persistedCell, sourceCode, mapping);
		
		if( result.getStatus() != smE_CompilationStatus.NO_ERROR )
		{
			bhCode emptySplashCode = new smCode("", smE_CodeType.SPLASH, smE_CodeType.COMPILED);
			persistedCell.setCode(smE_CodeType.SPLASH, emptySplashCode);
			persistedCell.setCode(smE_CodeType.COMPILED, null);
			
			s_logger.severe("Source code now has an error in it...presumably it did not before.");
		}
		
		return bhU_CellCode.saveBackCompiledCell(blobManager, mapping, persistedCell, response);
	}
}
