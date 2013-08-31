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

import swarm.server.account.smS_ServerAccount;
import swarm.server.account.smUserSession;
import swarm.server.account.sm_s;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smI_BlobKey;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smE_GridType;
import swarm.server.entities.smServerGrid;
import swarm.server.entities.smServerUser;
import swarm.server.thirdparty.servlet.smU_Servlet;
import swarm.server.session.smSessionManager;
import swarm.server.structs.smServerCellAddress;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.shared.entities.smA_Grid;
import swarm.shared.json.smI_ReadsJson;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.structs.smE_CellAddressParseError;
import swarm.shared.structs.smE_GetCellAddressMappingError;
import swarm.shared.structs.smGetCellAddressMappingResult;
import swarm.shared.structs.smGetCellAddressResult;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class smU_InlineTransactions
{
	private static final Logger s_logger = Logger.getLogger(smU_InlineTransactions.class.getName());
	
	public static void addInlineTransactions(HttpServletRequest nativeRequest, HttpServletResponse nativeResponse, Writer out) throws IOException
	{
		String rawAddress = nativeRequest.getRequestURI();
		smServerCellAddress cellAddress = new smServerCellAddress(rawAddress);
		smE_CellAddressParseError parseError = cellAddress.getParseError();
		
		//s_logger.severe("rawCellAddress: " + cellAddress.getRawAddress());
		
		//--- DRK > Servlet mappings can't be regexes, so the next best thing is for the main b33hive jsp servlet
		//---		to catch all paths and then do the regex itself. Technically speaking we don't *have* to redirect here. 
		//---		It's done so that hitting "private" servlet mappings have the same behavior as invalid addresses.
		if( parseError != smE_CellAddressParseError.NO_ERROR && parseError != smE_CellAddressParseError.EMPTY )
		{
			sm_s.requestRedirector.redirectToMainPage(nativeResponse);
			
			//s_logger.severe("redirecting");
			
			return;
		}
		
		smInlineTransactionManager transactionManager = sm_s.inlineTxnMngr;
		
		try
		{
			transactionManager.beginBatch(out, nativeRequest, nativeResponse);
			
			smGetCellAddressMappingResult mappingResult = new smGetCellAddressMappingResult();
			boolean getAddressMapping = true;
			
			if( parseError != smE_CellAddressParseError.NO_ERROR )
			{
				mappingResult.setError(smE_GetCellAddressMappingError.ADDRESS_PARSE_ERROR);
				
				//--- DRK > Only want to send down error if there's stuff after b33hive.net/
				if( parseError != smE_CellAddressParseError.EMPTY )
				{
					transactionManager.makeInlineRequestWithResponse(smE_RequestPath.getCellAddressMapping, cellAddress, mappingResult);
				}
				
				getAddressMapping = false;
			}
			
			smTransactionRequest dummyRequest = new smTransactionRequest(nativeRequest);
			smTransactionResponse dummyResponse = new smTransactionResponse(nativeResponse);
			smUserSession session = sm_s.sessionMngr.getSession(dummyRequest, dummyResponse);
			boolean isSessionActive = session != null;
			
			transactionManager.makeInlineRequest(smE_RequestPath.getPasswordChangeToken);
			transactionManager.makeInlineRequest(smE_RequestPath.getAccountInfo);
		
			boolean makeUserRequest = true;
			boolean makeGridRequest = true;
			
			Map<smI_BlobKey, Class<? extends smI_Blob>> batchGet = new HashMap<smI_BlobKey, Class<? extends smI_Blob>>();
			batchGet.put(smE_GridType.ACTIVE, smServerGrid.class);
			
			if( getAddressMapping )
			{
				batchGet.put(cellAddress, smServerCellAddressMapping.class);
			}
			
			if( isSessionActive )
			{
				batchGet.put(session, smServerUser.class);
			}
			else
			{
				makeUserRequest = false;
			}
			
			smI_BlobManager blobManager = sm_s.blobMngrFactory.create(smE_BlobCacheLevel.MEMCACHE, smE_BlobCacheLevel.PERSISTENT);
			Map<smI_BlobKey, smI_Blob> blobBatchResult = null;
			
			try
			{
				blobBatchResult = blobManager.getBlobs(batchGet);
			}
			catch(smBlobException e)
			{
				// doing nothing for now and retrying things individually below.
				s_logger.severe("batch get error: " + e + e.getCause());
			}
			
			smA_Grid grid = null;
			
			if( blobBatchResult != null )
			{
				if( blobBatchResult.containsKey(session))
				{
					transactionManager.makeInlineRequestWithResponse(smE_RequestPath.getUserData, (smI_ReadsJson) blobBatchResult.get(session));
					
					makeUserRequest = false;
				}
				else
				{
					makeUserRequest = isSessionActive;
				}
				
				if( blobBatchResult.containsKey(smE_GridType.ACTIVE) )
				{
					grid = (smA_Grid) blobBatchResult.get(smE_GridType.ACTIVE);
					transactionManager.makeInlineRequestWithResponse(smE_RequestPath.getGridData, grid);
					
					makeGridRequest = false;
				}
				
				if( blobBatchResult.containsKey(cellAddress) )
				{
					smCellAddressMapping mapping = (smCellAddressMapping) blobBatchResult.get(cellAddress);
					mappingResult.setMapping(mapping);
					
					//--- DRK > NOTE: Could inline cell code here too...might do that in future based on feedback.
				}
				else
				{
					mappingResult.setError(smE_GetCellAddressMappingError.NOT_FOUND);
				}
			}
			else
			{
				mappingResult.setError(smE_GetCellAddressMappingError.NOT_FOUND);
			}
			
			if( getAddressMapping )
			{
				transactionManager.makeInlineRequestWithResponse(smE_RequestPath.getCellAddressMapping, cellAddress, mappingResult);
			}
			
			if( makeUserRequest )
			{
				transactionManager.makeInlineRequest(smE_RequestPath.getUserData);
			}
			
			if( makeGridRequest )
			{
				smTransactionResponse response = transactionManager.makeInlineRequest(smE_RequestPath.getGridData);
				grid = new smA_Grid(){};
				grid.readJson(null, response.getJsonArgs());
			}
	
			smPoint startingPosition = new smPoint();
			if( mappingResult.isEverythingOk() )
			{
				mappingResult.getMapping().getCoordinate().calcCenterPoint(startingPosition, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
			}
			else
			{
				(new smGridCoordinate()).calcCenterPoint(startingPosition, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
			}
			
			startingPosition.setZ(sm_s.app.getConfig().startingZ);
			transactionManager.makeInlineRequestWithResponse(smE_RequestPath.getStartingPosition, startingPosition);
		}
		finally
		{
			transactionManager.endBatch();
		}
	}
}
