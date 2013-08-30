package swarm.server.account;

import swarm.server.app.smA_ServerApp;
import swarm.server.app.smI_RequestRedirector;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.session.smSessionManager;
import swarm.server.telemetry.smTelemetryDatabase;
import swarm.server.transaction.smInlineTransactionManager;
import swarm.server.transaction.smServerTransactionManager;
import swarm.shared.app.smSharedAppContext;

public class sm_s extends smSharedAppContext
{
	public static smServerTransactionManager txnMngr;
	public static smInlineTransactionManager inlineTxnMngr;
	public static smBlobManagerFactory blobMngrFactory;
	public static smSessionManager sessionMngr;
	public static smServerAccountManager accountMngr;
	public static smTelemetryDatabase telemetryDb;
	public static smA_ServerApp app;
	public static smI_RequestRedirector requestRedirector;
}
