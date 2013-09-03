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
import swarm.server.transaction.smA_DefaultRequestHandler;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.structs.smE_GetCellAddressError;
import swarm.shared.structs.smGetCellAddressResult;
import swarm.shared.transaction.smE_ResponseError;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public class logAssert extends smA_DefaultRequestHandler
{
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		smUserSession session = m_serverContext.sessionMngr.getSession(request, response);
		
		int accountId = session != null ? session.getAccountId() : -1;
		String ip = ((HttpServletRequest) request.getNativeRequest()).getRemoteAddr();
		
		smServerTelemetryAssert telemetryAssert = new smServerTelemetryAssert(m_serverContext.jsonFactory, request.getJsonArgs(), ip, accountId);
		
		m_serverContext.telemetryDb.put(telemetryAssert);
	}
}
