package swarm.server.handlers.normal;

import javax.servlet.http.HttpServletRequest;

import swarm.server.account.UserSession;

import swarm.server.data.blob.BlobException;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_BlobManager;
import swarm.server.debugging.ServerTelemetryAssert;
import swarm.server.entities.E_GridType;
import swarm.server.entities.ServerCell;
import swarm.server.session.SessionManager;
import swarm.server.structs.ServerCellAddressMapping;
import swarm.server.telemetry.TelemetryDatabase;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.structs.E_GetCellAddressError;
import swarm.shared.structs.GetCellAddressResult;
import swarm.shared.transaction.E_ResponseError;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class logAssert extends A_DefaultRequestHandler
{
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		UserSession session = m_serverContext.sessionMngr.getSession(request, response);
		
		int accountId = session != null ? session.getAccountId() : -1;
		String ip = ((HttpServletRequest) request.getNativeRequest()).getRemoteAddr();
		
		ServerTelemetryAssert telemetryAssert = new ServerTelemetryAssert(m_serverContext.jsonFactory, request.getJsonArgs(), ip, accountId);
		
		m_serverContext.telemetryDb.put(telemetryAssert);
	}
}
