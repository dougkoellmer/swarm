package swarm.server.handlers.admin;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;


import swarm.server.account.bhE_Role;
import swarm.server.account.bhUserSession;
import swarm.server.account.sm_s;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhE_BlobTransactionType;
import swarm.server.data.blob.bhI_Blob;
import swarm.server.data.blob.bhI_BlobKey;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.entities.bhE_GridType;
import swarm.server.entities.bhServerCell;
import swarm.server.entities.bhServerGrid;
import swarm.server.entities.bhServerUser;
import swarm.server.handlers.bhU_CellCode;
import swarm.server.session.bhSessionManager;
import swarm.server.structs.bhServerCellAddress;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.structs.bhServerCode;
import swarm.server.structs.bhServerCodePrivileges;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhServerTransactionManager;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.code.bhA_CodeCompiler;
import swarm.shared.code.bhCompilerResult;
import swarm.shared.code.bhE_CompilationStatus;
import swarm.shared.entities.bhA_User;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.structs.bhCode;
import swarm.shared.structs.bhE_NetworkPrivilege;
import swarm.shared.structs.bhGridCoordinate;
import swarm.shared.transaction.bhE_RequestPath;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class recompileCells implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(recompileCells.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhI_BlobManager blobManager = sm_s.blobMngrFactory.create(bhE_BlobCacheLevel.PERSISTENT);
		
		bhServerGrid grid = null;
		
		try
		{
			grid = blobManager.getBlob(bhE_GridType.ACTIVE, bhServerGrid.class);
		}
		catch( bhBlobException e)
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			s_logger.severe("Could not retrieve grid data due to exception: " + e);
			
			return;
		}
		
		if( grid == null )
		{
			response.setError(bhE_ResponseError.BAD_STATE);
			
			s_logger.severe("Grid came up null when it probably should have been initialized.");
			
			return;
		}
		
		bhServerCellAddressMapping mapping = new bhServerCellAddressMapping(bhE_GridType.ACTIVE);
		
		for( int i = 0; i < grid.getHeight(); i++ )
		{
			for( int j = 0; j < grid.getWidth(); j++ )
			{
				mapping.getCoordinate().set(j,  i);
				if( grid.isTaken(mapping.getCoordinate()) )
				{
					bhServerCell cell = bhU_CellCode.getCellForCompile(blobManager, mapping, response);
					
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
	
	private boolean recompile(bhI_BlobManager blobManager, bhServerCell persistedCell, bhServerCellAddressMapping mapping, bhTransactionResponse response)
	{
		bhCode sourceCode = persistedCell.getCode(bhE_CodeType.SOURCE);
		
		if( sourceCode == null )
		{
			return true;
		}
		
		bhCompilerResult result = bhU_CellCode.compileCell(persistedCell, sourceCode, mapping);
		
		if( result.getStatus() != bhE_CompilationStatus.NO_ERROR )
		{
			bhCode emptySplashCode = new bhCode("", bhE_CodeType.SPLASH, bhE_CodeType.COMPILED);
			persistedCell.setCode(bhE_CodeType.SPLASH, emptySplashCode);
			persistedCell.setCode(bhE_CodeType.COMPILED, null);
			
			s_logger.severe("Source code now has an error in it...presumably it did not before.");
		}
		
		return bhU_CellCode.saveBackCompiledCell(blobManager, mapping, persistedCell, response);
	}
}
