package b33hive.server.handlers.normal;

import javax.servlet.http.HttpServletRequest;

import b33hive.server.account.bhUserSession;
import b33hive.server.data.blob.bhBlobException;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhI_BlobManager;
import b33hive.server.debugging.bhServerTelemetryAssert;
import b33hive.server.entities.bhE_GridType;
import b33hive.server.entities.bhServerCell;
import b33hive.server.session.bhSessionManager;
import b33hive.server.structs.bhServerCellAddressMapping;
import b33hive.server.telemetry.bhTelemetryDatabase;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.app.bh;
import b33hive.shared.app.bhS_App;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.structs.bhE_GetCellAddressError;
import b33hive.shared.structs.bhGetCellAddressResult;
import b33hive.shared.transaction.bhE_ResponseError;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public class getServerVersion implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bh.jsonFactory.getHelper().putInt(response.getJson(), bhE_JsonKey.serverVersion, bhS_App.SERVER_VERSION);
	}
}
