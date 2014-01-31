package swarm.server.handlers.normal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;


import swarm.shared.app.BaseAppContext;

import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.entities.E_GridType;
import swarm.server.entities.ServerCell;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.structs.ServerGridCoordinate;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_DeferredRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.ServerTransactionManager;
import swarm.server.transaction.TransactionBatch;
import swarm.server.transaction.TransactionContext;
import swarm.shared.utils.U_Bits;
import swarm.shared.entities.A_Cell;
import swarm.shared.entities.E_CodeSafetyLevel;
import swarm.shared.entities.E_CodeType;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.JsonHelper;
import swarm.shared.structs.Code;
import swarm.shared.structs.CodePrivileges;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class getCode extends A_DefaultRequestHandler implements I_DeferredRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(getCode.class.getName());
	
	private HashMap<I_BlobKey, Class<? extends I_Blob>> getBlobCoordSet(TransactionContext context, boolean forceCreate)
	{
		HashMap<I_BlobKey, Class<? extends I_Blob>> set = (HashMap<I_BlobKey, Class<? extends I_Blob>>) context.getUserData(E_RequestPath.getCode);
		
		if( set == null && forceCreate)
		{
			set = new HashMap<I_BlobKey, Class<? extends I_Blob>>();
			context.setUserData(E_RequestPath.getCode, set);
		}
		
		return set;
	}
	
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		//smU_Servlet.simulateException(true);
		
		ServerCellAddressMapping mapping = new ServerCellAddressMapping(E_GridType.ACTIVE);
		mapping.readJson(m_serverContext.jsonFactory, request.getJsonArgs());
		
		if( context.getRequestCount(E_RequestPath.getCode) > 1 )
		{
			HashMap<I_BlobKey, Class<? extends I_Blob>> blobCoordSet = this.getBlobCoordSet(context, true);
			
			blobCoordSet.put(mapping, ServerCell.class);
			
			response.setError(E_ResponseError.DEFERRED);
			
			return;
		}
		
		I_BlobManager blobManager = m_serverContext.blobMngrFactory.create(E_BlobCacheLevel.values());
		
		ServerCell persistedCell = null;
		
		try
		{
			persistedCell = blobManager.getBlob(mapping, ServerCell.class);
		}
		catch(BlobException e)
		{
			response.setError(E_ResponseError.SERVICE_EXCEPTION);
			
			return;
		}

		E_CodeType eCodeType = m_serverContext.jsonFactory.getHelper().getEnum(request.getJsonArgs(), E_JsonKey.codeType, E_CodeType.values());
		
		writeResponse(eCodeType, persistedCell, response);
	}
	
	private Code writeResponse(E_CodeType eCodeType, ServerCell persistedCell, TransactionResponse response)
	{
		Code responseCode = null;
		
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
					responseCode = new Code("", E_CodeType.values());
				}
			}
		}
		
		//--- DRK > Figure out what privileges information to send down to client, if anything.
		//---		Just piggy-backing here cause it will be needed anyway for source.
		//---		If this turns out to be null, no privileges information is sent down.
		CodePrivileges privileges = null;
		if( responseCode!= null && (responseCode.isStandInFor(E_CodeType.SOURCE) || responseCode.getSafetyLevel() == E_CodeSafetyLevel.VIRTUAL_DYNAMIC_SANDBOX) )
		{
			if( persistedCell == null )
			{
				privileges = new CodePrivileges();
			}
			else
			{
				privileges = persistedCell.getCodePrivileges();
			}
		}
		
		//--- DRK > Create the response cell and send'er back to client.
		//---		We create a new cell here so that we can set code only for the desired type,
		//---		and don't send down all the other code types that the persisted cell might have.
		A_Cell responseCell = new A_Cell(privileges){};
		responseCell.setCode(eCodeType, responseCode);
		responseCell.writeJson(m_serverContext.jsonFactory, response.getJsonArgs());
		response.setError(E_ResponseError.NO_ERROR);
		
		return responseCode;
	}

	@Override
	public void handleDeferredRequests(TransactionContext context, TransactionBatch batch)
	{
		HashMap<I_BlobKey, Class<? extends I_Blob>> query = this.getBlobCoordSet(context, false);
		
		if( query == null )
		{
			return;
		}

		I_BlobManager blobManager = m_serverContext.blobMngrFactory.create(E_BlobCacheLevel.values());
		
		Map<I_BlobKey, I_Blob> result = null;
		E_ResponseError error = E_ResponseError.NO_ERROR;
		
		try
		{
			result = blobManager.getBlobs(query);
		}
		catch(BlobException e)
		{
			result = null;
			error = E_ResponseError.SERVICE_EXCEPTION;
		}
		
		HashMap<ServerCellAddressMapping, Integer> allTypesAlreadyReturned = new HashMap<ServerCellAddressMapping, Integer>();
		
		for( int i = 0; i < batch.getCount(); i++ )
		{
			TransactionRequest request = batch.getRequest(i);
			TransactionResponse response = batch.getResponse(i);
			
			if( request.getPath() != E_RequestPath.getCode )  continue;
			
			if( error != E_ResponseError.NO_ERROR )
			{
				response.setError(error);

				continue;
			}
			
			E_CodeType eCodeType = m_serverContext.jsonFactory.getHelper().getEnum(request.getJsonArgs(), E_JsonKey.codeType, E_CodeType.values());

			if( result == null )
			{
				writeResponse(eCodeType, null, response);
				
				continue;
			}
			
			ServerCellAddressMapping mapping = new ServerCellAddressMapping(E_GridType.ACTIVE);
			mapping.readJson(m_serverContext.jsonFactory, request.getJsonArgs());
			
			ServerCell persistedCell = (ServerCell) result.get(mapping);
			
			if( persistedCell == null )
			{
				writeResponse(eCodeType, null, response);
				
				continue;
			}
			
			Integer typesAlreadyReturnedForCoord = allTypesAlreadyReturned.get(mapping);
			
			if( typesAlreadyReturnedForCoord != null )
			{
				int typeBit = U_Bits.calcOrdinalBit(eCodeType.ordinal());
				if( (typesAlreadyReturnedForCoord & typeBit) != 0 )
				{
					response.setError(E_ResponseError.REDUNDANT);
					
					continue;
				}
			}

			Code code = this.writeResponse(eCodeType, persistedCell, response);
			
			typesAlreadyReturnedForCoord = typesAlreadyReturnedForCoord == null ? 0 : typesAlreadyReturnedForCoord;
			
			typesAlreadyReturnedForCoord |= code.getStandInFlags();
			allTypesAlreadyReturned.put(mapping, typesAlreadyReturnedForCoord);
		}
	}
}
