package b33hive.server.app;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.rdbms.AppEngineDriver;

import b33hive.client.app.bhClientAppConfig;
import b33hive.server.account.bhAccountDatabase;
import b33hive.server.account.bhServerAccountManager;
import b33hive.server.account.bh_s;
import b33hive.server.code.bhServerCodeCompiler;
import b33hive.server.data.blob.bhBlobManagerFactory;
import b33hive.server.entities.bhServerGrid;
import b33hive.server.handlers.admin.adminHandler;
import b33hive.server.handlers.admin.bhI_HomeCellCreator;
import b33hive.server.handlers.admin.clearCell;
import b33hive.server.handlers.admin.createGrid;
import b33hive.server.handlers.admin.deactivateUserCells;
import b33hive.server.handlers.admin.recompileCells;
import b33hive.server.handlers.admin.refreshHomeCells;
import b33hive.server.handlers.normal.getAccountInfo;
import b33hive.server.handlers.normal.getCellAddress;
import b33hive.server.handlers.normal.getCellAddressMapping;
import b33hive.server.handlers.normal.getCode;
import b33hive.server.handlers.normal.getGridData;
import b33hive.server.handlers.normal.getPasswordChangeToken;
import b33hive.server.handlers.normal.getServerVersion;
import b33hive.server.handlers.normal.getUserData;
import b33hive.server.handlers.normal.logAssert;
import b33hive.server.handlers.normal.sessionQueryTest;
import b33hive.server.handlers.normal.setNewDesiredPassword;
import b33hive.server.handlers.normal.signIn;
import b33hive.server.handlers.normal.signOut;
import b33hive.server.handlers.normal.signUp;
import b33hive.server.handlers.normal.syncCode;
import b33hive.server.session.bhSessionManager;
import b33hive.server.telemetry.bhTelemetryDatabase;
import b33hive.server.thirdparty.json.bhServerJsonFactory;
import b33hive.server.thirdparty.servlet.bhServletRedirector;
import b33hive.server.transaction.bhE_AdminRequestPath;
import b33hive.server.transaction.bhE_DebugRequestPath;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhI_TransactionScopeListener;
import b33hive.server.transaction.bhInlineTransactionManager;
import b33hive.server.transaction.bhServerTransactionManager;
import b33hive.shared.bhE_AppEnvironment;
import b33hive.shared.app.bh;
import b33hive.shared.app.bhA_App;
import b33hive.shared.app.bhS_App;
import b33hive.shared.debugging.bhI_AssertionDelegate;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhE_TelemetryRequestPath;
import b33hive.shared.transaction.bhI_RequestPath;
import b33hive.shared.transaction.bhRequestPathManager;

public abstract class bhA_ServerApp extends bhA_App
{
	private static final Logger s_logger = Logger.getLogger(bhA_ServerApp.class.getName());
	
	private bhServerAppConfig m_appConfig;
	
	protected bhA_ServerApp()
	{
		super(bhE_AppEnvironment.SERVER);
	}
	
	public bhServerAppConfig getConfig()
	{
		return m_appConfig;
	}
	
	protected void entryPoint(bhServerAppConfig appConfig)
	{
		m_appConfig = appConfig;
		bh_s.app = this;
		
		bhU_Debug.setDelegate(new bhI_AssertionDelegate()
		{
			@Override
			public void doAssert(String message)
			{
				assert(false);
			}
		});
		
		try
		{
			DriverManager.registerDriver(new AppEngineDriver());
		}
		catch (SQLException e1)
		{
			s_logger.log(Level.SEVERE, "Could not start up sql databases.", e1);
		}
		
		bh_s.jsonFactory = new bhServerJsonFactory();
		bh_s.codeCompiler = new bhServerCodeCompiler();
		bh.requestPathMngr = new bhRequestPathManager(bh_s.jsonFactory, appConfig.verboseTransactions);
		bh_s.txnMngr = new bhServerTransactionManager((bhA_ServerJsonFactory) bh_s.jsonFactory, bh.requestPathMngr, appConfig.verboseTransactions);
		bh_s.inlineTxnMngr = new bhInlineTransactionManager((bhA_ServerJsonFactory) bh_s.jsonFactory, appConfig.verboseTransactions);
		bh_s.blobMngrFactory = new bhBlobManagerFactory();
		bh_s.sessionMngr = new bhSessionManager();
		bh_s.accountMngr = new bhServerAccountManager(new bhAccountDatabase(appConfig.databaseUrl, appConfig.accountsDatabase));
		bh_s.telemetryDb = new bhTelemetryDatabase(appConfig.databaseUrl, appConfig.telemetryDatabase);
		bh_s.requestRedirector = new bhServletRedirector(appConfig.mainPage);
		
		addClientHandlers();
		addAdminHandlers(m_appConfig.T_homeCellCreator);
		addTelemetryHandlers();
		//addDebugHandlers();
		//addDebugPathResponseErrors();
		
		addTxnScopeListener(bh_s.blobMngrFactory);
		addTxnScopeListener(bh_s.sessionMngr);
		addTxnScopeListener(bh_s.accountMngr.getAccountDb());
		addTxnScopeListener(bh_s.telemetryDb);
	}
	
