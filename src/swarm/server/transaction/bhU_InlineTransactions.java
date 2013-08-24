package swarm.server.transaction;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import swarm.server.account.bhS_ServerAccount;
import swarm.server.account.bhUserSession;
import swarm.server.account.sm_s;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhI_Blob;
import swarm.server.data.blob.bhI_BlobKey;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.entities.bhE_GridType;
import swarm.server.entities.bhServerGrid;
import swarm.server.entities.bhServerUser;
import swarm.server.thirdparty.servlet.bhU_Servlet;
import swarm.server.session.bhSessionManager;
import swarm.server.structs.bhServerCellAddress;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.shared.entities.bhA_Grid;
import swarm.shared.json.bhI_JsonEncodable;
import swarm.shared.structs.bhCellAddress;
import swarm.shared.structs.bhCellAddressMapping;
import swarm.shared.structs.bhE_CellAddressParseError;
import swarm.shared.structs.bhE_GetCellAddressMappingError;
import swarm.shared.structs.bhGetCellAddressMappingResult;
import swarm.shared.structs.bhGetCellAddressResult;
import swarm.shared.structs.bhGridCoordinate;
import swarm.shared.structs.bhPoint;
import swarm.shared.transaction.bhE_RequestPath;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class bhU_InlineTransactions
{
	private static final Logger s_logger = Logger.getLogger(bhU_InlineTransactions.class.getName());
	
	public static void addInlineTransactions(HttpServletRequest nativeRequest, HttpServletResponse nativeResponse, Writer out) throws IOException
	{
		String rawAddress = nativeRequest.getRequestURI();
		bhServerCellAddress cellAddress = new bhServerCellAddress(rawAddress);
		bhE_CellAddressParseError parseError = cellAddress.getParseError();
		
		//s_logger.severe("rawCellAddress: " + cellAddress.getRawAddress());
		
		//--- DRK > Servlet mappings can't be regexes, so the next best thing is for the main b33hive jsp servlet
		//---		to catch all paths and then do the regex itself. Technically speaking we don't *have* to redirect here. 
		//---		It's done so that hitting "private" servlet mappings have the same behavior as invalid addresses.
		if( parseError != bhE_CellAddressParseError.NO_ERROR && parseError != bhE_CellAddressParseError.EMPTY )
		{
			sm_s.requestRedirector.redirectToMainPage(nativeResponse);
			
			//s_logger.severe("redirecting");
			
			return;
		}
		
		bhInlineTransactionManager transactionManager = sm_s.inlineTxnMngr;
		
		try
		{
			transactionManager.beginBatch(out, nativeRequest, nativeResponse);
			
			bhGetCellAddressMappingResult mappingResult = new bhGetCellAddressMappingResult();
			boolean getAddressMapping = true;
			
			if( parseError != bhE_CellAddressParseError.NO_ERROR )
			{
				mappingResult.setError(bhE_GetCellAddressMappingError.ADDRESS_PARSE_ERROR);
				
				//--- DRK > Only want to send down error if there's stuff after b33hive.net/
				if( parseError != bhE_CellAddressParseError.EMPTY )
				{
					transactionManager.makeInlineRequestWithResponse(bhE_RequestPath.getCellAddressMapping, cellAddress, mappingResult);
				}
				
				getAddressMapping = false;
			}
			
			bhTransactionRequest dummyRequest = new bhTransactionRequest(nativeRequest);
			bhTransactionResponse dummyResponse = new bhTransactionResponse(nativeResponse);
			bhUserSession session = sm_s.sessionMngr.getSession(dummyRequest, dummyResponse);
			boolean isSessionActive = session != null;
			
			transactionManager.makeInlineRequest(bhE_RequestPath.getPasswordChangeToken);
			transactionManager.makeInlineRequest(bhE_RequestPath.getAccountInfo);
		
			boolean makeUserRequest = true;
			boolean makeGridRequest = true;
			
			Map<bhI_BlobKey, Class<? extends bhI_Blob>> batchGet = new HashMap<bhI_BlobKey, Class<? extends bhI_Blob>>();
			batchGet.put(bhE_GridType.ACTIVE, bhServerGrid.class);
			
			if( getAddressMapping )
			{
				batchGet.put(cellAddress, bhServerCellAddressMapping.class);
			}
			
			if( isSessionActive )
			{
				batchGet.put(session, bhServerUser.class);
			}
			else
			{
				makeUserRequest = false;
			}
			
			bhI_BlobManager blobManager = sm_s.blobMngrFactory.create(bhE_BlobCacheLevel.MEMCACHE, bhE_BlobCacheLevel.PERSISTENT);
			Map<bhI_BlobKey, bhI_Blob> blobBatchResult = null;
			
			try
			{
				blobBatchResult = blobManager.getBlobs(batchGet);
			}
			catch(bhBlobException e)
			{
				// doing nothing for now and retrying things individually below.
				s_logger.severe("batch get error: " + e + e.getCause());
			}
			
			bhA_Grid grid = null;
			
			if( blobBatchResult != null )
			{
				if( blobBatchResult.containsKey(session))
				{
					transactionManager.makeInlineRequestWithResponse(bhE_RequestPath.getUserData, (bhI_JsonEncodable) blobBatchResult.get(session));
					
					makeUserRequest = false;
				}
				else
				{
					makeUserRequest = isSessionActive;
				}
				
				if( blobBatchResult.containsKey(bhE_GridType.ACTIVE) )
				{
					grid = (bhA_Grid) blobBatchResult.get(bhE_GridType.ACTIVE);
					transactionManager.makeInlineRequestWithResponse(bhE_RequestPath.getGridData, grid);
					
					makeGridRequest = false;
				}
				
				if( blobBatchResult.containsKey(cellAddress) )
				{
					bhCellAddressMapping mapping = (bhCellAddressMapping) blobBatchResult.get(cellAddress);
					mappingResult.setMapping(mapping);
					
					//--- DRK > NOTE: Could inline cell code here too...might do that in future based on feedback.
				}
				else
				{
					mappingResult.setError(bhE_GetCellAddressMappingError.NOT_FOUND);
				}
			}
			else
			{
				mappingResult.setError(bhE_GetCellAddressMappingError.NOT_FOUND);
			}
			
			if( getAddressMapping )
			{
				transactionManager.makeInlineRequestWithResponse(bhE_RequestPath.getCellAddressMapping, cellAddress, mappingResult);
			}
			
			if( makeUserRequest )
			{
				transactionManager.makeInlineRequest(bhE_RequestPath.getUserData);
			}
			
			if( makeGridRequest )
			{
				bhTransactionResponse response = transactionManager.makeInlineRequest(bhE_RequestPath.getGridData);
				grid = new bhA_Grid(){};
				grid.readJson(response.getJson());
			}
	
			bhPoint startingPosition = new bhPoint();
			if( mappingResult.isEverythingOk() )
			{
				mappingResult.getMapping().getCoordinate().calcCenterPoint(startingPosition, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
			}
			else
			{
				(new bhGridCoordinate()).calcCenterPoint(startingPosition, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
			}
			
			startingPosition.setZ(sm_s.app.getConfig().startingZ);
			transactionManager.makeInlineRequestWithResponse(bhE_RequestPath.getStartingPosition, startingPosition);
		}
		finally
		{
			transactionManager.endBatch();
		}
	}
}
