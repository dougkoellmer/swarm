package swarm.server.app;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.rdbms.AppEngineDriver;

import swarm.client.app.bhClientAppConfig;
import swarm.server.account.bhAccountDatabase;
import swarm.server.account.bhServerAccountManager;
import swarm.server.account.sm_s;
import swarm.server.code.bhServerCodeCompiler;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.entities.bhServerGrid;
import swarm.server.handlers.admin.adminHandler;
import swarm.server.handlers.admin.bhI_HomeCellCreator;
import swarm.server.handlers.admin.clearCell;
import swarm.server.handlers.admin.createGrid;
import swarm.server.handlers.admin.deactivateUserCells;
import swarm.server.handlers.admin.recompileCells;
import swarm.server.handlers.admin.refreshHomeCells;
import swarm.server.handlers.normal.getAccountInfo;
import swarm.server.handlers.normal.getCellAddress;
import swarm.server.handlers.normal.getCellAddressMapping;
import swarm.server.handlers.normal.getCode;
import swarm.server.handlers.normal.getGridData;
import swarm.server.handlers.normal.getPasswordChangeToken;
import swarm.server.handlers.normal.getServerVersion;
import swarm.server.handlers.normal.getUserData;
import swarm.server.handlers.normal.logAssert;
import swarm.server.handlers.normal.sessionQueryTest;
import swarm.server.handlers.normal.setNewDesiredPassword;
import swarm.server.handlers.normal.signIn;
import swarm.server.handlers.normal.signOut;
import swarm.server.handlers.normal.signUp;
import swarm.server.handlers.normal.syncCode;
import swarm.server.session.bhSessionManager;
import swarm.server.telemetry.bhTelemetryDatabase;
import swarm.server.thirdparty.json.bhServerJsonFactory;
import swarm.server.thirdparty.servlet.bhServletRedirector;
import swarm.server.transaction.bhE_AdminRequestPath;
import swarm.server.transaction.bhE_DebugRequestPath;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhI_TransactionScopeListener;
import swarm.server.transaction.bhInlineTransactionManager;
import swarm.server.transaction.bhServerTransactionManager;
import swarm.shared.bhE_AppEnvironment;
import swarm.shared.app.sm;
import swarm.shared.app.bhA_App;
import swarm.shared.app.bhS_App;
import swarm.shared.debugging.bhI_AssertionDelegate;
import swarm.shared.debugging.bhU_Debug;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.transaction.bhE_RequestPath;
import swarm.shared.transaction.bhE_TelemetryRequestPath;
import swarm.shared.transaction.bhI_RequestPath;
import swarm.shared.transaction.bhRequestPathManager;

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
		sm_s.app = this;
		
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
		
		sm_s.jsonFactory = new bhServerJsonFactory();
		sm_s.codeCompiler = new bhServerCodeCompiler();
		sm.requestPathMngr = new bhRequestPathManager(sm_s.jsonFactory, appConfig.verboseTransactions);
		sm_s.txnMngr = new bhServerTransactionManager((bhA_ServerJsonFactory) sm_s.jsonFactory, sm.requestPathMngr, appConfig.verboseTransactions);
		sm_s.inlineTxnMngr = new bhInlineTransactionManager((bhA_ServerJsonFactory) sm_s.jsonFactory, appConfig.appId, appConfig.verboseTransactions);
		sm_s.blobMngrFactory = new bhBlobManagerFactory();
		sm_s.sessionMngr = new bhSessionManager();
		sm_s.accountMngr = new bhServerAccountManager(new bhAccountDatabase(appConfig.databaseUrl, appConfig.accountsDatabase));
		sm_s.telemetryDb = new bhTelemetryDatabase(appConfig.databaseUrl, appConfig.telemetryDatabase);
		sm_s.requestRedirector = new bhServletRedirector(appConfig.mainPage);
		
		addClientHandlers();
		addAdminHandlers(m_appConfig.T_homeCellCreator);
		addTelemetryHandlers();
		//addDebugHandlers();
		//addDebugPathResponseErrors();
		
		addTxnScopeListener(sm_s.blobMngrFactory);
		addTxnScopeListener(sm_s.sessionMngr);
		addTxnScopeListener(sm_s.accountMngr.getAccountDb());
		addTxnScopeListener(sm_s.telemetryDb);
	}
	
	private void addTxnScopeListener(bhI_TransactionScopeListener listener)
	{
		sm_s.txnMngr.addScopeListener(listener);
		sm_s.inlineTxnMngr.addScopeListener(listener);
	}
	
	private static void addDebugPathResponseErrors()
	{
		sm_s.txnMngr.setDebugResponseError(bhE_RequestPath.syncCode);
	}
	
	private void addClientHandlers()
	{
		bhServerTransactionManager txnManager = sm_s.txnMngr;
		sm.requestPathMngr.register(bhE_RequestPath.values());
		
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
		bhServerTransactionManager txnManager = sm_s.txnMngr;
		sm.requestPathMngr.register(bhE_AdminRequestPath.values());
		
		setAdminHandler(new createGrid(bhServerGrid.class),						bhE_AdminRequestPath.createGrid);
		setAdminHandler(new deactivateUserCells(),								bhE_AdminRequestPath.deactivateUserCells);
		setAdminHandler(new refreshHomeCells(T_homeCellCreator),				bhE_AdminRequestPath.refreshHomeCells);
		setAdminHandler(new clearCell(),										bhE_AdminRequestPath.clearCell);
		setAdminHandler(new recompileCells(),									bhE_AdminRequestPath.recompileCells);
	}
	
	protected static void setAdminHandler(bhI_RequestHandler handler, bhI_RequestPath path)
	{
		bhServerTransactionManager txnManager = sm_s.txnMngr;
		
		txnManager.setRequestHandler(new adminHandler(handler), path);
	}
	
	private static void addTelemetryHandlers()
	{
		bhServerTransactionManager txnManager = sm_s.txnMngr;
		sm.requestPathMngr.register(bhE_TelemetryRequestPath.values());
		
		txnManager.setRequestHandler(new logAssert(),	bhE_TelemetryRequestPath.logAssert);
	}
	
	private static void addDebugHandlers()
	{
		bhServerTransactionManager txnManager = sm_s.txnMngr;
		sm.requestPathMngr.register(bhE_DebugRequestPath.values());
		
		txnManager.setRequestHandler(new sessionQueryTest(),		bhE_DebugRequestPath.sessionQueryTest);
	}
}
