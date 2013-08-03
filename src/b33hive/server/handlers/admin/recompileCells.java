package b33hive.server.handlers.admin;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import b33hive.server.account.bhE_Role;
import b33hive.server.account.bhUserSession;
import b33hive.server.app.bhS_ServerApp;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhE_BlobTransactionType;
import b33hive.server.data.blob.bhI_Blob;
import b33hive.server.data.blob.bhI_BlobKeySource;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.entities.bhE_GridType;
import b33hive.server.entities.bhServerCell;
import b33hive.server.entities.bhServerGrid;
import b33hive.server.entities.bhServerUser;
import b33hive.server.handlers.bhU_CellCode;
import b33hive.server.session.bhSessionManager;
import b33hive.server.structs.bhServerCellAddress;
import b33hive.server.structs.bhServerCellAddressMapping;
import b33hive.server.structs.bhServerCode;
import b33hive.server.structs.bhServerCodePrivileges;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhServerTransactionManager;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.code.bhA_CodeCompiler;
import b33hive.shared.code.bhCompilerResult;
import b33hive.shared.code.bhE_CompilationStatus;
import b33hive.shared.entities.bhA_User;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhE_NetworkPrivilege;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class recompileCells implements bhI_RequestHandler
{
	private static final Logger s_logger = Logger.getLogger(recompileCells.class.getName());
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhI_BlobManager blobManager = bhBlobManagerFactory.getInstance().create(bhE_BlobCacheLevel.PERSISTENT);
		
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
		
		for( int i = 0; i < grid.getSize(); i++ )
		{
			for( int j = 0; j < grid.getSize(); j++ )
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
