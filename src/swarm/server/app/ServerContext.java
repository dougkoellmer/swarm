package swarm.server.app;

import swarm.server.account.ServerAccountManager;
import swarm.server.code.ServerCodeCompiler;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.session.SessionManager;
import swarm.server.telemetry.TelemetryDatabase;
import swarm.server.thirdparty.servlet.ServletRedirector;
import swarm.server.transaction.InlineTransactionManager;
import swarm.server.transaction.ServerTransactionManager;
import swarm.shared.app.BaseAppContext;
import swarm.shared.json.A_JsonFactory;

public class ServerContext extends BaseAppContext
{
	public BlobManagerFactory blobMngrFactory;
	public SessionManager sessionMngr;
	public ServerAccountManager accountMngr;
	public TelemetryDatabase telemetryDb;
	public InlineTransactionManager inlineTxnMngr;
	public ServletRedirector redirector;
	public ServerTransactionManager txnMngr;
	
	public ServerAppConfig config;
}
