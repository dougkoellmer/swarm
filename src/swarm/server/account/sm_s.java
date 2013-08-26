package swarm.server.account;

import swarm.server.app.smA_ServerApp;
import swarm.server.app.smI_RequestRedirector;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.session.smSessionManager;
import swarm.server.telemetry.smTelemetryDatabase;
import swarm.server.transaction.smInlineTransactionManager;
import swarm.server.transaction.smServerTransactionManager;
import swarm.shared.app.sm;

public class sm_s extends sm
{
	public static smServerTransactionManager txnMngr;
	public static bhInlineTransactionManager inlineTxnMngr;
	public static bhBlobManagerFactory blobMngrFactory;
	public static smSessionManager sessionMngr;
	public static smServerAccountManager accountMngr;
	public static bhTelemetryDatabase telemetryDb;
	public static smA_ServerApp app;
	public static smI_RequestRedirector requestRedirector;
}
