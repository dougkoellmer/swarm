package swarm.server.account;

import swarm.server.app.bhA_ServerApp;
import swarm.server.app.bhI_RequestRedirector;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.session.bhSessionManager;
import swarm.server.telemetry.bhTelemetryDatabase;
import swarm.server.transaction.bhInlineTransactionManager;
import swarm.server.transaction.bhServerTransactionManager;
import swarm.shared.app.sm;

public class sm_s extends sm
{
	public static bhServerTransactionManager txnMngr;
	public static bhInlineTransactionManager inlineTxnMngr;
	public static bhBlobManagerFactory blobMngrFactory;
	public static bhSessionManager sessionMngr;
	public static bhServerAccountManager accountMngr;
	public static bhTelemetryDatabase telemetryDb;
	public static bhA_ServerApp app;
	public static bhI_RequestRedirector requestRedirector;
}
