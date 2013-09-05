package swarm.server.app;

import swarm.server.account.smServerAccountManager;
import swarm.server.code.smServerCodeCompiler;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.session.smSessionManager;
import swarm.server.telemetry.smTelemetryDatabase;
import swarm.server.thirdparty.servlet.smServletRedirector;
import swarm.server.transaction.smInlineTransactionManager;
import swarm.server.transaction.smServerTransactionManager;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.json.smA_JsonFactory;

public class smServerContext extends smSharedAppContext
{
	public smBlobManagerFactory blobMngrFactory;
	public smSessionManager sessionMngr;
	public smServerAccountManager accountMngr;
	public smTelemetryDatabase telemetryDb;
	public smInlineTransactionManager inlineTxnMngr;
	public smServletRedirector redirector;
	public smServerTransactionManager txnMngr;
	
	public smServerAppConfig config;
}
