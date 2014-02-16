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

import swarm.server.account.S_ServerAccount;
import swarm.server.account.UserSession;
import swarm.server.app.A_ServerApp;
import swarm.server.app.ServerAppConfig;
import swarm.server.app.ServerContext;
import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.E_GridType;
import swarm.server.entities.BaseServerGrid;
import swarm.server.entities.ServerUser;
import swarm.server.thirdparty.servlet.U_Servlet;
import swarm.server.session.SessionManager;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.shared.entities.A_Grid;
import swarm.shared.json.I_ReadsJson;
import swarm.shared.json.I_WritesJson;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.E_CellAddressParseError;
import swarm.shared.structs.E_GetCellAddressMappingError;
import swarm.shared.structs.GetCellAddressMappingResult;
import swarm.shared.structs.GetCellAddressResult;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class U_InlineTransactions
{
	private static final Logger s_logger = Logger.getLogger(U_InlineTransactions.class.getName());
	
	public static void addInlineTransactions(HttpServletRequest nativeRequest, HttpServletResponse nativeResponse, Writer out) throws IOException
	{
		ServerContext context = A_ServerApp.getInstance().getContext();
		ServerAppConfig config = context.config;
		
		String rawAddress = nativeRequest.getRequestURI();
		ServerCellAddress cellAddress = new ServerCellAddress(rawAddress);
		E_CellAddressParseError parseError = cellAddress.getParseError();
		
		//s_logger.severe("rawCellAddress: " + cellAddress.getRawAddress());
		
		//--- DRK > Servlet mappings can't be regexes, so the next best thing is for the main b33hive jsp servlet
		//---		to catch all paths and then do the regex itself. Technically speaking we don't *have* to redirect here. 
		//---		It's done so that hitting "private" servlet mappings have the same behavior as invalid addresses.
		if( parseError != E_CellAddressParseError.NO_ERROR && parseError != E_CellAddressParseError.EMPTY )
		{
			context.redirector.redirectToMainPage(nativeResponse);
			
			//s_logger.severe("redirecting");
			
			return;
		}
		
		InlineTransactionManager transactionMngr = context.inlineTxnMngr;
		
		try
		{
			transactionMngr.beginBatch(out, nativeRequest, nativeResponse);
			
			GetCellAddressMappingResult mappingResult = new GetCellAddressMappingResult();
			boolean getAddressMapping = true;
			boolean getFocusedCellSize = true;
			
			if( parseError != E_CellAddressParseError.NO_ERROR )
			{
				mappingResult.setError(E_GetCellAddressMappingError.ADDRESS_PARSE_ERROR);
				
				//--- DRK > Only want to send down error if there's stuff after b33hive.net/
				if( parseError != E_CellAddressParseError.EMPTY )
				{
					transactionMngr.makeInlineRequestWithResponse(E_RequestPath.getCellAddressMapping, cellAddress, mappingResult);
				}
				
				getAddressMapping = false;
				getFocusedCellSize = false;
			}
			
			TransactionRequest dummyRequest = new TransactionRequest(context.jsonFactory, nativeRequest);
			TransactionResponse dummyResponse = new TransactionResponse(context.jsonFactory, nativeResponse);
			UserSession session = context.sessionMngr.getSession(dummyRequest, dummyResponse);
			boolean isSessionActive = session != null;
			
			transactionMngr.makeInlineRequest(E_RequestPath.getPasswordChangeToken);
			transactionMngr.makeInlineRequest(E_RequestPath.getAccountInfo);
		
			boolean makeUserRequest = true;
			boolean makeGridRequest = true;
			
			Map<I_BlobKey, Class<? extends I_Blob>> batchGetMap = new HashMap<I_BlobKey, Class<? extends I_Blob>>();
			batchGetMap.put(E_GridType.ACTIVE, BaseServerGrid.class);
			
			if( getAddressMapping )
			{
				batchGetMap.put(cellAddress, ServerCellAddressMapping.class);
			}
			
			if( isSessionActive )
			{
				batchGetMap.put(session, ServerUser.class);
			}
			else
			{
				makeUserRequest = false;
			}
			
			I_BlobManager blobManager = context.blobMngrFactory.create(E_BlobCacheLevel.MEMCACHE, E_BlobCacheLevel.PERSISTENT);
			Map<I_BlobKey, I_Blob> blobBatchResult = null;
			
			try
			{
				blobBatchResult = blobManager.getBlobs(batchGetMap);
			}
			catch(BlobException e)
			{
				// doing nothing for now and retrying things individually below.
				s_logger.severe("batch get error: " + e + e.getCause());
			}
			
			A_Grid grid = null;
			
			if( blobBatchResult != null )
			{
				if( blobBatchResult.containsKey(session))
				{
					transactionMngr.makeInlineRequestWithResponse(E_RequestPath.getUserData, (I_WritesJson) blobBatchResult.get(session));
					
					makeUserRequest = false;
				}
				else
				{
					makeUserRequest = isSessionActive;
				}
				
				if( blobBatchResult.containsKey(E_GridType.ACTIVE) )
				{
					grid = (A_Grid) blobBatchResult.get(E_GridType.ACTIVE);
					transactionMngr.makeInlineRequestWithResponse(E_RequestPath.getGridData, grid);
					
					makeGridRequest = false;
				}
				
				if( blobBatchResult.containsKey(cellAddress) )
				{
					CellAddressMapping mapping = (CellAddressMapping) blobBatchResult.get(cellAddress);
					mappingResult.setMapping(mapping);
					
					//--- DRK > NOTE: Could inline cell code here too...might do that in future based on feedback.
				}
				else
				{
					mappingResult.setError(E_GetCellAddressMappingError.NOT_FOUND);
				}
			}
			else
			{
				mappingResult.setError(E_GetCellAddressMappingError.NOT_FOUND);
			}
			
			if( getAddressMapping )
			{
				transactionMngr.makeInlineRequestWithResponse(E_RequestPath.getCellAddressMapping, cellAddress, mappingResult);
			}
			
			if( makeUserRequest )
			{
				transactionMngr.makeInlineRequest(E_RequestPath.getUserData);
			}
			
			if( makeGridRequest )
			{
				TransactionResponse response = transactionMngr.makeInlineRequest(E_RequestPath.getGridData);
				grid = new A_Grid(){};
				grid.readJson(response.getJsonArgs(), context.jsonFactory);
			}
	
			Point startingPosition = new Point();
			if( mappingResult.isEverythingOk() )
			{
				mappingResult.getMapping().getCoordinate().calcCenterPoint(startingPosition, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
				
				if( getFocusedCellSize ) // should be redundant check...just being safe.
				{
					transactionMngr.makeInlineRequest(E_RequestPath.getFocusedCellSize, mappingResult.getMapping());
				}
			}
			else
			{
				GridCoordinate startingCoord = config.startingCoord != null ? config.startingCoord : new GridCoordinate();
				startingCoord.calcCenterPoint(startingPosition, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
			}
			
			startingPosition.setZ(A_ServerApp.getInstance().getConfig().startingZ);
			transactionMngr.makeInlineRequestWithResponse(E_RequestPath.getStartingPosition, startingPosition);
		}
		finally
		{
			transactionMngr.endBatch();
		}
	}
}
