package swarm.server.app;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.rdbms.AppEngineDriver;

import swarm.client.app.smClientAppConfig;
import swarm.server.account.smAccountDatabase;
import swarm.server.account.smServerAccountManager;
import swarm.server.account.sm_s;
import swarm.server.code.smServerCodeCompiler;
import swarm.server.data.blob.smBlobManagerFactory;
import swarm.server.entities.smServerGrid;
import swarm.server.handlers.admin.adminHandler;
import swarm.server.handlers.admin.smI_HomeCellCreator;
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
import swarm.server.session.smSessionManager;
import swarm.server.telemetry.smTelemetryDatabase;
import swarm.server.thirdparty.json.smServerJsonFactory;
import swarm.server.thirdparty.servlet.smServletRedirector;
import swarm.server.transaction.smE_AdminRequestPath;
import swarm.server.transaction.smE_DebugRequestPath;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smI_TransactionScopeListener;
import swarm.server.transaction.smInlineTransactionManager;
import swarm.server.transaction.smServerTransactionManager;
import swarm.shared.smE_AppEnvironment;
import swarm.shared.app.sm;
import swarm.shared.app.smA_App;
import swarm.shared.app.smS_App;
import swarm.shared.debugging.smI_AssertionDelegate;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.json.smJsonHelper;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_TelemetryRequestPath;
import swarm.shared.transaction.smI_RequestPath;
import swarm.shared.transaction.smRequestPathManager;

public abstract class smA_ServerApp extends smA_App
{
	private static final Logger s_logger = Logger.getLogger(smA_ServerApp.class.getName());
	
	private smServerAppConfig m_appConfig;
	
	protected smA_ServerApp()
	{
		super(smE_AppEnvironment.SERVER);
	}
	
	public smServerAppConfig getConfig()
	{
		return m_appConfig;
	}
	
	protected void entryPoint(smServerAppConfig appConfig)
	{
		m_appConfig = appConfig;
		sm_s.app = this;
		
		smU_Debug.setDelegate(new smI_AssertionDelegate()
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
		
		sm_s.jsonFactory = new smServerJsonFactory();
		sm_s.codeCompiler = new smServerCodeCompiler();
		sm.requestPathMngr = new smRequestPathManager(sm_s.jsonFactory, appConfig.verboseTransactions);
		sm_s.txnMngr = new smServerTransactionManager((smA_ServerJsonFactory) sm_s.jsonFactory, sm.requestPathMngr, appConfig.verboseTransactions);
		sm_s.inlineTxnMngr = new smInlineTransactionManager((smA_ServerJsonFactory) sm_s.jsonFactory, appConfig.appId, appConfig.verboseTransactions);
		sm_s.blobMngrFactory = new smBlobManagerFactory();
		sm_s.sessionMngr = new smSessionManager();
		sm_s.accountMngr = new smServerAccountManager(new smAccountDatabase(appConfig.databaseUrl, appConfig.accountsDatabase));
		sm_s.telemetryDb = new smTelemetryDatabase(appConfig.databaseUrl, appConfig.telemetryDatabase);
		sm_s.requestRedirector = new smServletRedirector(appConfig.mainPage);
		
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
	
	private void addTxnScopeListener(smI_TransactionScopeListener listener)
	{
		sm_s.txnMngr.addScopeListener(listener);
		sm_s.inlineTxnMngr.addScopeListener(listener);
	}
	
	private static void addDebugPathResponseErrors()
	{
		sm_s.txnMngr.setDebugResponseError(smE_RequestPath.syncCode);
	}
	
	private void addClientHandlers()
	{
		smServerTransactionManager txnManager = sm_s.txnMngr;
		sm.requestPathMngr.register(smE_RequestPath.values());
		
		getCode getCodeHandler = new getCode();
		
		txnManager.setRequestHandler(getCodeHandler,				smE_RequestPath.getCode);
		txnManager.setRequestHandler(new syncCode(),				smE_RequestPath.syncCode);
		txnManager.setRequestHandler(new getCellAddress(),			smE_RequestPath.getCellAddress);
		txnManager.setRequestHandler(new getCellAddressMapping(),	smE_RequestPath.getCellAddressMapping);
		txnManager.setRequestHandler(new getUserData(false),		smE_RequestPath.getUserData);
		txnManager.setRequestHandler(new getGridData(),				smE_RequestPath.getGridData);
		txnManager.setRequestHandler(new signIn(),					smE_RequestPath.signIn);
		txnManager.setRequestHandler(new signUp(m_appConfig.publicRecaptchaKey, m_appConfig.privateRecaptchaKey), smE_RequestPath.signUp);
		txnManager.setRequestHandler(new signOut(),					smE_RequestPath.signOut);
		txnManager.setRequestHandler(new getAccountInfo(),			smE_RequestPath.getAccountInfo);
		txnManager.setRequestHandler(new setNewDesiredPassword(),	smE_RequestPath.setNewDesiredPassword);
		txnManager.setRequestHandler(new getPasswordChangeToken(),	smE_RequestPath.getPasswordChangeToken);
		txnManager.setRequestHandler(new getServerVersion(),		smE_RequestPath.getServerVersion);
		
		txnManager.addDeferredHandler(getCodeHandler);
	}
	
	private static void addAdminHandlers(Class<? extends smI_HomeCellCreator> T_homeCellCreator)
	{
		smServerTransactionManager txnManager = sm_s.txnMngr;
		sm.requestPathMngr.register(smE_AdminRequestPath.values());
		
		setAdminHandler(new createGrid(smServerGrid.class),						smE_AdminRequestPath.createGrid);
		setAdminHandler(new deactivateUserCells(),								smE_AdminRequestPath.deactivateUserCells);
		setAdminHandler(new refreshHomeCells(T_homeCellCreator),				smE_AdminRequestPath.refreshHomeCells);
		setAdminHandler(new clearCell(),										smE_AdminRequestPath.clearCell);
		setAdminHandler(new recompileCells(),									smE_AdminRequestPath.recompileCells);
	}
	
	protected static void setAdminHandler(smI_RequestHandler handler, smI_RequestPath path)
	{
		smServerTransactionManager txnManager = sm_s.txnMngr;
		
		txnManager.setRequestHandler(new adminHandler(handler), path);
	}
	
	private static void addTelemetryHandlers()
	{
		smServerTransactionManager txnManager = sm_s.txnMngr;
		sm.requestPathMngr.register(smE_TelemetryRequestPath.values());
		
		txnManager.setRequestHandler(new logAssert(),	smE_TelemetryRequestPath.logAssert);
	}
	
	private static void addDebugHandlers()
	{
		smServerTransactionManager txnManager = sm_s.txnMngr;
		sm.requestPathMngr.register(smE_DebugRequestPath.values());
		
		txnManager.setRequestHandler(new sessionQueryTest(),		smE_DebugRequestPath.sessionQueryTest);
	}
}
