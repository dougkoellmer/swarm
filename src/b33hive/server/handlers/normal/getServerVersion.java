package com.b33hive.server.handlers;

import javax.servlet.http.HttpServletRequest;

import com.b33hive.server.account.bhUserSession;
import com.b33hive.server.data.blob.bhBlobException;
import com.b33hive.server.data.blob.bhBlobManagerFactory;
import com.b33hive.server.data.blob.bhE_BlobCacheLevel;
import com.b33hive.server.data.blob.bhI_BlobManager;
import com.b33hive.server.debugging.bhServerTelemetryAssert;
import com.b33hive.server.entities.bhE_GridType;
import com.b33hive.server.entities.bhServerCell;
import com.b33hive.server.session.bhSessionManager;
import com.b33hive.server.structs.bhServerCellAddressMapping;
import com.b33hive.server.telemetry.bhTelemetryDatabase;
import com.b33hive.server.transaction.bhI_RequestHandler;
import com.b33hive.server.transaction.bhTransactionContext;
import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.json.bhE_JsonKey;
import com.b33hive.shared.json.bhJsonHelper;
import com.b33hive.shared.structs.bhE_GetCellAddressError;
import com.b33hive.shared.structs.bhGetCellAddressResult;
import com.b33hive.shared.transaction.bhE_ResponseError;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

public class getServerVersion implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhJsonHelper.getInstance().putInt(response.getJson(), bhE_JsonKey.serverVersion, bhS_App.SERVER_VERSION);
	}
}
