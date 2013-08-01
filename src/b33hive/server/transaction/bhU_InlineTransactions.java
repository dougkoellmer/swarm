package com.b33hive.server.transaction;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import com.b33hive.server.account.bhS_ServerAccount;
import com.b33hive.server.account.bhUserSession;
import com.b33hive.server.app.bhS_ServerApp;
import com.b33hive.server.data.blob.bhBlobException;
import com.b33hive.server.data.blob.bhBlobManagerFactory;
import com.b33hive.server.data.blob.bhE_BlobCacheLevel;
import com.b33hive.server.data.blob.bhI_Blob;
import com.b33hive.server.data.blob.bhI_BlobKeySource;
import com.b33hive.server.data.blob.bhI_BlobManager;
import com.b33hive.server.entities.bhE_GridType;
import com.b33hive.server.entities.bhServerGrid;
import com.b33hive.server.entities.bhServerUser;
import com.b33hive.server.servlets.bhU_Servlet;
import com.b33hive.server.session.bhSessionManager;
import com.b33hive.server.structs.bhServerCellAddress;
import com.b33hive.server.structs.bhServerCellAddressMapping;
import com.b33hive.server.test.bhT_Grid;
import com.b33hive.shared.json.bhI_JsonEncodable;
import com.b33hive.shared.structs.bhCellAddress;
import com.b33hive.shared.structs.bhCellAddressMapping;
import com.b33hive.shared.structs.bhE_CellAddressParseError;
import com.b33hive.shared.structs.bhE_GetCellAddressMappingError;
import com.b33hive.shared.structs.bhGetCellAddressMappingResult;
import com.b33hive.shared.structs.bhGetCellAddressResult;
import com.b33hive.shared.structs.bhGridCoordinate;
import com.b33hive.shared.structs.bhPoint;
import com.b33hive.shared.transaction.bhE_RequestPath;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

public class bhU_InlineTransactions
{
	private static final Logger s_logger = Logger.getLogger(bhU_InlineTransactions.class.getName());
	
	public static void addInlineTransactions(HttpServletRequest nativeRequest, HttpServletResponse nativeResponse, Writer out) throws IOException
	{
		String rawAddress = nativeRequest.getRequestURI().toLowerCase();
		bhServerCellAddress cellAddress = new bhServerCellAddress(rawAddress);
		bhE_CellAddressParseError parseError = cellAddress.getParseError();
		
		//s_logger.severe("rawCellAddress: " + cellAddress.getRawAddress());
		
		//--- DRK > Servlet mappings can't be regexes, so the next best thing is for the main b33hive jsp servlet
		//---		to catch all paths and then do the regex itself. Technically speaking we don't *have* to redirect here. 
		//---		It's done so that "private" servlet mappings can have the same behavior...redirecting to the main page.
		if( parseError != bhE_CellAddressParseError.NO_ERROR && parseError != bhE_CellAddressParseError.EMPTY )
		{
			bhU_Servlet.redirectToMainPage(nativeResponse);
			
			//s_logger.severe("redirecting");
			
			return;
		}
		
		bhInlineTransactionManager transactionManager = bhInlineTransactionManager.getInstance();
		
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
			bhSessionManager sessionManager = bhSessionManager.getInstance();
			bhUserSession session = sessionManager.getSession(dummyRequest, dummyResponse);
			boolean isSessionActive = session != null;
			
			transactionManager.makeInlineRequest(bhE_RequestPath.getPasswordChangeToken);
			transactionManager.makeInlineRequest(bhE_RequestPath.getAccountInfo);
		
			boolean makeUserRequest = true;
			boolean makeGridRequest = true;
			
			Map<bhI_BlobKeySource, Class<? extends bhI_Blob>> batchGet = new HashMap<bhI_BlobKeySource, Class<? extends bhI_Blob>>();
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
			
			bhI_BlobManager blobManager = bhBlobManagerFactory.getInstance().create(bhE_BlobCacheLevel.MEMCACHE, bhE_BlobCacheLevel.PERSISTENT);
			Map<bhI_BlobKeySource, bhI_Blob> blobBatchResult = null;
			
			try
			{
				blobBatchResult = blobManager.getBlobs(batchGet);
			}
			catch(bhBlobException e)
			{
				// doing nothing for now and retrying things individually below.
				s_logger.severe("batch get error: " + e);
			}
			
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
					transactionManager.makeInlineRequestWithResponse(bhE_RequestPath.getGridData, (bhI_JsonEncodable) blobBatchResult.get(bhE_GridType.ACTIVE));
					
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
				transactionManager.makeInlineRequest(bhE_RequestPath.getGridData);
			}
	
			bhPoint startingPosition = new bhPoint();
			if( mappingResult.isEverythingOk() )
			{
				mappingResult.getMapping().getCoordinate().calcCenterPoint(startingPosition, 1);
			}
			else
			{
				(new bhGridCoordinate()).calcCenterPoint(startingPosition, 1);
			}
			
			startingPosition.setZ(bhS_ServerApp.DEFAULT_STARTING_Z);
			transactionManager.makeInlineRequestWithResponse(bhE_RequestPath.getStartingPosition, startingPosition);
		}
		finally
		{
			transactionManager.endBatch();
		}
	}
}
