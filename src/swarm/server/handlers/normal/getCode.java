package swarm.server.handlers.normal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;


import swarm.shared.app.smSharedAppContext;

import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smI_BlobKey;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.entities.smE_GridType;
import swarm.server.entities.smServerCell;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.structs.smServerGridCoordinate;
import swarm.server.transaction.smA_DefaultRequestHandler;
import swarm.server.transaction.smI_DeferredRequestHandler;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smServerTransactionManager;
import swarm.server.transaction.smTransactionBatch;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.utils.smU_Bits;
import swarm.shared.entities.smA_Cell;
import swarm.shared.entities.smE_CodeSafetyLevel;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smJsonHelper;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smCodePrivileges;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class getCode extends smA_DefaultRequestHandler implements smI_DeferredRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(getCode.class.getName());
	
	private HashMap<smI_BlobKey, Class<? extends smI_Blob>> getBlobCoordSet(smTransactionContext context, boolean forceCreate)
	{
		HashMap<smI_BlobKey, Class<? extends smI_Blob>> set = (HashMap<smI_BlobKey, Class<? extends smI_Blob>>) context.getUserData(smE_RequestPath.getCode);
		
		if( set == null && forceCreate)
		{
			set = new HashMap<smI_BlobKey, Class<? extends smI_Blob>>();
			context.setUserData(smE_RequestPath.getCode, set);
		}
		
		return set;
	}
	
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		//smU_Servlet.simulateException(true);
		
		smServerCellAddressMapping mapping = new smServerCellAddressMapping(smE_GridType.ACTIVE);
		mapping.readJson(m_serverContext.jsonFactory, request.getJsonArgs());
		
		if( context.getRequestCount(smE_RequestPath.getCode) > 1 )
		{
			HashMap<smI_BlobKey, Class<? extends smI_Blob>> blobCoordSet = this.getBlobCoordSet(context, true);
			
			blobCoordSet.put(mapping, smServerCell.class);
			
			response.setError(smE_ResponseError.DEFERRED);
			
			return;
		}
		
		smI_BlobManager blobManager = m_serverContext.blobMngrFactory.create(smE_BlobCacheLevel.values());
		
		smServerCell persistedCell = null;
		
		try
		{
			persistedCell = blobManager.getBlob(mapping, smServerCell.class);
		}
		catch(smBlobException e)
		{
			response.setError(smE_ResponseError.SERVICE_EXCEPTION);
			
			return;
		}

		smE_CodeType eCodeType = m_serverContext.jsonFactory.getHelper().getEnum(request.getJsonArgs(), smE_JsonKey.codeType, smE_CodeType.values());
		
		writeResponse(eCodeType, persistedCell, response);
	}
	
	private smCode writeResponse(smE_CodeType eCodeType, smServerCell persistedCell, smTransactionResponse response)
	{
		smCode responseCode = null;
		
		if( persistedCell == null )
		{
			//responseCode = new smCode("", smE_CodeType.values());
		}
		else
		{
			responseCode = persistedCell.getCode(eCodeType);
			
			if( responseCode == null )
			{
				responseCode = persistedCell.getStandInCode(eCodeType);
				
				if( responseCode /*still*/== null )
				{
					responseCode = new smCode("", smE_CodeType.values());
				}
			}
		}
		
		//--- DRK > Figure out what privileges information to send down to client, if anything.
		//---		Just piggy-backing here cause it will be needed anyway for source.
		//---		If this turns out to be null, no privileges information is sent down.
		smCodePrivileges privileges = null;
		if( responseCode!= null && (responseCode.isStandInFor(smE_CodeType.SOURCE) || responseCode.getSafetyLevel() == smE_CodeSafetyLevel.VIRTUAL_DYNAMIC_SANDBOX) )
		{
			if( persistedCell == null )
			{
				privileges = new smCodePrivileges();
			}
			else
			{
				privileges = persistedCell.getCodePrivileges();
			}
		}
		
		//--- DRK > Create the response cell and send'er back to client.
		//---		We create a new cell here so that we can set code only for the desired type,
		//---		and don't send down all the other code types that the persisted cell might have.
		smA_Cell responseCell = new smA_Cell(privileges){};
		responseCell.setCode(eCodeType, responseCode);
		responseCell.writeJson(m_serverContext.jsonFactory, response.getJsonArgs());
		response.setError(smE_ResponseError.NO_ERROR);
		
		return responseCode;
	}

	@Override
	public void handleDeferredRequests(smTransactionContext context, smTransactionBatch batch)
	{
		HashMap<smI_BlobKey, Class<? extends smI_Blob>> query = this.getBlobCoordSet(context, false);
		
		if( query == null )
		{
			return;
		}

		smI_BlobManager blobManager = m_serverContext.blobMngrFactory.create(smE_BlobCacheLevel.values());
		
		Map<smI_BlobKey, smI_Blob> result = null;
		smE_ResponseError error = smE_ResponseError.NO_ERROR;
		
		try
		{
			result = blobManager.getBlobs(query);
		}
		catch(smBlobException e)
		{
			result = null;
			error = smE_ResponseError.SERVICE_EXCEPTION;
		}
		
		HashMap<smServerCellAddressMapping, Integer> allTypesAlreadyReturned = new HashMap<smServerCellAddressMapping, Integer>();
		
		for( int i = 0; i < batch.getCount(); i++ )
		{
			smTransactionRequest request = batch.getRequest(i);
			smTransactionResponse response = batch.getResponse(i);
			
			if( request.getPath() != smE_RequestPath.getCode )  continue;
			
			if( error != smE_ResponseError.NO_ERROR )
			{
				response.setError(error);

				continue;
			}
			
			smE_CodeType eCodeType = m_serverContext.jsonFactory.getHelper().getEnum(request.getJsonArgs(), smE_JsonKey.codeType, smE_CodeType.values());

			if( result == null )
			{
				writeResponse(eCodeType, null, response);
				
				continue;
			}
			
			smServerCellAddressMapping mapping = new smServerCellAddressMapping(smE_GridType.ACTIVE);
			mapping.readJson(m_serverContext.jsonFactory, request.getJsonArgs());
			
			smServerCell persistedCell = (smServerCell) result.get(mapping);
			
			if( persistedCell == null )
			{
				writeResponse(eCodeType, null, response);
				
				continue;
			}
			
			Integer typesAlreadyReturnedForCoord = allTypesAlreadyReturned.get(mapping);
			
			if( typesAlreadyReturnedForCoord != null )
			{
				int typeBit = smU_Bits.calcOrdinalBit(eCodeType.ordinal());
				if( (typesAlreadyReturnedForCoord & typeBit) != 0 )
				{
					response.setError(smE_ResponseError.REDUNDANT);
					
					continue;
				}
			}

			smCode code = this.writeResponse(eCodeType, persistedCell, response);
			
			typesAlreadyReturnedForCoord = typesAlreadyReturnedForCoord == null ? 0 : typesAlreadyReturnedForCoord;
			
			typesAlreadyReturnedForCoord |= code.getStandInFlags();
			allTypesAlreadyReturned.put(mapping, typesAlreadyReturnedForCoord);
		}
	}
}
