package b33hive.server.account;

import b33hive.server.app.bhA_ServerApp;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.session.bhSessionManager;
import b33hive.server.telemetry.bhTelemetryDatabase;
import b33hive.server.transaction.bhInlineTransactionManager;
import b33hive.server.transaction.bhServerTransactionManager;
import b33hive.shared.app.bh;

public class bh_s extends bh
{
	public static bhServerTransactionManager txnMngr;
	public static bhInlineTransactionManager inlineTxnMngr;
	public static bhBlobManagerFactory blobMngrFactory;
	public static bhSessionManager sessionMngr;
	public static bhServerAccountManager accountMngr;
	public static bhTelemetryDatabase telemetryDb;
	public static bhA_ServerApp app;
}