	private void addTxnScopeListener(bhI_TransactionScopeListener listener)
	{
		bh_s.txnMngr.addScopeListener(listener);
		bh_s.inlineTxnMngr.addScopeListener(listener);
	}
	
	private static void addDebugPathResponseErrors()
	{
		bh_s.txnMngr.setDebugResponseError(bhE_RequestPath.syncCode);
	}
	
	private void addClientHandlers()
	{
		bhServerTransactionManager txnManager = bh_s.txnMngr;
		bh.requestPathMngr.register(bhE_RequestPath.values());
		
		getCode getCodeHandler = new getCode();
		
		txnManager.setRequestHandler(getCodeHandler,				bhE_RequestPath.getCode);
		txnManager.setRequestHandler(new syncCode(),				bhE_RequestPath.syncCode);
		txnManager.setRequestHandler(new getCellAddress(),			bhE_RequestPath.getCellAddress);
		txnManager.setRequestHandler(new getCellAddressMapping(),	bhE_RequestPath.getCellAddressMapping);
		txnManager.setRequestHandler(new getUserData(false),		bhE_RequestPath.getUserData);
		txnManager.setRequestHandler(new getGridData(),				bhE_RequestPath.getGridData);
		txnManager.setRequestHandler(new signIn(),					bhE_RequestPath.signIn);
		txnManager.setRequestHandler(new signUp(m_appConfig.publicRecaptchaKey, m_appConfig.privateRecaptchaKey), bhE_RequestPath.signUp);
		txnManager.setRequestHandler(new signOut(),					bhE_RequestPath.signOut);
		txnManager.setRequestHandler(new getAccountInfo(),			bhE_RequestPath.getAccountInfo);
		txnManager.setRequestHandler(new setNewDesiredPassword(),	bhE_RequestPath.setNewDesiredPassword);
		txnManager.setRequestHandler(new getPasswordChangeToken(),	bhE_RequestPath.getPasswordChangeToken);
		txnManager.setRequestHandler(new getServerVersion(),		bhE_RequestPath.getServerVersion);
		
		txnManager.addDeferredHandler(getCodeHandler);
	}
	
	private static void addAdminHandlers(Class<? extends bhI_HomeCellCreator> T_homeCellCreator)
	{
		bhServerTransactionManager txnManager = bh_s.txnMngr;
		bh.requestPathMngr.register(bhE_AdminRequestPath.values());
		
		setAdminHandler(new createGrid(bhServerGrid.class),						bhE_AdminRequestPath.createGrid);
		setAdminHandler(new deactivateUserCells(),								bhE_AdminRequestPath.deactivateUserCells);
		setAdminHandler(new refreshHomeCells(T_homeCellCreator),				bhE_AdminRequestPath.refreshHomeCells);
		setAdminHandler(new clearCell(),										bhE_AdminRequestPath.clearCell);
		setAdminHandler(new recompileCells(),									bhE_AdminRequestPath.recompileCells);
	}
	
	protected static void setAdminHandler(bhI_RequestHandler handler, bhI_RequestPath path)
	{
		bhServerTransactionManager txnManager = bh_s.txnMngr;
		
		txnManager.setRequestHandler(new adminHandler(handler), path);
	}
	
	private static void addTelemetryHandlers()
	{
		bhServerTransactionManager txnManager = bh_s.txnMngr;
		bh.requestPathMngr.register(bhE_TelemetryRequestPath.values());
		
		txnManager.setRequestHandler(new logAssert(),	bhE_TelemetryRequestPath.logAssert);
	}
	
	private static void addDebugHandlers()
	{
		bhServerTransactionManager txnManager = bh_s.txnMngr;
		bh.requestPathMngr.register(bhE_DebugRequestPath.values());
		
		txnManager.setRequestHandler(new sessionQueryTest(),		bhE_DebugRequestPath.sessionQueryTest);
	}
}
