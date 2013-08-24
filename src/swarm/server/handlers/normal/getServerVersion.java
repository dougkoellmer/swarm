package swarm.server.handlers.normal;

import javax.servlet.http.HttpServletRequest;

import swarm.server.account.bhUserSession;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.debugging.bhServerTelemetryAssert;
import swarm.server.entities.bhE_GridType;
import swarm.server.entities.bhServerCell;
import swarm.server.session.bhSessionManager;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.telemetry.bhTelemetryDatabase;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.app.sm;
import swarm.shared.app.bhS_App;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.structs.bhE_GetCellAddressError;
import swarm.shared.structs.bhGetCellAddressResult;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class getServerVersion implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		sm.jsonFactory.getHelper().putInt(response.getJson(), bhE_JsonKey.serverVersion, bhS_App.SERVER_VERSION);
	}
}
