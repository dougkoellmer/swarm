package swarm.server.app;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.rdbms.AppEngineDriver;

import swarm.client.app.smClientAppConfig;
import swarm.server.account.smAccountDatabase;
import swarm.server.account.smServerAccountManager;

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
import swarm.server.transaction.smA_DefaultRequestHandler;
import swarm.server.transaction.smE_AdminRequestPath;
import swarm.server.transaction.smE_DebugRequestPath;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smI_TransactionScopeListener;
import swarm.server.transaction.smInlineTransactionManager;
import swarm.server.transaction.smServerTransactionManager;
import swarm.shared.smE_AppEnvironment;
import swarm.shared.account.smSignInValidator;
import swarm.shared.account.smSignUpValidator;
import swarm.shared.app.smSharedAppContext;
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
	
	private static smA_ServerApp s_instance;
	
	private smServerAppConfig m_appConfig;
	protected smServerContext m_context;
	
	protected smA_ServerApp()
	{
		super(smE_AppEnvironment.SERVER);
		
		s_instance = this;
		
		m_context = new smServerContext();
	}
	
	public static smA_ServerApp getInstance()
	{
		return s_instance;
	}
	
	public smServerContext getContext()
	{
		return m_context;
	}
	
	public smServerAppConfig getConfig()
	{
		return m_appConfig;
	}
	
	protected void entryPoint(smServerAppConfig appConfig)
	{
		m_appConfig = appConfig;
		
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
		
		boolean clientSide = false;
		smAccountDatabase accountDatabase = new smAccountDatabase(appConfig.databaseUrl, appConfig.accountsDatabase);
		smSignInValidator signInValidator = new smSignInValidator(clientSide);
		smSignUpValidator signUpValidator = new smSignUpValidator(clientSide);
		
		m_context.jsonFactory = new smServerJsonFactory();
		m_context.codeCompiler = new smServerCodeCompiler();
		m_context.requestPathMngr = new smRequestPathManager(m_context.jsonFactory, appConfig.verboseTransactions);
		m_context.txnMngr = new smServerTransactionManager((smA_ServerJsonFactory) m_context.jsonFactory, m_context.requestPathMngr, appConfig.verboseTransactions);
		m_context.inlineTxnMngr = new smInlineTransactionManager(m_context.txnMngr, (smA_ServerJsonFactory) m_context.jsonFactory, appConfig.appId, appConfig.verboseTransactions);
		m_context.blobMngrFactory = new smBlobManagerFactory();
		m_context.sessionMngr = new smSessionManager(m_context.blobMngrFactory, m_context.jsonFactory);
		m_context.accountMngr = new smServerAccountManager(signInValidator, signUpValidator, accountDatabase);
		m_context.telemetryDb = new smTelemetryDatabase(appConfig.databaseUrl, appConfig.telemetryDatabase);
		m_context.redirector = new smServletRedirector(appConfig.mainPage);
		
		addClientHandlers();
		addAdminHandlers(m_appConfig.T_homeCellCreator);
		addTelemetryHandlers();
		//addDebugHandlers();
		//addDebugPathResponseErrors();
		
		addTxnScopeListener(m_context.blobMngrFactory);
		addTxnScopeListener(m_context.sessionMngr);
		addTxnScopeListener(m_context.accountMngr.getAccountDb());
		addTxnScopeListener(m_context.telemetryDb);
	}
	
	private void addTxnScopeListener(smI_TransactionScopeListener listener)
	{
		m_context.txnMngr.addScopeListener(listener);
		m_context.inlineTxnMngr.addScopeListener(listener);
	}
	
	private void addDebugPathResponseErrors()
	{
		m_context.txnMngr.setDebugResponseError(smE_RequestPath.syncCode);
	}
	
	private void addClientHandlers()
	{
		m_context.requestPathMngr.register(smE_RequestPath.values());
		
		getCode getCodeHandler = new getCode();
		
		setNormalHandler(getCodeHandler,				smE_RequestPath.getCode);
		setNormalHandler(new syncCode(),				smE_RequestPath.syncCode);
		setNormalHandler(new getCellAddress(),			smE_RequestPath.getCellAddress);
		setNormalHandler(new getCellAddressMapping(),	smE_RequestPath.getCellAddressMapping);
		setNormalHandler(new getUserData(false, m_appConfig.gridExpansionDelta),		smE_RequestPath.getUserData);
		setNormalHandler(new getGridData(),				smE_RequestPath.getGridData);
		setNormalHandler(new signIn(),					smE_RequestPath.signIn);
		setNormalHandler(new signUp(m_appConfig.publicRecaptchaKey, m_appConfig.privateRecaptchaKey), smE_RequestPath.signUp);
		setNormalHandler(new signOut(),					smE_RequestPath.signOut);
		setNormalHandler(new getAccountInfo(),			smE_RequestPath.getAccountInfo);
		setNormalHandler(new setNewDesiredPassword(),	smE_RequestPath.setNewDesiredPassword);
		setNormalHandler(new getPasswordChangeToken(),	smE_RequestPath.getPasswordChangeToken);
		setNormalHandler(new getServerVersion(),		smE_RequestPath.getServerVersion);
		
		m_context.txnMngr.addDeferredHandler(getCodeHandler);
	}
	
	private void setNormalHandler(smA_DefaultRequestHandler handler, smI_RequestPath path)
	{
		handler.init(m_context);
		
		m_context.txnMngr.setRequestHandler(handler, path);
	}
	
	private void addAdminHandlers(Class<? extends smI_HomeCellCreator> T_homeCellCreator)
	{
		m_context.requestPathMngr.register(smE_AdminRequestPath.values());
		
		setAdminHandler(new createGrid(smServerGrid.class),						smE_AdminRequestPath.createGrid);
		setAdminHandler(new deactivateUserCells(),								smE_AdminRequestPath.deactivateUserCells);
		setAdminHandler(new refreshHomeCells(T_homeCellCreator),				smE_AdminRequestPath.refreshHomeCells);
		setAdminHandler(new clearCell(),										smE_AdminRequestPath.clearCell);
		setAdminHandler(new recompileCells(),									smE_AdminRequestPath.recompileCells);
	}
	
	protected void setAdminHandler(smA_DefaultRequestHandler handler, smI_RequestPath path)
	{
		handler.init(m_context);
		
		m_context.txnMngr.setRequestHandler(new adminHandler(m_context.sessionMngr, handler), path);
	}
	
	private void addTelemetryHandlers()
	{
		smServerTransactionManager txnManager = m_context.txnMngr;
		m_context.requestPathMngr.register(smE_TelemetryRequestPath.values());
		
		txnManager.setRequestHandler(new logAssert(),	smE_TelemetryRequestPath.logAssert);
	}
	
	private void addDebugHandlers()
	{
		smServerTransactionManager txnManager = m_context.txnMngr;
		m_context.requestPathMngr.register(smE_DebugRequestPath.values());
		
		txnManager.setRequestHandler(new sessionQueryTest(),		smE_DebugRequestPath.sessionQueryTest);
	}
}
