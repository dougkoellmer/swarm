package swarm.server.handlers.normal;

import javax.servlet.http.HttpServletRequest;

import swarm.server.account.smUserSession;
import swarm.server.data.blob.smBlobException;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smI_BlobManager;
import swarm.server.debugging.smServerTelemetryAssert;
import swarm.server.entities.smE_GridType;
import swarm.server.entities.smServerCell;
import swarm.server.session.smSessionManager;
import swarm.server.structs.smServerCellAddressMapping;
import swarm.server.telemetry.smTelemetryDatabase;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.app.sm;
import swarm.shared.app.smS_App;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smJsonHelper;
import swarm.shared.structs.smE_GetCellAddressError;
import swarm.shared.structs.smGetCellAddressResult;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class getServerVersion implements smI_RequestHandler
{
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		sm.jsonFactory.getHelper().putInt(response.getJson(), smE_JsonKey.serverVersion, smS_App.SERVER_VERSION);
	}
}
