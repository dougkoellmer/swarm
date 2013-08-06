package b33hive.server.handlers.normal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;


import b33hive.shared.app.bh;
import b33hive.server.account.bh_s;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhI_Blob;
import b33hive.server.data.blob.bhI_BlobKeySource;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.entities.bhE_GridType;
import b33hive.server.entities.bhServerCell;
import b33hive.server.structs.bhServerCellAddressMapping;
import b33hive.server.structs.bhServerGridCoordinate;
import b33hive.server.transaction.bhI_DeferredRequestHandler;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhServerTransactionManager;
import b33hive.server.transaction.bhTransactionBatch;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.utils.bhU_BitTricks;
import b33hive.shared.entities.bhA_Cell;
import b33hive.shared.entities.bhE_CodeSafetyLevel;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhCodePrivileges;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class getCode implements bhI_RequestHandler, bhI_DeferredRequestHandler
{
	private static final Logger s_logger = Logger.getLogger(getCode.class.getName());
	
	private static final String htmls[] = 
	{
		//"<img src='http://www.krason.mat.univ.szczecin.pl/pk.jpg' />",
		//"<script type='text/javascript'>document.write('Hello Cell');</script>"
		//"<center>HEY</center>",
		
		"<table width='100%' height='100%'><tr><td>A</td><td>B</td></tr><tr><td>C</td><td>D</td></tr></table>",
		//"<img src='http://i2.kym-cdn.com/entries/icons/original/000/007/263/photo_cat2.jpg' />",
		//"<img src='http://indianahumanities.net/wp-content/uploads/2011/08/cat2.jpg' />"
	};
	
	private HashMap<bhI_BlobKeySource, Class<? extends bhI_Blob>> getBlobCoordSet(bhTransactionContext context, boolean forceCreate)
	{
		HashMap<bhI_BlobKeySource, Class<? extends bhI_Blob>> set = (HashMap<bhI_BlobKeySource, Class<? extends bhI_Blob>>) context.getUserData(bhE_RequestPath.getCode);
		
		if( set == null && forceCreate)
		{
			set = new HashMap<bhI_BlobKeySource, Class<? extends bhI_Blob>>();
			context.setUserData(bhE_RequestPath.getCode, set);
		}
		
		return set;
	}
	
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		//bhU_Servlet.simulateException(true);
		
		bhServerCellAddressMapping mapping = new bhServerCellAddressMapping(bhE_GridType.ACTIVE);
		mapping.readJson(request.getJson());
		
		if( context.getRequestCount(bhE_RequestPath.getCode) > 1 )
		{
			HashMap<bhI_BlobKeySource, Class<? extends bhI_Blob>> blobCoordSet = this.getBlobCoordSet(context, true);
			
			blobCoordSet.put(mapping, bhServerCell.class);
			
			response.setError(bhE_ResponseError.DEFERRED);
			
			return;
		}
		
		bhI_BlobManager blobManager = bh_s.blobMngrFactory.create(bhE_BlobCacheLevel.values());
		
		bhServerCell persistedCell = null;
		
		try
		{
			persistedCell = blobManager.getBlob(mapping, bhServerCell.class);
		}
		catch(bhBlobException e)
		{
			response.setError(bhE_ResponseError.SERVICE_EXCEPTION);
			
			return;
		}

		bhE_CodeType eCodeType = bh.jsonFactory.getHelper().getEnum(request.getJson(), bhE_JsonKey.codeType, bhE_CodeType.values());
		
		writeResponse(eCodeType, persistedCell, response);
	}
	
	private bhCode writeResponse(bhE_CodeType eCodeType, bhServerCell persistedCell, bhTransactionResponse response)
	{
		bhCode responseCode = null;
		
		if( persistedCell == null )
		{
			//responseCode = new bhCode("", bhE_CodeType.values());
		}
		else
		{
			responseCode = persistedCell.getCode(eCodeType);
			
			if( responseCode == null )
			{
				responseCode = persistedCell.getStandInCode(eCodeType);
				
				if( responseCode /*still*/== null )
				{
					responseCode = new bhCode("", bhE_CodeType.values());
				}
			}
		}
		
		//--- DRK > Figure out what privileges information to send down to client, if anything.
		//---		Just piggy-backing here cause it will be needed anyway for source.
		//---		If this turns out to be null, no privileges information is sent down.
		bhCodePrivileges privileges = null;
		if( responseCode!= null && (responseCode.isStandInFor(bhE_CodeType.SOURCE) || responseCode.getSafetyLevel() == bhE_CodeSafetyLevel.REQUIRES_DYNAMIC_SANDBOX) )
		{
			if( persistedCell == null )
			{
				privileges = new bhCodePrivileges();
			}
			else
			{
				privileges = persistedCell.getCodePrivileges();
			}
		}
		
		//--- DRK > Create the response cell and send'er back to client.
		//---		We create a new cell here so that we can set code only for the desired type,
		//---		and don't send down all the other code types that the persisted cell might have.
		bhA_Cell responseCell = new bhA_Cell(privileges){};
		responseCell.setCode(eCodeType, responseCode);
		responseCell.writeJson(response.getJson());
		response.setError(bhE_ResponseError.NO_ERROR);
		
		return responseCode;
	}

	@Override
	public void handleDeferredRequests(bhTransactionContext context, bhTransactionBatch batch)
	{
		HashMap<bhI_BlobKeySource, Class<? extends bhI_Blob>> query = this.getBlobCoordSet(context, false);
		
		if( query == null )
		{
			return;
		}

		bhI_BlobManager blobManager = bh_s.blobMngrFactory.create(bhE_BlobCacheLevel.values());
		
		Map<bhI_BlobKeySource, bhI_Blob> result = null;
		bhE_ResponseError error = bhE_ResponseError.NO_ERROR;
		
		try
		{
			result = blobManager.getBlobs(query);
		}
		catch(bhBlobException e)
		{
			result = null;
			error = bhE_ResponseError.SERVICE_EXCEPTION;
		}
		
		HashMap<bhServerCellAddressMapping, Integer> allTypesAlreadyReturned = new HashMap<bhServerCellAddressMapping, Integer>();
		
		for( int i = 0; i < batch.getCount(); i++ )
		{
			bhTransactionRequest request = batch.getRequest(i);
			bhTransactionResponse response = batch.getResponse(i);
			
			if( request.getPath() != bhE_RequestPath.getCode )  continue;
			
			if( error != bhE_ResponseError.NO_ERROR )
			{
				response.setError(error);

				continue;
			}
			
			bhE_CodeType eCodeType = bh.jsonFactory.getHelper().getEnum(request.getJson(), bhE_JsonKey.codeType, bhE_CodeType.values());

			if( result == null )
			{
				writeResponse(eCodeType, null, response);
				
				continue;
			}
			
			bhServerCellAddressMapping mapping = new bhServerCellAddressMapping(bhE_GridType.ACTIVE);
			mapping.readJson(request.getJson());
			
			bhServerCell persistedCell = (bhServerCell) result.get(mapping);
			
			if( persistedCell == null )
			{
				writeResponse(eCodeType, null, response);
				
				continue;
			}
			
			Integer typesAlreadyReturnedForCoord = allTypesAlreadyReturned.get(mapping);
			
			if( typesAlreadyReturnedForCoord != null )
			{
				int typeBit = bhU_BitTricks.calcOrdinalBit(eCodeType.ordinal());
				if( (typesAlreadyReturnedForCoord & typeBit) != 0 )
				{
					response.setError(bhE_ResponseError.REDUNDANT);
					
					continue;
				}
			}

			bhCode code = this.writeResponse(eCodeType, persistedCell, response);
			
			typesAlreadyReturnedForCoord = typesAlreadyReturnedForCoord == null ? 0 : typesAlreadyReturnedForCoord;
			
			typesAlreadyReturnedForCoord |= code.getStandInFlags();
			allTypesAlreadyReturned.put(mapping, typesAlreadyReturnedForCoord);
		}
	}
}
